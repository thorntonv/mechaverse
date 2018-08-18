package org.mechaverse.simulation.common.cellautomaton.simulation;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * A cellular automaton simulator that splits separate bits in the integer values between multiple entities.
 */
public class BitwiseCellularAutomatonSimulator implements CellularAutomatonSimulator {

    private static class AutomatonStateCache {
        private int[][] cache;
        private boolean valid;
        private boolean dirty;
        private int bitsPerEntity;
        private BiConsumer<Integer, int[]> getFunction;
        private BiConsumer<Integer, int[]> setFunction;

        AutomatonStateCache(int numAutomata, int stateSize, int bitsPerEntity,
                            BiConsumer<Integer, int[]> getFunction, BiConsumer<Integer, int[]> setFunction) {
            this.bitsPerEntity = bitsPerEntity;
            this.getFunction = getFunction;
            this.setFunction = setFunction;

            cache = new int[numAutomata][];
            for (int idx = 0; idx < cache.length; idx++) {
                cache[idx] = new int[stateSize];
            }
            valid = false;
            dirty = false;
        }

        public void get(int index, int[] state) {
            if (!valid) {
                cache();
            }
            int simulatorAutomatonIndex = getSimulatorAutomatonIndex(index);
            int srcBitOffset = getSimulatorAutomatonBitOffset(index);
            int[] src = cache[simulatorAutomatonIndex];
            copyIntArrayBits(src, srcBitOffset, bitsPerEntity, state, 0);
        }

        public void set(int index, int[] state) {
            if (!valid) {
                cache();
            }
            int destBitOffset = getSimulatorAutomatonBitOffset(index);
            int simulatorAutomatonIndex = getSimulatorAutomatonIndex(index);
            int[] dest = cache[simulatorAutomatonIndex];
            copyIntArrayBits(state, 0, bitsPerEntity, dest, destBitOffset);
            dirty = true;
        }

        private void cache() {
            for (int idx = 0; idx < cache.length; idx++) {
                getFunction.accept(idx, cache[idx]);
            }
            valid = true;
            dirty = false;
        }

        private void flush() {
            for (int idx = 0; idx < cache.length; idx++) {
                setFunction.accept(idx, cache[idx]);
            }
            valid = true;
            dirty = false;
        }

        private int getSimulatorAutomatonIndex(int index) {
            return index * bitsPerEntity / Integer.SIZE;
        }

        private int getSimulatorAutomatonBitOffset(int index) {
            return (index * bitsPerEntity) % Integer.SIZE;
        }

        /**
         * Copies the specified bits from the int values in the src array to the destination array.
         */
        private void copyIntArrayBits(int[] src, int srcBitOffset, int srcBitCount, int[] dest, int destBitOffset) {
            int srcBitMask = ((1 << srcBitCount) - 1) << srcBitOffset;
            int destBitMask = ~(srcBitMask << destBitOffset);
            for (int idx = 0; idx < src.length; idx++) {
                dest[idx] = dest[idx] & destBitMask | ((src[idx] & srcBitMask) << destBitOffset);
            }
        }

        boolean isDirty() {
            return dirty;
        }

        void invalidate() {
            valid = false;
        }
    }

    private CellularAutomatonSimulator cellularAutomatonSimulator;
    private int bitsPerEntity;
    private CellularAutomatonAllocator allocator;

    private AutomatonStateCache stateCache;
    private AutomatonStateCache inputCache;
    private AutomatonStateCache outputCache;

    private List<AutomatonStateCache> caches;

    public BitwiseCellularAutomatonSimulator(CellularAutomatonSimulator cellularAutomatonSimulator, int bitsPerEntity) {
        Preconditions.checkState(bitsPerEntity > 0 && bitsPerEntity < Integer.SIZE);
        Preconditions.checkState(Integer.SIZE % bitsPerEntity == 0);

        this.cellularAutomatonSimulator = cellularAutomatonSimulator;
        this.bitsPerEntity = bitsPerEntity;
        this.allocator = new CellularAutomatonAllocator(cellularAutomatonSimulator.size() * Integer.SIZE / bitsPerEntity);

        stateCache = new AutomatonStateCache(
                cellularAutomatonSimulator.size(),
                cellularAutomatonSimulator.getAutomatonStateSize(),
                bitsPerEntity,
                cellularAutomatonSimulator::getAutomatonState,
                cellularAutomatonSimulator::setAutomatonState);

        inputCache = new AutomatonStateCache(
                cellularAutomatonSimulator.size(),
                cellularAutomatonSimulator.getAutomatonInputSize(),
                bitsPerEntity,
                (index, state) -> {},
                cellularAutomatonSimulator::setAutomatonInput);

        outputCache = new AutomatonStateCache(
                cellularAutomatonSimulator.size(),
                cellularAutomatonSimulator.getAutomatonOutputSize(),
                bitsPerEntity,
                cellularAutomatonSimulator::getAutomatonOutput,
                (index, state) -> {});

        this.caches = ImmutableList.of(stateCache, inputCache, outputCache);
    }

    @Override
    public CellularAutomatonAllocator getAllocator() {
        return allocator;
    }

    @Override
    public int size() {
        return cellularAutomatonSimulator.size() * Integer.SIZE / bitsPerEntity;
    }

    @Override
    public int getAutomatonInputSize() {
        return cellularAutomatonSimulator.getAutomatonInputSize();
    }

    @Override
    public int getAutomatonInputMapSize() {
        return cellularAutomatonSimulator.getAutomatonInputMapSize();
    }

    @Override
    public int getAutomatonStateSize() {
        return cellularAutomatonSimulator.getAutomatonStateSize();
    }

    @Override
    public int getAutomatonOutputSize() {
        return cellularAutomatonSimulator.getAutomatonOutputSize();
    }

    @Override
    public int getAutomatonOutputMapSize() {
        return cellularAutomatonSimulator.getAutomatonOutputMapSize();
    }

    @Override
    public void getAutomatonState(int index, int[] state) {
        stateCache.get(index, state);
    }

    @Override
    public void setAutomatonState(int index, int[] state) {
        stateCache.set(index, state);
    }

    @Override
    public void setAutomatonInputMap(int index, int[] inputMap) {
        int automatonIndex = getSimulatorAutomatonIndex(index);
        cellularAutomatonSimulator.setAutomatonInputMap(automatonIndex, inputMap);
    }

    @Override
    public void setAutomatonInput(int index, int[] input) {
        inputCache.set(index, input);
    }

    @Override
    public void setAutomatonOutputMap(int index, int[] outputMap) {
        int automatonIndex = getSimulatorAutomatonIndex(index);
        cellularAutomatonSimulator.setAutomatonOutputMap(automatonIndex, outputMap);
    }

    @Override
    public void getAutomatonOutput(int index, int[] output) {
        outputCache.get(index, output);
    }

    @Override
    public void update() {
        caches.forEach(cache -> {
            if (cache.isDirty()) {
                cache.flush();
            }
        });

        cellularAutomatonSimulator.update();

        stateCache.invalidate();
        outputCache.invalidate();
    }

    @Override
    public void close() throws Exception {
        cellularAutomatonSimulator.close();
    }

    private int getSimulatorAutomatonIndex(int index) {
        return index * bitsPerEntity / Integer.SIZE;
    }

}

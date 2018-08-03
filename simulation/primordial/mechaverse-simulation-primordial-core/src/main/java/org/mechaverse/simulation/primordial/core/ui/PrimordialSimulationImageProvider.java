package org.mechaverse.simulation.primordial.core.ui;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;

import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.ui.SimulationImageProvider;
import org.mechaverse.simulation.primordial.core.model.EntityType;
import org.mechaverse.simulation.primordial.core.model.PrimordialEnvironmentModel;
import com.google.common.collect.ImmutableSet;

@SuppressWarnings("WeakerAccess")
public class PrimordialSimulationImageProvider implements SimulationImageProvider<PrimordialEnvironmentModel, EntityModel<EntityType>, EntityType> {

    private static final String BARRIER_FILENAME = "barrier.png";
    private static final String DIRT_FILENAME = "dirt.png";
    private static final String FOOD_FILENAME = "food.png";
    private static final String ROCK_FILENAME = "rock.png";

    private static final Set<String> IMAGE_FILENAMES = ImmutableSet.<String>builder()
            .add(DIRT_FILENAME)
            .add(BARRIER_FILENAME)
            .add(FOOD_FILENAME)
            .add(ROCK_FILENAME)
            .build();

    private final Map<String, BufferedImage> imageMap = new HashMap<>();

    public static final int DEFAULT_CELL_SIZE = 24;

    private final int cellImageSize;

    public PrimordialSimulationImageProvider() throws IOException {
        for (String filename : IMAGE_FILENAMES) {
            imageMap.put(filename, ImageIO.read(getClass().getClassLoader().getResourceAsStream("images/" + filename)));
        }
        cellImageSize = imageMap.get(DIRT_FILENAME).getWidth();
    }

    @Override
    public BufferedImage getCellBackgroundImage(final PrimordialEnvironmentModel environmentModel,
            final int row, final int column) {
        return null; // imageMap.get(DIRT_FILENAME);
    }

    @Override
    public BufferedImage getEntityImage(final EntityModel<EntityType> entityModel,
            final PrimordialEnvironmentModel environmentModel) {
        switch (entityModel.getType()) {
            case ENTITY:
                return imageMap.get(FOOD_FILENAME);
            case BARRIER:
                return imageMap.get(BARRIER_FILENAME);
            case FOOD:
                return imageMap.get(ROCK_FILENAME);
        }
        return null;
    }

    @Override
    public int getZOrder(final EntityModel<EntityType> entityModel) {
        return EntityType.values().length - entityModel.getType().ordinal();
    }

    @Override
    public int getCellImageSize() {
        return cellImageSize;
    }

}

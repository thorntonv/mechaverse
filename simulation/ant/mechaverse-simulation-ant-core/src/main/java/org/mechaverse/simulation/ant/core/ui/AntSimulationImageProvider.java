package org.mechaverse.simulation.ant.core.ui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;

import org.mechaverse.simulation.ant.core.model.Ant;
import org.mechaverse.simulation.ant.core.model.CellEnvironment;
import org.mechaverse.simulation.ant.core.model.EntityType;
import org.mechaverse.simulation.common.model.Direction;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.ui.SimulationImageProvider;
import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableSet;

@SuppressWarnings("WeakerAccess")
public class AntSimulationImageProvider implements SimulationImageProvider<CellEnvironment, EntityModel<EntityType>, EntityType> {

    private static final String BARRIER_FILENAME = "barrier.png";
    private static final String DIRT_FILENAME = "dirt.png";
    private static final String FOOD_FILENAME = "food.png";
    private static final String ROCK_FILENAME = "rock.png";
    private static final String NEST_FILENAME = "nest.png";
    private static final String PHEROMONE_FILENAME = "pheromone.png";

    private static final Set<String> IMAGE_FILENAMES = ImmutableSet.<String>builder()
            .add(DIRT_FILENAME)
            .add(BARRIER_FILENAME)
            .add(FOOD_FILENAME)
            .add(ROCK_FILENAME)
            .add(NEST_FILENAME)
            .add(PHEROMONE_FILENAME)
            .addAll(getAntImageFileNames("red"))
            .addAll(getAntImageFileNames("black"))
            .build();

    private final Map<String, BufferedImage> imageMap = new HashMap<>();

    public static final int DEFAULT_CELL_SIZE = 24;

    private final int cellImageSize;

    public AntSimulationImageProvider() throws IOException {
        for (String filename : IMAGE_FILENAMES) {
            imageMap.put(filename, ImageIO.read(getClass().getClassLoader().getResourceAsStream("images/" + filename)));
        }
        cellImageSize = imageMap.get(DIRT_FILENAME).getWidth();
    }

    @Override
    public BufferedImage getCellBackgroundImage(final CellEnvironment environmentModel, final int row,
            final int column) {
        return imageMap.get(DIRT_FILENAME);
    }

    @Override
    public BufferedImage getEntityImage(final EntityModel<EntityType> entityModel,
            final CellEnvironment environmentModel) {
        switch (entityModel.getType()) {
            case NEST:
                return imageMap.get(NEST_FILENAME);
            case BARRIER:
                return imageMap.get(BARRIER_FILENAME);
            case FOOD:
                return imageMap.get(FOOD_FILENAME);
            case ROCK:
                return imageMap.get(ROCK_FILENAME);
            case ANT:
                return getAntImage((Ant) entityModel, environmentModel);
            case PHEROMONE:
                return imageMap.get(PHEROMONE_FILENAME);
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

    private static final Map<String, BufferedImage> antImageCache = new HashMap<>();

    private BufferedImage getAntImage(Ant ant, final CellEnvironment environmentModel) {
        String antImageFilename = getAntImageFilename("black", ant.getDirection());
        BufferedImage antImage = imageMap.get(antImageFilename);
        if (ant.getCarriedEntity() != null) {
            String key = antImageFilename + ant.getCarriedEntity().getType();
            if (antImageCache.containsKey(key)) {
                return antImageCache.get(key);
            }
            antImage = clone(antImage);
            Graphics2D g2d = antImage.createGraphics();
            BufferedImage carriedEntityImage = getEntityImage(ant.getCarriedEntity(), environmentModel);
            float carriedEntityScale = .5f;
            int carriedEntityWidth = (int) (carriedEntityImage.getWidth() * carriedEntityScale);
            int carriedEntityHeight = (int) (carriedEntityImage.getHeight() * carriedEntityScale);
            g2d.drawImage(carriedEntityImage,
                    (antImage.getWidth() - carriedEntityWidth) / 2,
                    (antImage.getHeight() - carriedEntityHeight) / 2,
                    carriedEntityWidth, carriedEntityHeight, null);
            antImageCache.put(key, antImage);
        }
        return antImage;
    }

    private static Set<String> getAntImageFileNames(String color) {
        Set<String> filenames = new HashSet<>();
        for (Direction direction : Direction.values()) {
            filenames.add(getAntImageFilename(color, direction));
        }
        return filenames;
    }

    private static String getAntImageFilename(String color, Direction direction) {
        String dirString = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, direction.name());
        return color + "-ant-" + dirString + ".png";
    }

    private static BufferedImage clone(BufferedImage image) {
        BufferedImage clone = new BufferedImage(image.getWidth(),
                image.getHeight(), image.getType());
        Graphics2D g2d = clone.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        return clone;
    }
}

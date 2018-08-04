package org.mechaverse.simulation.primordial.core.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.ui.SimulationImageProvider;
import org.mechaverse.simulation.primordial.core.model.EntityType;
import org.mechaverse.simulation.primordial.core.model.PrimordialEnvironmentModel;

@SuppressWarnings("WeakerAccess")
public class PrimordialSimulationImageProvider implements SimulationImageProvider<PrimordialEnvironmentModel, EntityModel<EntityType>, EntityType> {

    public static final int DEFAULT_CELL_SIZE = 8;

    private final int cellImageSize;

    private final Map<String, BufferedImage> imageCache = new HashMap<>();

    public PrimordialSimulationImageProvider() {
        cellImageSize = DEFAULT_CELL_SIZE;
    }

    @Override
    public BufferedImage getCellBackgroundImage(final PrimordialEnvironmentModel environmentModel,
            final int row, final int column) {

        BufferedImage image = imageCache.get("cellBackground");
        if(image != null) {
            return image;
        }

        image = createCellImage();
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
        g2d.setColor(new Color(85, 85, 85));
        g2d.fillRect(1, 1, image.getWidth() - 2, image.getHeight() - 2);
        imageCache.put("cellBackground", image);
        return image;
    }

    @Override
    public BufferedImage getEntityImage(final EntityModel<EntityType> entityModel,
            final PrimordialEnvironmentModel environmentModel) {
        String imageCacheKey = entityModel.getType().name();
        BufferedImage image = imageCache.get(imageCacheKey);
        if(image != null) {
            return image;
        }
        image = createCellImage();
        Graphics2D g2d = image.createGraphics();
        switch (entityModel.getType()) {
            case ENTITY:
                g2d.setColor(new Color(0, 200, 0));
                g2d.fillRect(1, 1, image.getWidth() - 2, image.getHeight() - 2);
                break;
            case BARRIER:
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
                break;
            case FOOD:
                g2d.setColor(new Color(0, 85, 200));
                g2d.fillRect(1, 1, image.getWidth() - 2, image.getHeight() - 2);
                break;
        }
        imageCache.put(imageCacheKey, image);
        return image;
    }

    @Override
    public int getZOrder(final EntityModel<EntityType> entityModel) {
        return EntityType.values().length - entityModel.getType().ordinal();
    }

    @Override
    public int getCellImageSize() {
        return cellImageSize;
    }

    private BufferedImage createCellImage() {
        GraphicsConfiguration config = GraphicsEnvironment
            .getLocalGraphicsEnvironment()
            .getDefaultScreenDevice()
            .getDefaultConfiguration();
        return config.createCompatibleImage(
            DEFAULT_CELL_SIZE, DEFAULT_CELL_SIZE, Transparency.OPAQUE);
    }
}

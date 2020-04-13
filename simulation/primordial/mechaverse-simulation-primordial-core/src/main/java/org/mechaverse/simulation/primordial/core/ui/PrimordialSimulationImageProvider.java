package org.mechaverse.simulation.primordial.core.ui;

import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.ui.SimulationImageProvider;
import org.mechaverse.simulation.primordial.core.model.EntityType;
import org.mechaverse.simulation.primordial.core.model.PrimordialEntityModel;
import org.mechaverse.simulation.primordial.core.model.PrimordialEnvironmentModel;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public class PrimordialSimulationImageProvider implements SimulationImageProvider<PrimordialEnvironmentModel, EntityModel<EntityType>, EntityType> {

    public static final int DEFAULT_CELL_SIZE = 5;

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
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
        imageCache.put("cellBackground", image);
        return image;
    }

    @Override
    public BufferedImage getEntityImage(final EntityModel<EntityType> entityModel,
            final PrimordialEnvironmentModel environmentModel) {
        String imageCacheKey = entityModel.getType().name();
        BufferedImage image = imageCache.get(imageCacheKey);
        if(image != null && entityModel.getType() != EntityType.ENTITY) {
            return image;
        }
        image = createCellImage();
        Graphics2D g2d = image.createGraphics();
        switch (entityModel.getType()) {
            case ENTITY:
                PrimordialEntityModel model = (PrimordialEntityModel) entityModel;
              int r = (int) (model.getStrainId() & 255) % 128 + 115;
              int g = (int) ((model.getStrainId() >> 8) & 255) % 128 + 115;
              int b = (int) ((model.getStrainId() >> 16) & 255) % 128 + 115;

                if(model.getStrainId() == 0) {
                    r = g = b = 255;
                }
                if (model.getEnergy() <= 0) {
                    r = r / 2;
                    g = g / 2;
                    b = b / 2;
                    r = g = b = 85;
                }
                g2d.setColor(new Color(r, g, b));
                g2d.fillRect(1, 1, image.getWidth() - 2, image.getHeight() - 2);
                break;
            case BARRIER:
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
                break;
            case FOOD:
                g2d.setColor(new Color(0, 200, 45));
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

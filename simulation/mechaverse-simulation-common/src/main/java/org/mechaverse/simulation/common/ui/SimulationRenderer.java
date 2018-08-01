package org.mechaverse.simulation.common.ui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.model.EnvironmentModel;
import org.mechaverse.simulation.common.model.SimulationModel;
import com.google.common.base.Preconditions;

/**
 * Renders the current state of a simulation environment to an image.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class SimulationRenderer<
        SIM_MODEL extends SimulationModel<ENV_MODEL, ENT_MODEL, ENT_TYPE>,
        ENV_MODEL extends EnvironmentModel<ENT_MODEL, ENT_TYPE>,
        ENT_MODEL extends EntityModel<ENT_TYPE>,
        ENT_TYPE extends Enum<ENT_TYPE>> {

    private final int cellSize;
    private SimulationImageProvider<ENV_MODEL, ENT_MODEL, ENT_TYPE> imageProvider;

    public SimulationRenderer(SimulationImageProvider<ENV_MODEL, ENT_MODEL, ENT_TYPE> imageProvider, int cellSize) {
        this.imageProvider = Preconditions.checkNotNull(imageProvider);
        this.cellSize = cellSize;
    }

    public BufferedImage draw(SIM_MODEL simulationModel, ENV_MODEL envModel) {
        return draw(simulationModel, envModel, 0, 0, envModel.getHeight(), envModel.getWidth());
    }

    public BufferedImage draw(SIM_MODEL simulationModel, ENV_MODEL envModel,
            int startRow, int startCol, int rowCount, int colCount) {
        BufferedImage image = new BufferedImage(colCount * cellSize,
                rowCount * cellSize, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        draw(g2d, simulationModel, envModel, startRow, startCol, rowCount, colCount);
        return image;
    }

    public void draw(Graphics2D g2d, SIM_MODEL simulationModel, ENV_MODEL envModel) {
        draw(g2d, simulationModel, envModel, 0, 0, envModel.getHeight(), envModel.getWidth());
    }

    public void draw(Graphics2D g2d, SIM_MODEL simulationModel, ENV_MODEL envModel,
            int startRow, int startCol, int rowCount, int colCount) {

        int cellImageSize = imageProvider.getCellImageSize();

        // Draw the cells.
        for (int row = 0; row < envModel.getHeight(); row++) {
            if (row < startRow || row > startRow + rowCount) {
                continue;
            }
            for (int col = 0; col < envModel.getWidth(); col++) {
                if (col < startCol || col > startCol + colCount) {
                    continue;
                }
                Image cellBackground = imageProvider.getCellBackgroundImage(envModel, row, col);
                if (cellBackground != null) {
                    g2d.drawImage(cellBackground, col * cellSize, row * cellSize, cellSize, cellSize, null);
                }
            }
        }

        // Get entity z order.
        TreeMap<Integer, List<ENT_MODEL>> zOrderEntitiesMap = new TreeMap<>();
        for (ENT_MODEL entityModel : envModel.getEntities()) {
            int zOrder = imageProvider.getZOrder(entityModel);
            List<ENT_MODEL> entityModels = zOrderEntitiesMap.getOrDefault(zOrder, new ArrayList<>());
            entityModels.add(entityModel);
            zOrderEntitiesMap.put(zOrder, entityModels);
        }

        // Draw the entities.
        for (List<ENT_MODEL> entities : zOrderEntitiesMap.values()) {
            for (ENT_MODEL entityModel : entities) {
                if (entityModel.getY() < startRow || entityModel.getY() > startRow + rowCount ||
                        entityModel.getX() < startCol || entityModel.getX() > startCol + colCount) {
                    continue;
                }
                BufferedImage entityImage = imageProvider.getEntityImage(entityModel, envModel);
                if (entityImage != null) {
                    float xScaleFactor = (float) entityImage.getWidth() / cellImageSize;
                    float yScaleFactor = (float) entityImage.getHeight() / cellImageSize;
                    int renderWidth = (int) (xScaleFactor * cellSize);
                    int renderHeight = (int) (yScaleFactor * cellSize);
                    g2d.drawImage(entityImage,
                            entityModel.getX() * cellSize + (cellSize - renderWidth) / 2,
                            entityModel.getY() * cellSize + (cellSize - renderHeight) / 2,
                            renderWidth,
                            renderHeight, null);
                }
            }
        }
    }

    Dimension getPreferredSize(ENV_MODEL envModel) {
        return new Dimension(envModel.getWidth() * cellSize, envModel.getHeight() * cellSize);
    }
}

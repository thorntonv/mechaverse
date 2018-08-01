package org.mechaverse.simulation.common.ui;

import java.awt.image.BufferedImage;

import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.model.EnvironmentModel;

/**
 * Provides images to a {@link SimulationRenderer}.
 */
public interface SimulationImageProvider<
        ENV_MODEL extends EnvironmentModel<ENT_MODEL, ENT_TYPE>,
        ENT_MODEL extends EntityModel<ENT_TYPE>,
        ENT_TYPE extends Enum<ENT_TYPE>> {

    BufferedImage getCellBackgroundImage(ENV_MODEL environmentModel, int row, int column);

    BufferedImage getEntityImage(ENT_MODEL entityModel, ENV_MODEL environmentModel);

    /**
     * The current z-order for the entity. Entities with higher z-orders are drawn after those with lower z-orders.
     */
    int getZOrder(ENT_MODEL entityModel);

    /**
     * Returns the unscaled size of a cell image.
     */
    int getCellImageSize();
}

package org.mechaverse.gwt.client.environment;

import java.util.Map;

import org.mechaverse.gwt.client.AntSimulationResourceBundle;
import org.mechaverse.simulation.ant.core.model.EntityType;

import com.google.common.collect.Maps;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;

/**
 * A toolbar for selecting an entity.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class EntityToolbar extends HorizontalPanel {

  private static class EntityButton extends FocusPanel {

    public EntityButton(EntityType entityType) {
      addStyleName(AntSimulationResourceBundle.INSTANCE.css().entityButton());
      add(new Image(AntSimulationResourceBundle.ENTITY_IMAGES.get(entityType)));
    }

    public void setSelected(boolean selected) {
      if(selected) {
        addStyleName(AntSimulationResourceBundle.INSTANCE.css().entityButtonSelected());
      } else {
        removeStyleName(AntSimulationResourceBundle.INSTANCE.css().entityButtonSelected());
      }
    }
  }

  private EntityType selectedEntityType = EntityType.ROCK;
  private Map<EntityType, EntityButton> entityButtons = Maps.newHashMap();

  public EntityToolbar() {

    addStyleName(AntSimulationResourceBundle.INSTANCE.css().entityToolbar());

    for (final EntityType entityType : EntityType.values()) {
      if (entityType != EntityType.NONE) {
        EntityButton button = new EntityButton(entityType);
        button.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            setSelectedEntityType(entityType);
          }
        });

        entityButtons.put(entityType, button);
        add(button);
      }
    }
  }

  public EntityType getSelectedEntityType() {
    return selectedEntityType;
  }

  public void setSelectedEntityType(EntityType entityType) {
    this.selectedEntityType = entityType;

    for (Map.Entry<EntityType, EntityButton> entry : entityButtons.entrySet()) {
      entry.getValue().setSelected(entry.getKey() == entityType);
    }
  }
}

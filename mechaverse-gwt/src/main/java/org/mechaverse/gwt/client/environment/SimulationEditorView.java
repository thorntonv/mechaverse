package org.mechaverse.gwt.client.environment;

import java.util.Collection;
import java.util.Iterator;

import org.mechaverse.simulation.ant.api.model.Environment;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.Widget;

/**
 * A view for editing a simulation.
 *
 * @author thorntonv@mechaverse.org
 */
public class SimulationEditorView extends ResizeComposite {

  public static interface Observer {

    void onNew();
    void onSave();
    void onDelete();
    void onEnvironmentSelected(String environmentId);
  }

  interface MyUiBinder extends UiBinder<Widget, SimulationEditorView> {};
  private static final MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField EnvironmentDropDown environmentDropDown;
  @UiField Button newButton;
  @UiField Button saveButton;
  @UiField Button deleteButton;
  @UiField EnvironmentEditorView environmentEditorView;

  private Observer observer;

  public SimulationEditorView() {
    initWidget(uiBinder.createAndBindUi(this));

    environmentDropDown.addValueChangeHandler(new ValueChangeHandler<Environment>() {
      @Override
      public void onValueChange(ValueChangeEvent<Environment> event) {
        if (observer != null) {
          observer.onEnvironmentSelected(event.getValue().getId());
        }
      }
    });

    newButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (observer != null) {
          observer.onNew();
        }
      }
    });
    saveButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (observer != null) {
          observer.onSave();
        }
      }
    });
    deleteButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (observer != null) {
          observer.onDelete();
        }
      }
    });
  }

  public void setObserver(Observer observer) {
    this.observer = observer;
  }

  public EnvironmentEditorView getEnvironmentEditorView() {
    return environmentEditorView;
  }

  public void setAvailableEnvironments(Collection<Environment> environments) {
    Iterator<Environment> environmentIt = environments.iterator();
    if (environmentIt.hasNext()) {
      environmentDropDown.setValue(environmentIt.next());
      environmentDropDown.setAcceptableValues(environments);
    }
  }

  public void setEnvironment(Environment environment) {
    environmentEditorView.setEnvironment(environment);
  }

  public void setDeleteEnabled(boolean enabled) {
    deleteButton.setEnabled(enabled);
  }
}

package org.mechaverse.gwt.common.client.webconsole;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;

/**
 * A bundle which contains web console resources.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public interface WebConsoleResourceBundle extends ClientBundle {

  WebConsoleResourceBundle INSTANCE =
      GWT.create(WebConsoleResourceBundle.class);

  @Source("images/refresh.png")
  ImageResource refresh();

  interface Style extends CssResource {

    String actionButton();
    String menuLink();
    String notificationNoticeText();
    String notificationErrorText();
  }

  interface TableResources extends CellTable.Resources {

    TableResources INSTANCE = GWT.create(TableResources.class);

    interface TableStyle extends CellTable.Style {}

    @Override
    @Source("celltable.css")
    TableStyle cellTableStyle();
  }

  @Source("webconsole.css")
  Style css();
}

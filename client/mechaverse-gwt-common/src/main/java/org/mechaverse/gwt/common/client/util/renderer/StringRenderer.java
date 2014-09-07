package org.mechaverse.gwt.common.client.util.renderer;

import com.google.gwt.text.shared.AbstractRenderer;

/**
 * A renderer that renders a string.
 *
 * @author Vance Thornton
 */
public class StringRenderer extends AbstractRenderer<String> {

  public static final StringRenderer INSTANCE = new StringRenderer();

  @Override
  public String render(String str) {
    return str;
  }
}

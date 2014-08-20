package org.mechaverse.gwt.client.util.renderer;

import com.google.gwt.text.shared.AbstractRenderer;

/**
 * A renderer which renders a string.
 * 
 * @author thorntonv@mechaverse.org
 */
public class StringRenderer extends AbstractRenderer<String> {

  public static final StringRenderer INSTANCE = new StringRenderer();
  
  @Override
  public String render(String str) {
    return str;
  }
}

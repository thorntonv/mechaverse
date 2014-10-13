package org.mechaverse.gwt.common.client.util;

import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Image;

/**
 * Image utility methods.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class ImageUtil {

  public static ImageElement asImageElement(ImageResource imageResource) {
    return ImageElement.as((new Image(imageResource.getSafeUri())).getElement());
  }
}

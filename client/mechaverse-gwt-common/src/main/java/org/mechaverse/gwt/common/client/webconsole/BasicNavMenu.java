package org.mechaverse.gwt.common.client.webconsole;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Hyperlink;

/**
 * A basic navigation menu that consists of a list of links.
 *
 * @author Vance Thornton
 */
public class BasicNavMenu extends FlowPanel {

  public static class NavMenuLink {

    private String name;
    private Place place;

    public NavMenuLink(String name, Place place) {
      this.name = name;
      this.place = place;
    }

    public String getName() {
      return name;
    }

    public Place getPlace() {
      return place;
    }
  }

  public static class Builder {

    private final PlaceHistoryMapper placeHistoryMapper;
    private final List<NavMenuLink> links = new ArrayList<>();

    public Builder(PlaceHistoryMapper placeHistoryMapper) {
      this.placeHistoryMapper = placeHistoryMapper;
    }

    public Builder addLink(String name, Place place) {
      links.add(new NavMenuLink(name, place));
      return this;
    }

    public BasicNavMenu build() {
      return new BasicNavMenu(placeHistoryMapper, links);
    }
  }

  private final PlaceHistoryMapper placeHistoryMapper;

  public BasicNavMenu(PlaceHistoryMapper placeHistoryMapper, List<NavMenuLink> links) {
    this.placeHistoryMapper = placeHistoryMapper;
    for (NavMenuLink link : links) {
      add(createHyperlink(link.getName(), link.getPlace()));
    }
  }

  public static Builder newBuilder(PlaceHistoryMapper placeHistoryMapper) {
    return new Builder(placeHistoryMapper);
  }

  protected Hyperlink createHyperlink(String name, Place place) {
    Hyperlink link = new Hyperlink(name, placeHistoryMapper.getToken(place));
    link.setStyleName(WebConsoleResourceBundle.INSTANCE.css().menuLink());
    return link;
  }
}

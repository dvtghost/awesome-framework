package io.awesome.ui.components.navigation.drawer;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import elemental.json.JsonObject;
import io.awesome.ui.components.SelectDto;
import io.awesome.ui.util.UIUtil;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@CssImport("./styles/components/navi-drawer.css")
@JsModule("./swipe-away.js")
public class NaviDrawer extends Div implements AfterNavigationObserver {

  public static final String BRAND_EXPRESSION = "Awesome";
  private final String CLASS_NAME = "navi-drawer";
  private final String RAIL = "rail";
  private final String OPEN = "open";

  private Div scrim;

  private Div mainContent;

  @Getter private VerticalLayout searchResultWrapper;
  @Getter private ComboBox<SelectDto.SelectItem> search;
  @Getter private Button pinButton;

  private Button railButton;
  private NaviMenu menu;

  public NaviDrawer() {
    this(BRAND_EXPRESSION);
  }

  public NaviDrawer(String brandExpression) {
    setClassName(CLASS_NAME);

    initScrim();
    initMainContent();

    initHeader(brandExpression);
    initSearch();

    initMenu();

    initFooter();
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);
    UI ui = attachEvent.getUI();
    ui.getPage()
        .executeJavaScript(
            "window.addSwipeAway($0,$1,$2,$3)",
            mainContent.getElement(),
            this,
            "onSwipeAway",
            scrim.getElement());
  }

  @ClientCallable
  public void onSwipeAway(JsonObject data) {
    close();
  }

  private void initScrim() {
    // Backdrop on small viewports
    scrim = new Div();
    scrim.addClassName(CLASS_NAME + "__scrim");
    scrim.addClickListener(event -> close());
    add(scrim);
  }

  private void initMainContent() {
    mainContent = new Div();
    mainContent.addClassName(CLASS_NAME + "__content");
    add(mainContent);
  }

  private void initHeader(String brandExpression) {
    if (StringUtils.isBlank(brandExpression)) {
      brandExpression = BRAND_EXPRESSION;
    }
    mainContent.add(new BrandExpression(brandExpression));
  }

  private void initSearch() {
    searchResultWrapper = new VerticalLayout();
    searchResultWrapper.setWidthFull();
    searchResultWrapper.setPadding(false);
    searchResultWrapper.setVisible(false);
    searchResultWrapper.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
    searchResultWrapper.setAlignItems(FlexComponent.Alignment.CENTER);
    searchResultWrapper.getStyle().set("padding", "5px");

    search = new ComboBox<>();
    search.setPlaceholder("Search Applicant");
    search.setItemLabelGenerator(SelectDto.SelectItem::getLabel);
    search.getStyle().set("margin-left", "24px");

    pinButton = new Button("PIN");
    pinButton.getStyle().set("background", "#122b40");
    pinButton.getStyle().set("border-color", "#122b40");
    pinButton.getStyle().set("color", "white");
    pinButton.getStyle().set("margin-left", "5px");

    HorizontalLayout pinWrapper = new HorizontalLayout();
    pinWrapper.add(search, pinButton);

    VerticalLayout searchWrapper = new VerticalLayout();
    searchWrapper.add(pinWrapper, searchResultWrapper);
    searchWrapper.setWidthFull();
    searchWrapper.setPadding(false);
    searchWrapper.setVisible(false);
    mainContent.add(searchWrapper);
  }

  private void initMenu() {
    menu = new NaviMenu();
    mainContent.add(menu);
  }

  private void initFooter() {
    railButton = UIUtil.createSmallButton("Collapse", VaadinIcon.CHEVRON_LEFT_SMALL);
    railButton.addClassName(CLASS_NAME + "__footer");
    railButton.addClickListener(event -> toggleRailMode());
    railButton.getElement().setAttribute("aria-label", "Collapse menu");
    mainContent.add(railButton);
  }

  private void toggleRailMode() {
    if (getElement().hasAttribute(RAIL)) {
      getElement().setAttribute(RAIL, false);
      railButton.setIcon(new Icon(VaadinIcon.CHEVRON_LEFT_SMALL));
      railButton.setText("Collapse");
      UIUtil.setAriaLabel("Collapse menu", railButton);

    } else {
      getElement().setAttribute(RAIL, true);
      railButton.setIcon(new Icon(VaadinIcon.CHEVRON_RIGHT_SMALL));
      railButton.setText("Expand");
      UIUtil.setAriaLabel("Expand menu", railButton);
      getUI()
          .get()
          .getPage()
          .executeJavaScript(
              "var originalStyle = getComputedStyle($0).pointerEvents;" //
                  + "$0.style.pointerEvents='none';" //
                  + "setTimeout(function() {$0.style.pointerEvents=originalStyle;}, 170);",
              getElement());
    }
  }

  public void toggle() {
    if (getElement().hasAttribute(OPEN)) {
      close();
    } else {
      open();
    }
  }

  private void open() {
    getElement().setAttribute(OPEN, true);
  }

  private void close() {
    getElement().setAttribute(OPEN, false);
    // Thai: This will cause the page crash sometimes, thus remove it for now
    // applyIOS122Workaround();
  }

  private void applyIOS122Workaround() {
    // iOS 12.2 sometimes fails to animate the menu away.
    // It should be gone after 240ms
    // This will make sure it disappears even when the browser fails.
    getUI()
        .get()
        .getPage()
        .executeJavaScript(
            "var originalStyle = getComputedStyle($0).transitionProperty;" //
                + "setTimeout(function() {$0.style.transitionProperty='padding'; requestAnimationFrame(function() {$0.style.transitionProperty=originalStyle})}, 250);",
            mainContent.getElement());
  }

  public NaviMenu getMenu() {
    return menu;
  }

  @Override
  public void afterNavigation(AfterNavigationEvent afterNavigationEvent) {
    close();
  }
}

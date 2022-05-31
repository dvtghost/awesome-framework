package io.awesome.ui.components.detailsdrawer;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.shared.Registration;
import io.awesome.ui.components.FlexBoxLayout;
import io.awesome.ui.layout.size.Horizontal;
import io.awesome.ui.layout.size.Right;
import io.awesome.ui.layout.size.Vertical;
import io.awesome.ui.util.LumoStyles;
import io.awesome.ui.util.UIUtil;

public class DetailsDrawerFooter extends FlexBoxLayout {

  private final Button save;
  private final Button cancel;

  public DetailsDrawerFooter() {
    setBackgroundColor(LumoStyles.Color.Contrast._5);
    setPadding(Horizontal.RESPONSIVE_L, Vertical.S);
    setSpacing(Right.S);
    setWidthFull();

    save = UIUtil.createPrimaryButton("Save");
    cancel = UIUtil.createTertiaryButton("Cancel");
    add(save, cancel);
  }

  public Registration addSaveListener(ComponentEventListener<ClickEvent<Button>> listener) {
    return save.addClickListener(listener);
  }

  public Registration addCancelListener(ComponentEventListener<ClickEvent<Button>> listener) {
    return cancel.addClickListener(listener);
  }
}

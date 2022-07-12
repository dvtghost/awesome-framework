package io.awesome.ui.components.grid.column;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

public class Checkbox extends Div {

    public Checkbox(Boolean value) {
        this.setWidthFull();
        this.add(buildIcon(value));
    }

    private Icon buildIcon(Boolean value) {
        Icon icon;
        if (value.equals(Boolean.FALSE)) {
            icon = new Icon(VaadinIcon.CLOSE_CIRCLE);
            icon.setColor("red");
        } else {
            icon = new Icon(VaadinIcon.CHEVRON_CIRCLE_DOWN);
            icon.setColor("green");
        }
        icon.setSize("15px");
        return icon;
    }
}

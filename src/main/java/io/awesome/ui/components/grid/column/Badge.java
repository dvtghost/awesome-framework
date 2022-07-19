package io.awesome.ui.components.grid.column;

import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import io.awesome.enums.IAttributeEnum;
import io.awesome.ui.components.FlexBoxLayout;
import io.awesome.ui.util.UIUtil;
import org.apache.commons.lang.StringUtils;

public class Badge extends FlexBoxLayout {

    public <X, T> Badge(IAttributeEnum<X, T> attributeEnum) {
        super();
        this.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        this.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        this.add(buildBadge(attributeEnum));
    }

    private <X, T> io.awesome.ui.components.Badge buildBadge(IAttributeEnum<X, T> attributeEnum) {
        io.awesome.ui.components.Badge badge;
        if (StringUtils.isNotBlank(attributeEnum.getLabel())) {
            badge = new io.awesome.ui.components.Badge(attributeEnum.getLabel());
        } else {
            badge = new io.awesome.ui.components.Badge(attributeEnum.getName());
        }

        if (StringUtils.isNotBlank(attributeEnum.getColor())) {
            UIUtil.addTheme(attributeEnum.getColor(), badge);
        }
        badge.getStyle().set("padding", "5px");

        UIUtil.setTooltip(attributeEnum.getLabel(), badge);
        return badge;
    }
}


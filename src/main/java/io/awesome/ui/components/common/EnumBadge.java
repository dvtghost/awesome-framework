package io.awesome.ui.components.common;

import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import io.awesome.enums.IAttributeEnum;
import io.awesome.ui.components.Badge;
import io.awesome.ui.components.FlexBoxLayout;
import io.awesome.ui.util.UIUtil;
import org.apache.commons.lang.StringUtils;

public class EnumBadge extends FlexBoxLayout {

    public <X, T> EnumBadge(IAttributeEnum<X, T> attributeEnum) {
        super();
        this.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        this.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        this.add(buildBadge(attributeEnum));
    }

    private <X, T> Badge buildBadge(IAttributeEnum<X, T> attributeEnum) {
        Badge badge;
        if (StringUtils.isNotBlank(attributeEnum.getLabel())) {
            badge = new Badge(attributeEnum.getLabel());
        } else {
            badge = new Badge(attributeEnum.getName());
        }

        if (StringUtils.isNotBlank(attributeEnum.getColor())) {
            UIUtil.addTheme(attributeEnum.getColor(), badge);
        }
        badge.getStyle().set("padding", "5px");

        UIUtil.setTooltip(attributeEnum.getLabel(), badge);
        return badge;
    }
}


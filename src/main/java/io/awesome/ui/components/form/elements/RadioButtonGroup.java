package io.awesome.ui.components.form.elements;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.function.ValueProvider;
import io.awesome.ui.annotations.FormElement;
import io.awesome.ui.binder.ExtendedBinder;
import io.awesome.ui.components.SelectDto;
import io.awesome.ui.util.UIUtil;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RadioButtonGroup<T> extends AbstractFormElement<T, SelectDto.SelectItem> {
    public RadioButtonGroup(FormLayout parentForm, ExtendedBinder<T> binder, T entity, Map<String, FormLayout.FormItem> items) {
        super(parentForm, binder, entity, items);
    }

    @Override
    public Optional<Binder.Binding<T, SelectDto.SelectItem>> binding(FormElement annotation, String fieldName) {
        try {
            com.vaadin.flow.component.radiobutton.RadioButtonGroup<SelectDto.SelectItem> select = new com.vaadin.flow.component.radiobutton.RadioButtonGroup<>();
            select.setRequired(annotation.required());
            select.setEnabled(annotation.enable());
            if (annotation.isRadioButtonGroupVertical()) {
                select.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
            }
            SelectDto dto = (SelectDto) util.invokeGetter(entity, fieldName);
            select.setItems(dto.getItems());
            HorizontalLayout wrapper = new HorizontalLayout();
            wrapper.add(select);
            FormLayout.FormItem selectItem = this.parentForm.addFormItem(wrapper, annotation.label());
            UIUtil.setColSpan(annotation.colspan(), selectItem);
            items.put(fieldName, selectItem);

            select.setRenderer(
                    new ComponentRenderer<>(
                            i -> {
                                Span component = new Span();
                                component.setText(i.getLabel());
                                if (!StringUtils.isEmpty(i.getExtra())) {
                                    component.getStyle().set("color", i.getExtra());
                                }
                                return component;
                            }));
            return Optional.of(binder
                    .forField(select)
                    .bind(
                            (ValueProvider<T, SelectDto.SelectItem>)
                                    t -> {
                                        SelectDto selectDto = (SelectDto) util.invokeGetter(entity, fieldName);
                                        List<SelectDto.SelectItem> selected = selectDto.getSelected();
                                        if (selected != null && !selected.isEmpty()) return selected.get(0);
                                        return null;
                                    },
                            (com.vaadin.flow.data.binder.Setter<T, SelectDto.SelectItem>)
                                    (t, a) -> {
                                        SelectDto selectDto = (SelectDto) util.invokeGetter(entity, fieldName);
                                        List<SelectDto.SelectItem> newSelected = new ArrayList<>();
                                        newSelected.add(a);
                                        selectDto.setSelected(newSelected);
                                    }));
        } catch (Exception e) {
            logError(entity, annotation, fieldName, e);
        }
        return Optional.empty();
    }
}

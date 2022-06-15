package io.awesome.ui.components.form.elements;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Setter;
import com.vaadin.flow.function.ValueProvider;
import io.awesome.ui.annotations.FormElement;
import io.awesome.ui.binder.ExtendedBinder;
import io.awesome.ui.components.SelectDto;
import io.awesome.ui.util.UIUtil;

import java.util.*;

public class CheckboxGroup<T> extends  AbstractFormElement<T, Set<SelectDto.SelectItem>> {
    public CheckboxGroup(FormLayout parentForm, ExtendedBinder<T> binder, T entity, Map<String, FormLayout.FormItem> items) {
        super(parentForm, binder, entity, items);
    }

    @Override
    public Optional<Binder.Binding<T, Set<SelectDto.SelectItem>>> binding(FormElement annotation, String fieldName) {
        try {
            com.vaadin.flow.component.checkbox.CheckboxGroup<SelectDto.SelectItem> checkboxGroup = new com.vaadin.flow.component.checkbox.CheckboxGroup<>();
            checkboxGroup.setRequired(annotation.required());
            checkboxGroup.setEnabled(annotation.enable());
            SelectDto dto = (SelectDto) util.invokeGetter(entity, fieldName);
            checkboxGroup.setItems(dto.getItems());
            HorizontalLayout wrapper = new HorizontalLayout();
            wrapper.add(checkboxGroup);
            checkboxGroup.setItemLabelGenerator(SelectDto.SelectItem::getLabel);
            FormLayout.FormItem checkboxGroupItem = this.parentForm.addFormItem(wrapper, annotation.label());
            UIUtil.setColSpan(annotation.colspan(), checkboxGroupItem);
            items.put(fieldName, checkboxGroupItem);
            return Optional.of(binder
                    .forField(checkboxGroup)
                    .bind(
                            (ValueProvider<T, Set<SelectDto.SelectItem>>)
                                    t -> {
                                        List<SelectDto.SelectItem> dtoSelected = dto.getSelected();
                                        if (dtoSelected != null && !dtoSelected.isEmpty())
                                            return new HashSet<>(dtoSelected);
                                        return null;
                                    },
                            (com.vaadin.flow.data.binder.Setter<T, Set<SelectDto.SelectItem>>)
                                    (t, a) -> {
                                        List<SelectDto.SelectItem> newSelected = new ArrayList<>(a);
                                        dto.setSelected(newSelected);
                                    }));
        } catch (Exception e) {
            logError(entity, annotation, fieldName, e);
        }
        return Optional.empty();
    }

}

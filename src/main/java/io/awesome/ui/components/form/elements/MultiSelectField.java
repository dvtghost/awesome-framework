package io.awesome.ui.components.form.elements;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Setter;
import com.vaadin.flow.function.ValueProvider;
import io.awesome.ui.annotations.FormElement;
import io.awesome.ui.binder.ExtendedBinder;
import io.awesome.ui.components.SelectDto;
import io.awesome.ui.util.UIUtil;
import org.vaadin.gatanaso.MultiselectComboBox;

import java.util.*;

public class MultiSelectField<T> extends  AbstractHasValueFormElement<T, Set<SelectDto.SelectItem>> {
    public MultiSelectField(FormLayout parentForm, ExtendedBinder<T> binder, T entity, Map<String, FormLayout.FormItem> items) {
        super(parentForm, binder, entity, items);
    }

//    @Override
//    public Optional<Binder.Binding<T, Set<SelectDto.SelectItem>>> binding(FormElement annotation, String fieldName) {
//        try {
//            MultiselectComboBox<SelectDto.SelectItem> select = new MultiselectComboBox<>();
//            select.setWidthFull();
//            select.setRequired(annotation.required());
//            select.setEnabled(annotation.enable());
//            select.setClearButtonVisible(annotation.isClearButton());
//            SelectDto dto = (SelectDto) util.invokeGetter(entity, fieldName);
//            select.setItems(dto.getItems());
//            select.setItemLabelGenerator(SelectDto.SelectItem::getLabel);
//
//
//            this.parentForm.add(selectItem);
//            UIUtil.setColSpan(annotation.colspan(), selectItem);
//            items.put(fieldName, selectItem);
//            return Optional.of(binder
//                    .forField(select)
//                    .bind(
//                            (ValueProvider<T, Set<SelectDto.SelectItem>>)
//                                    t -> {
//                                        List<SelectDto.SelectItem> dtoSelected = dto.getSelected();
//                                        if (dtoSelected != null && !dtoSelected.isEmpty())
//                                            return new HashSet<>(dtoSelected);
//                                        return null;
//                                    },
//                            (com.vaadin.flow.data.binder.Setter<T, Set<SelectDto.SelectItem>>)
//                                    (t, a) -> {
//                                        List<SelectDto.SelectItem> newSelected = new ArrayList<>(a);
//                                        dto.setSelected(newSelected);
//                                    }));
//
//        } catch (Exception e) {
//            logError(entity, annotation, fieldName, e);
//        }
//        return Optional.empty();
//    }

    @Override
    protected HasValue<?, Set<SelectDto.SelectItem>> buildField(FormElement annotation, String fieldName) {
        MultiselectComboBox<SelectDto.SelectItem> select = new MultiselectComboBox<>();
        select.setWidthFull();
        select.setRequired(annotation.required());
        select.setEnabled(annotation.enable());
        select.setClearButtonVisible(annotation.isClearButton());
        SelectDto dto = (SelectDto) util.invokeGetter(entity, fieldName);
        select.setItems(dto.getItems());
        select.setItemLabelGenerator(SelectDto.SelectItem::getLabel);
        return select;
    }

    @Override
    protected FormLayout.FormItem buildFormItem(FormElement annotation, Component field) {
        FormLayout.FormItem selectItem =
                new FormLayout.FormItem() {
                    @Override
                    public void setEnabled(boolean enabled) {
                        this.getElement().setAttribute("disabled", !enabled);
                        this.getElement()
                                .getChildren()
                                .forEach(
                                        child -> {
                                            child.setAttribute("disabled", !enabled);
                                        });
                    }
                };
        Label label = new Label(annotation.label());
        selectItem.add(label, field);

        label.addClassName("item-label");
        selectItem.addClassName("multiselect-item");
        return selectItem;
    }

    @Override
    protected ValueProvider<T, Set<SelectDto.SelectItem>> bindingValueProvider(String fieldName) {
        return t -> {
            List<SelectDto.SelectItem> dtoSelected = ((SelectDto) util.invokeGetter(entity, fieldName)).getSelected();
            if (dtoSelected != null && !dtoSelected.isEmpty())
                return new HashSet<>(dtoSelected);
            return null;
        };
    }

    @Override
    protected Setter<T, Set<SelectDto.SelectItem>> bindingSetter(String fieldName) {
        return (t, v) -> {
            List<SelectDto.SelectItem> newSelected = new ArrayList<>(v);
            ((SelectDto) util.invokeGetter(entity, fieldName)).setSelected(newSelected);
        };
    }
}

package io.awesome.ui.components.form.elements;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.function.ValueProvider;
import io.awesome.ui.annotations.FormElement;
import io.awesome.ui.binder.ExtendedBinder;
import io.awesome.ui.components.SelectDto;
import io.awesome.ui.util.UIUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class SelectField<T> extends AbstractFormElement<T, SelectDto.SelectItem> {

    public SelectField(FormLayout parentForm, ExtendedBinder<T> binder, T entity, Map<String, FormLayout.FormItem> items) {
        super(parentForm, binder, entity, items);
    }

    @Override
    public Optional<Binder.Binding<T, SelectDto.SelectItem>> binding(FormElement annotation, String fieldName) {
        try {
            ComboBox<SelectDto.SelectItem> select = new ComboBox<>();
            select.setWidthFull();
            select.setRequired(annotation.required());
            select.setEnabled(annotation.enable());
            select.setClearButtonVisible(annotation.isClearButton());
            SelectDto dto = (SelectDto) this.util.invokeGetter(this.entity, fieldName);
            select.setItems(dto.getItems());
            select.setItemLabelGenerator(SelectDto.SelectItem::getLabel);
            FormLayout.FormItem selectItem = this.parentForm.addFormItem(select, annotation.label());
            UIUtil.setColSpan(annotation.colspan(), selectItem);
            this.items.put(fieldName, selectItem);
            return Optional.of(this.binder
                    .forField(select)
                    .bind(
                            (ValueProvider<T, SelectDto.SelectItem>)
                                    t -> {
                                        List<SelectDto.SelectItem> selected = dto.getSelected();
                                        if (selected != null && !selected.isEmpty()) return selected.get(0);
                                        return null;
                                    },
                            (com.vaadin.flow.data.binder.Setter<T, SelectDto.SelectItem>)
                                    (t, a) -> {
                                        List<SelectDto.SelectItem> newSelected = new ArrayList<>();
                                        newSelected.add(a);
                                        dto.setSelected(newSelected);
                                    }));
        } catch (Exception e) {
            logError(this.entity, annotation, fieldName, e);
        }
        return Optional.empty();
    }


}

package io.awesome.ui.binder;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.data.binder.Binder;
import io.awesome.ui.enums.FormElementType;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.vaadin.gatanaso.MultiselectComboBox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@Slf4j
public class ExtendedBinder<BEAN> extends Binder<BEAN> {
  private Map<String, FormElementType> fieldTypeMaps = new HashMap<>();
  private Map<String, Binding<BEAN, ?>> bindingsMap = new HashMap<>();

  public <FIELDVALUE> void addBinding(
      String fieldName, FormElementType type, Binding<BEAN, FIELDVALUE> binding) {
    this.fieldTypeMaps.put(fieldName, type);
    this.bindingsMap.put(fieldName, binding);
  }

  public <FIELDVALUE> void updateBinding(List<String> fieldNames, BEAN entity) {
    fieldNames.stream()
        .filter(fieldName -> !fieldName.equalsIgnoreCase("generalErrorField"))
        .forEach(
            fieldName -> {
              FormElementType type = fieldTypeMaps.get(fieldName);
              Binding<BEAN, ?> beanBinding = bindingsMap.get(fieldName);
              if (type == null || beanBinding == null) {
                throw new RuntimeException(
                    String.format("%s not yet in the extended binding map", fieldName));
              }
              if (type.equals(FormElementType.SelectField)) {
                FIELDVALUE newValue = (FIELDVALUE) beanBinding.getGetter().apply(entity);
                ComboBox<FIELDVALUE> field = (ComboBox<FIELDVALUE>) beanBinding.getField();
                if (field == null) {
                  return;
                }
                if (newValue == null) {
                  field.clear();
                  return;
                }
                if (newValue.equals(field.getValue())) {
                  return;
                }
                field.setValue(newValue);
              } else if (type.equals(FormElementType.MultiSelectField)) {
                Set<FIELDVALUE> newValue = (Set<FIELDVALUE>) beanBinding.getGetter().apply(entity);
                MultiselectComboBox<FIELDVALUE> field =
                    (MultiselectComboBox<FIELDVALUE>) beanBinding.getField();
                if (field == null) {
                  return;
                }
                if (CollectionUtils.isEmpty(newValue)) {
                  field.clear();
                  return;
                }
                field.setValue(newValue);
              } else if (type.equals(FormElementType.RadioButtonGroup)) {
                FIELDVALUE newValue = (FIELDVALUE) beanBinding.getGetter().apply(entity);
                RadioButtonGroup<FIELDVALUE> field =
                    (RadioButtonGroup<FIELDVALUE>) beanBinding.getField();
                if (field == null) {
                  return;
                }
                if (newValue == null) {
                  field.clear();
                  return;
                }
                if (newValue.equals(field.getValue())) {
                  return;
                }
                field.setValue(newValue);
              } else {
                FIELDVALUE newValue = (FIELDVALUE) beanBinding.getGetter().apply(entity);
                ((HasValue<?, FIELDVALUE>) beanBinding.getField()).setValue(newValue);
              }
            });
  }
}

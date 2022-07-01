package io.awesome.ui.components.form;


import io.awesome.ui.components.form.elements.*;
import io.awesome.ui.enums.FormElementType;

public enum FormElementMapping {
    Widget(FormElementType.Widget, WidgetField.class),
    TextField(FormElementType.TextField, TextField.class),
    DateField(FormElementType.DateField, DateField.class),
    RadioButtonGroup(FormElementType.RadioButtonGroup, RadioButtonGroup.class),
    DateTimeField(FormElementType.DateTimeField, DateTimeField.class),
    DateRangePicker(FormElementType.DateRangePicker, DateField.class),
    TimeField(FormElementType.TimeField, TimeField.class),
    SelectField(FormElementType.SelectField, SelectField.class),
    MultiSelectField(FormElementType.MultiSelectField, MultiSelectField.class),
    PhoneField(FormElementType.PhoneField, PhoneField.class),
    PasswordField(FormElementType.PasswordField, TextField.class),
    TextAreaField(FormElementType.TextAreaField, TextField.class),
    IntegerField(FormElementType.IntegerField, IntegerField.class),
    EmailField(FormElementType.EmailField, TextField.class),
    FileField(FormElementType.FileField, FileField.class),
    Checkbox(FormElementType.Checkbox, Checkbox.class),
    DoubleField(FormElementType.DoubleField, DoubleField.class),
    LongField(FormElementType.LongField, LongField.class),
    SignatureField(FormElementType.SignatureField, SignatureField.class),
    CheckboxGroup(FormElementType.CheckboxGroup, CheckboxGroup.class),
    NumberRangePicker(FormElementType.NumberRangePicker, TextField.class);

    private FormElementType type;
    private Class<? extends AbstractFormElement> formElementClazz;

    FormElementMapping(FormElementType type, Class<? extends AbstractFormElement> formElementClazz) {
        this.type = type;
        this.formElementClazz = formElementClazz;
    }

    public static FormElementMapping retrieveByFormElementType(FormElementType type) {
        for (FormElementMapping mapping : FormElementMapping.values()) {
            if (mapping.type == type) {
                return mapping;
            }
        }
        return null;
    }

    public Class<? extends AbstractFormElement> getFormElementClazz() {
        return formElementClazz;
    }
}

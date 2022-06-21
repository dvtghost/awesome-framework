package io.awesome.ui.views;

import com.vaadin.flow.component.Component;
import io.awesome.ui.annotations.ValueChangeHandler;
import io.awesome.ui.annotations.ValueInitHandler;
import io.awesome.ui.binder.ExtendedBinder;
import io.awesome.ui.components.AbstractForm;
import io.awesome.ui.components.FormControl;
import io.awesome.ui.components.button.ActionButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class EditorView<E> {
    private final Logger logger = LoggerFactory.getLogger(EditorView.class);
    private Class<E> editEntityClazz;
    private List<ActionButton> actionButtons;
    private Map<String, Consumer<E>> actionButtonConsumers;
    private Map<String, Runnable> actionButtonRunnable;
    private ValueChangeHandler<E> onChange;
    private ValueInitHandler<E> onInit;


    private EditorView(Class<E> editEntityClazz) {
        this.editEntityClazz = editEntityClazz;
    }


    public Component create(E edit, boolean isAbleToEdit) {
        ExtendedBinder<E> binder = new ExtendedBinder<>();
        binder.setBean(edit);
        AbstractForm<E> form =
                new AbstractForm<>(
                        editEntityClazz,
                        edit,
                        binder,
                        isAbleToEdit,
                        onChange,
                        onInit,
                        "");
        FormControl formControl =
                new FormControl(form)
                        .addActionBtns(this.actionButtons.toArray(new ActionButton[0]));
        this.actionButtonConsumers.forEach((k, v) -> {
            formControl
                    .getActionButton(k)
                    .ifPresent(
                            btn ->
                                    btn.addActionHandler(
                                            entity -> {
                                                E editEntity = (E) entity;
                                                v.accept(editEntity);
                                            },
                                            form,
                                            logger));
        });
        this.actionButtonRunnable.forEach((k, v) -> {
            formControl
                    .getActionButton(k)
                    .ifPresent(
                            btn ->
                                    btn.addActionHandler(
                                            entity -> v.run(),
                                            form,
                                            logger));
        });

        formControl.showActionButtons("50%", actionButtons.stream().map(ActionButton::getType).toArray(String[]::new));
        form.setFormControl(formControl);
        return form;
    }

    public static class EditorViewBuilder<E> {
        private final Class<E> editEntityClazz;
        private final List<ActionButton> actionButtons;
        private final Map<String, Consumer<E>> actionButtonConsumers;
        private final Map<String, Runnable> actionButtonRunnable;
        private ValueChangeHandler<E> onChange;
        private ValueInitHandler<E> onInit;

        public EditorViewBuilder(Class<E> editEntityClazz) {
            this.editEntityClazz = editEntityClazz;
            this.actionButtons = new ArrayList<>();
            this.actionButtonConsumers = new HashMap<>();
            this.actionButtonRunnable = new HashMap<>();
        }

        public EditorViewBuilder<E> onChange(ValueChangeHandler<E> onChange) {
            this.onChange = onChange;
            return this;
        }

        public EditorViewBuilder<E> onInit(ValueInitHandler<E> onInit) {
            this.onInit = onInit;
            return this;
        }

        public ActionButtonBuilder<E> button(ActionButton actionButton) {
            ActionButtonBuilder<E> actionButtonBuilder = new ActionButtonBuilder<>(this);
            actionButtonBuilder.setActionButton(actionButton);
            this.actionButtons.add(actionButton);
            return actionButtonBuilder;
        }

        public EditorView<E> build() {
            EditorView<E> editorView = new EditorView<>(editEntityClazz);
            editorView.actionButtons = this.actionButtons;
            editorView.actionButtonConsumers = this.actionButtonConsumers;
            editorView.actionButtonRunnable = this.actionButtonRunnable;
            editorView.onChange = this.onChange;
            editorView.onInit = this.onInit;
            return  editorView;
        }


        public static class ActionButtonBuilder<E>{
            private final EditorViewBuilder<E> editorViewBuilder;
            private ActionButton actionButton;

            public ActionButtonBuilder(EditorViewBuilder<E> editorViewBuilder) {
                this.editorViewBuilder = editorViewBuilder;
            }

            public void setActionButton(ActionButton actionButton) {
                this.actionButton = actionButton;
            }

            public EditorViewBuilder<E> action(Consumer<E> action) {
                this.editorViewBuilder.actionButtonConsumers.put(this.actionButton.getType(), action);
                return this.editorViewBuilder;
            }

            public EditorViewBuilder<E> action(Runnable action) {
                this.editorViewBuilder.actionButtonRunnable.put(this.actionButton.getType(), action);
                return this.editorViewBuilder;
            }
        }
    }


}

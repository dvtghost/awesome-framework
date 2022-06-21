package io.awesome.ui.components;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Shortcuts;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@CssImport("styles/components/filter-control.css")
@Getter
@Setter
public class FilterControl<F> extends HorizontalLayout {

  public static final String SAVE_AS_NEW_FILTER = "Save as New Filter";
  private ComboBox<String> filterComboBox;
  private Button loadFilter;
  private Button removeFilter;
  private Button searchButton;
  private Button resetButton;

  private ActionWithString<F> onFilterActionWithString;
  private ActionWithString<F> onFilterRemove;
  private Action<F> onFilterSearch;
  private Action<F> onFilterReset;
  private Save<F> onFilterSave;

  private F filterEntity;
  private List<String> savedFilters = new ArrayList<>();

  public FilterControl(F filterEntity, Action<F> onSearch, Action<F> onFilterReset) {
    this.filterEntity = filterEntity;
    this.onFilterSearch = onSearch;
    this.onFilterReset = onFilterReset;

    buildSearchBtn();

    buildResetBtn();

    add(searchButton, resetButton);
  }

  public FilterControl(
      F filterEntity,
      boolean hasSaveFilters,
      Set<String> savedFilters,
      Save<F> onSaveFilter,
      ActionWithString<F> onLoadFilter,
      ActionWithString<F> onRemoveFilter,
      Action<F> onSearch,
      Action<F> onFilterReset) {
    addClassNames("filter-control-layout");
    this.filterEntity = filterEntity;
    this.savedFilters = new ArrayList<>();
    this.savedFilters.add(SAVE_AS_NEW_FILTER);
    this.savedFilters.addAll(savedFilters);
    this.onFilterActionWithString = onLoadFilter;
    this.onFilterRemove = onRemoveFilter;
    this.onFilterSearch = onSearch;
    this.onFilterReset = onFilterReset;
    this.onFilterSave = onSaveFilter;

    buildSaveFilterBox(savedFilters);

    buildLoadBtn();

    buildRemoveBtn();

    buildSearchBtn();

    buildResetBtn();

    if (!hasSaveFilters) {
      add(searchButton, resetButton);
      addClassName("text-field-custom");
      addClassName("filter-control-compact");
    } else {
      add(filterComboBox, loadFilter, removeFilter, searchButton, resetButton);
    }
  }

  private void buildResetBtn() {
    resetButton = createButton(null, "Reset", new String[] {"blue-button", "reset-button"});
    resetButton.addClickListener(
        event -> {
          this.onFilterReset.execute();
          this.onFilterSearch.execute();
        });
  }

  private void buildSearchBtn() {
    searchButton =
        createButton(
            new Icon(VaadinIcon.SEARCH), "Search", new String[] {"green-button", "search-button"});
    searchButton.addClickListener(e -> onFilterSearch.execute());
  }

  private void buildRemoveBtn() {
    removeFilter =
        createButton(null, "Remove Filter", new String[] {"red-button", "remove-button"});
    removeFilter.addClickListener(
        e -> {
          onFilterRemove.execute(this.filterComboBox.getValue());
          this.savedFilters =
              this.savedFilters.stream()
                  .filter(n -> !n.equalsIgnoreCase(this.filterComboBox.getValue()))
                  .collect(Collectors.toList());
          buildItemList(this.savedFilters);
        });
  }

  private void buildLoadBtn() {
    loadFilter =
        createButton(
            new Icon(VaadinIcon.FILTER),
            "Load Filter",
            new String[] {"blue-button", "filter-button"});
    loadFilter.addClickListener(
        event -> {
          onFilterActionWithString.execute(this.filterComboBox.getValue());
          this.onFilterSearch.execute();
        });
  }

  private void buildSaveFilterBox(Set<String> savedFilters) {
    filterComboBox = new ComboBox<>();
    filterComboBox.setLabel("Select Filter");
    buildItemList(new ArrayList<>(savedFilters));
    filterComboBox.setClassName("text-field-custom");
    filterComboBox.setWidth("185px");
    filterComboBox.addValueChangeListener(
        event -> {
          if (event.getValue() == null || !event.getValue().equalsIgnoreCase(SAVE_AS_NEW_FILTER)) {
            return;
          }
          TextField textField = new TextField();
          Dialog dialog = new Dialog();
          dialog.add(new Text("Please enter the filter name!"));
          dialog.add(textField);
          Shortcuts.addShortcutListener(
              dialog,
              () -> {
                if (!StringUtils.isBlank(textField.getValue())) {
                  dialog.close();
                }
              },
              Key.ENTER);
          Button button =
              new Button(
                  "Save",
                  e -> {
                    dialog.close();
                    Pair<String, F> pair =
                        this.onFilterSave.execute(textField.getValue(), this.filterEntity);
                    this.savedFilters.add(pair.getLeft());
                    buildItemList(new ArrayList<>(this.savedFilters));
                    this.getFilterComboBox().setValue(null);
                  });
          dialog.add(button);
          dialog.open();
        });
  }

  private void buildItemList(List<String> savedFilters) {
    List<String> itemLists = new ArrayList<>();
    if (!savedFilters.contains(SAVE_AS_NEW_FILTER)) {
      itemLists.add(SAVE_AS_NEW_FILTER);
    }
    itemLists.addAll(savedFilters);
    filterComboBox.setItems(itemLists);
  }

  public Button createButton(Icon icon, String title, String[] classNames) {
    Button button = new Button();
    if (icon != null) {
      button.setIcon(icon);
    }
    button.setText(title);
    button.addClassName("control-button");
    button.addClassNames(classNames);
    return button;
  }

  public interface Action<E> {
    E execute();
  }

  public interface ActionWithString<E> {
    E execute(String value);
  }

  public interface ChildComponentAction<E> {
    E execute(E entity, FilterControl<E> parent);
  }

  public interface Save<E> {
    Pair<String, E> execute(String name, E entity);
  }
}

package io.awesome.ui.components;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SelectDto {
  private List<SelectItem> items = new ArrayList<>();
  private List<SelectItem> selected = new ArrayList<>();
  private Comparator<SelectDto.SelectItem> comparator = Comparator.comparing(SelectItem::getLabel);

  public SelectDto(List<SelectItem> items, List<SelectItem> selected) {
    this.items = items;
    this.selected = selected;
  }

  public boolean hasSelectedItems() {
    return !CollectionUtils.isEmpty(selected) && selected.get(0) != null;
  }

  public Set<String> getSelectedLabel() {
    return !CollectionUtils.isEmpty(this.getSelected())
        ? this.getSelected().stream().map(SelectItem::getLabel).collect(Collectors.toSet())
        : new HashSet<>();
  }

  public List<SelectItem> getItems() {
    if (items != null && !items.isEmpty()) items.sort(comparator);
    return items;
  }

  @NoArgsConstructor
  @Getter
  @Setter
  public static class SelectItem {
    private Object value;
    private String name;
    private String label;
    private String extra;

    public SelectItem(Object value, String label) {
      this.value = value;
      this.label = label;
    }

    public SelectItem(Object value, String name, String label) {
      this.value = value;
      this.label = label;
      this.name = name;
    }

    public SelectItem(Object value, String name, String label, String extra) {
      this.value = value;
      this.name = name;
      this.label = label;
      this.extra = extra;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      SelectItem that = (SelectItem) o;
      return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
      return Objects.hash(value);
    }
  }
}

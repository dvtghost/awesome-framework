package io.awesome.ui.components.pagination;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@CssImport("styles/components/pagination.css")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Pagination extends VerticalLayout {

  final List<PaginationChangeListener> listeners = new ArrayList<>();
  Button firstButton;
  Button lastButton;
  Button previousButton;
  Button nextButton;
  HorizontalLayout buttonsPageNumberLayout;
  private PaginationResource resource;

  public int getPage() {
    return resource.getPage();
  }

  public int getLimit() {
    return resource.getLimit();
  }

  public boolean hasNext() {
    return resource.isHasNext();
  }

  public Pagination(PaginationResource resource) {
    this.resource = resource;

    firstButton = new Button("First");
    firstButton.addClassNames("page-button");

    lastButton = new Button("Last");
    lastButton.addClassNames("page-button");

    previousButton = new Button("Prev");
    previousButton.addClassNames("page-button");

    nextButton = new Button("Next");
    nextButton.addClassNames("page-button");

    buttonsPageNumberLayout = new HorizontalLayout();
    buttonsPageNumberLayout.setJustifyContentMode(JustifyContentMode.END);
    buttonsPageNumberLayout.setWidthFull();

    addPaginationButtons();

    getStyle().set("margin-top", "0");
    getStyle().set("padding-bottom", "0");
    add(buttonsPageNumberLayout);
    buttonClick();
  }

  public void addPaginationButtons() {
    buttonsPageNumberLayout.removeAll();
    buttonsPageNumberLayout.add(firstButton);
    buttonsPageNumberLayout.add(previousButton);
    buttonsPageNumberLayout.add(nextButton);
    buttonsPageNumberLayout.add(lastButton);
    updatePaginationButtonsStatus();
  }

  public void buttonEvent(PaginationResource change) {
    resource.setPage(change.getPage());
    resource.setLimit(change.getLimit());
    firePagedChangedEvent();
  }

  public void firePagedChangedEvent() {
    addPaginationButtons();
    updatePaginationButtonsStatus();
    if (listeners != null) {
      for (PaginationChangeListener listener : listeners) {
        listener.changed(resource);
      }
    }
  }

  private void buttonClick() {
    firstButton.addClickListener(
        e -> {
          PaginationResource first = resource.first();
          buttonEvent(first);
        });

    previousButton.addClickListener(
        e -> {
          PaginationResource previous = resource.previous();
          buttonEvent(previous);
        });

    nextButton.addClickListener(
        e -> {
          PaginationResource next = resource.next();
          buttonEvent(next);
        });

    lastButton.addClickListener(
        e -> {
          PaginationResource last = resource.last();
          buttonEvent(last);
        });
  }

  public void addPageChangeListener(PaginationChangeListener listener) {
    listeners.add(listener);
  }

  public void removePageChangeListener(PaginationChangeListener listener) {
    listeners.remove(listener);
  }

  public void updatePaginationButtonsStatus() {
    firstButton.setEnabled(resource.hasPrevious());
    previousButton.setEnabled(resource.hasPrevious());
    nextButton.setEnabled(resource.isHasNext());
    lastButton.setEnabled(
        resource.isHasNext() && resource.getTotal() != null && resource.getTotal() > 0);
  }
}

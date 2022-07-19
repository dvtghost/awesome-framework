package io.awesome.ui.components.button;

public class DeleteButton extends ConfirmButton {
  public static final String DELETE_BTN_LABEL = "Delete";
  public static final String TYPE_DELETE = "Delete";

  public DeleteButton() {
    super(DELETE_BTN_LABEL, "Are you sure you want to delete this record ?", "red-button");
  }

  @Override
  public String getType() {
    return TYPE_DELETE;
  }
}

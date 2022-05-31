package io.awesome.dto;

public class FormOptionDto extends AbstractDto {

  private String value;
  private String label;

  public FormOptionDto(String valueAndLabel) {
    this.value = valueAndLabel;
    this.label = valueAndLabel;
  }

  public FormOptionDto(Integer value, Integer label) {
    this.value = value.toString();
    this.label = label.toString();
  }

  public FormOptionDto(Integer value, String label) {
    this.value = value.toString();
    this.label = label;
  }

  public FormOptionDto(String value, String label) {
    this.value = value;
    this.label = label;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }
}

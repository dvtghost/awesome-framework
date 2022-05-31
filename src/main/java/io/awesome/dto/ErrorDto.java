package io.awesome.dto;

public class ErrorDto extends AbstractDto {

  public static final String TO_STRING_DELIM = "~~br~~";

  private final Boolean isFieldError;
  private final String errorKey;
  private final Object[] errorArgs;
  private String errorField;

  public ErrorDto(String errorField, String errorKey, Object... errorArgs) {
    this.isFieldError = true;
    this.errorField = errorField;
    this.errorKey = errorKey;
    this.errorArgs = errorArgs;
  }

  public ErrorDto(String errorKey, Object... errorArgs) {
    this.isFieldError = false;
    this.errorKey = errorKey;
    this.errorArgs = errorArgs;
  }

  public String getErrorField() {
    return errorField;
  }

  public void setErrorField(String errorField) {
    this.errorField = errorField;
  }

  public Object[] getErrorArgs() {
    return errorArgs;
  }

  public Boolean getIsFieldError() {
    return isFieldError;
  }

  public String getErrorKey() {
    return errorKey;
  }

  public String toString() {
    String sep = "|";
    if (isFieldError) return true + sep + errorField + sep + errorKey;
    else return false + sep + errorKey;
  }
}

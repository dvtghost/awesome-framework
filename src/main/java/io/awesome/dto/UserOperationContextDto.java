package io.awesome.dto;

import io.awesome.enums.UserOperationContextResultType;

public class UserOperationContextDto extends AbstractDto {

  private String key;

  private UserOperationContextResultType resultType;

  private String
      messageFromBundle; // message gotten from context using key at UserOperationContextInterceptor

  private Object[] values;
  // helper methods

  public void set(UserOperationContextResultType resultType, String key) {
    this.key = key;
    this.resultType = resultType;
  }

  public void set(UserOperationContextResultType resultType, String key, Object[] values) {
    this.key = key;
    this.resultType = resultType;
    this.values = values;
  }

  public String getKey() {
    return key;
  }

  public UserOperationContextResultType getResultType() {
    return resultType;
  }

  public String getMessageFromBundle() {
    return messageFromBundle;
  }

  public void setMessageFromBundle(String messageFromBundle) {
    this.messageFromBundle = messageFromBundle;
  }

  public Object[] getValues() {
    return values;
  }

  public void setValues(Object[] values) {
    this.values = values;
  }
}

package io.awesome.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ApplicationContext implements ByteEnum {
  WebRoot(1);

  private final byte val;

  ApplicationContext(int val) {
    this.val = (byte) val;
  }

  @JsonCreator
  public static ApplicationContext fromValue(String val) {
    if (!val.equals("")) {
      return ApplicationContext.valueOf(val);
    }
    return null;
  }

  @Override
  public byte getValue() {
    return val;
  }
}

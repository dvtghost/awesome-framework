package io.awesome.enums.converter;

import org.hibernate.type.StringType;

import java.sql.Types;

public enum EnumPersistentType {
  BOOL,
  BYTE,
  STRING,
  STRING_COMMA;

  public int toSqlType() {
    if (this == BYTE) {
      return Types.TINYINT;
    } else if (this == STRING || this == STRING_COMMA) {
      return StringType.INSTANCE.sqlType();
    }
    return 0;
  }
}

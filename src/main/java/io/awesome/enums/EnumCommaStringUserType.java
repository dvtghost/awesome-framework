package io.awesome.enums;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.StringType;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class EnumCommaStringUserType implements UserType, ParameterizedType {

  private static final int[] SQL_TYPES = new int[] {StringType.INSTANCE.sqlType()};

  private Class<? extends StringEnum> enumClass;

  @Override
  public Object deepCopy(Object value) {
    return value;
  }

  @Override
  public boolean equals(Object x, Object y) {
    if (x == y) {
      return true;
    } else if (x == null) {
      return false;
    } else {
      return x.equals(y);
    }
  }

  @Override
  public boolean isMutable() {
    return false;
  }

  @Override
  public Object nullSafeGet(
      ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner)
      throws HibernateException, SQLException {
    String value = rs.getString(names[0]);
    if (rs.wasNull()) {
      return null;
    }

    try {
      String[] values = value.split(",");

      Set<StringEnum> result = new HashSet<StringEnum>();

      for (String val : values) {
        StringEnum current = getEnumValueFor(val);
        if (current != null) {
          result.add(current);
        }
      }

      return result;
    } catch (Exception e) {
      throw new HibernateException("Exception getting object from comma separated string", e);
    }
  }

  private StringEnum getEnumValueFor(String name) {
    for (StringEnum current : enumClass.getEnumConstants()) {
      if (current.getValue().equals(name)) {
        return current;
      }
    }
    return null;
  }

  @Override
  public void nullSafeSet(
      PreparedStatement st, Object value, int index, SharedSessionContractImplementor session)
      throws HibernateException, SQLException {
    if (value == null) {
      st.setNull(index, StringType.INSTANCE.sqlType());
    } else {
      if (value instanceof Set) {
        SortedSet<StringEnum> values = new TreeSet<>(Comparator.comparing(StringEnum::getValue));
        ((Set<StringEnum>) value).forEach(v -> values.add(v));

        st.setString(index, toCommaSeparated(values));
      } else if (value instanceof String) {
        st.setString(index, (String) value);
      }
    }
  }

  private String toCommaSeparated(Set<StringEnum> inputSet) {
    StringBuilder sb = new StringBuilder();
    String delim = "";
    for (StringEnum current : inputSet) {
      sb.append(delim).append(current.getValue());
      delim = ",";
    }
    return sb.toString();
  }

  @Override
  public Object replace(Object original, Object target, Object owner) {
    return original;
  }

  @Override
  public Class returnedClass() {
    return enumClass;
  }

  @Override
  public int[] sqlTypes() {
    return SQL_TYPES;
  }

  @Override
  public void setParameterValues(Properties parameters) {
    String enumClassName =
        parameters.getProperty("enumClassName"); // parameter which holds enumClassName
    try {
      enumClass = Class.forName(enumClassName).asSubclass(StringEnum.class);
    } catch (ClassNotFoundException e) {
      throw new HibernateException("Enum class not found: " + enumClassName, e);
    }

    if (!enumClass.isEnum()) {
      throw new ClassCastException(enumClass.getCanonicalName() + " must be an enum type.");
    }
  }

  @Override
  public Serializable disassemble(Object value) {
    return (Serializable) value;
  }

  @Override
  public Object assemble(Serializable cached, Object owner) {
    return cached;
  }

  @Override
  public int hashCode(Object x) {
    return x.hashCode();
  }
}

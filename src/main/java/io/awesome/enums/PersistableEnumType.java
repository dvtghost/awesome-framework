package io.awesome.enums;

import io.awesome.enums.converter.EnumPersistentConverter;
import io.awesome.enums.converter.EnumPersistentType;
import io.awesome.mapper.TypedSet;
import org.apache.commons.lang.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.DynamicParameterizedType;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

// TODO dang unit test exp handling
public class PersistableEnumType implements UserType, DynamicParameterizedType {
  private int sqlType;
  private EnumPersistentType type;
  private Class<? extends IAttributeEnum> clazz;

  @Override
  public void setParameterValues(Properties parameters) {
    ParameterType reader = (ParameterType) parameters.get(PARAMETER_TYPE);

    EnumPersistentConverter converter = getEnumConverter(reader);
    type = converter.sqlType();
    sqlType = type.toSqlType();
    clazz = converter.enumClass();
  }

  private EnumPersistentConverter getEnumConverter(ParameterType reader) {
    for (Annotation annotation : reader.getAnnotationsMethod()) {
      if (annotation instanceof EnumPersistentConverter) {
        return (EnumPersistentConverter) annotation;
      }
    }
    throw new IllegalStateException(
        "The PersistableEnumType should be used with @EnumConverter annotation.");
  }

  @Override
  public int[] sqlTypes() {
    return new int[] {sqlType};
  }

  @Override
  public Class<?> returnedClass() {
    return clazz;
  }

  @Override
  public boolean equals(Object x, Object y) throws HibernateException {
    return Objects.equals(x, y);
  }

  @Override
  public int hashCode(Object x) throws HibernateException {
    return Objects.hashCode(x);
  }

  @Override
  public Object nullSafeGet(
      ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner)
      throws HibernateException, SQLException {

    Object val = null;
    if (type == EnumPersistentType.BOOL) val = rs.getBoolean(names[0]) ? 1 : 0;
    if (type == EnumPersistentType.BYTE) val = rs.getByte(names[0]);
    if (type == EnumPersistentType.STRING || type == EnumPersistentType.STRING_COMMA)
      val = rs.getString(names[0]);
    if (rs.wasNull()) {
      return type == EnumPersistentType.STRING_COMMA ? new TypedSet(clazz) : null;
    }
    if (type == EnumPersistentType.STRING_COMMA && val instanceof String) {

      String value = (String) val;
      if (StringUtils.isBlank(value)) return new TypedSet(clazz);

      if (StringUtils.isBlank(value)) return new TypedSet(clazz);
      try {
        String[] values = value.split(",");

        TypedSet result = new TypedSet(clazz);
        Arrays.stream(values).forEach(v -> result.add(getEnumFromValue(v)));
        return result;
      } catch (Exception e) {
        throw new HibernateException("Exception getting object from comma separated string", e);
      }
    }
    return getEnumFromValue(val);
  }

  private IAttributeEnum getEnumFromValue(Object value) {
    for (IAttributeEnum pEnum : clazz.getEnumConstants()) {
      if (value != null && String.valueOf(pEnum.getValue()).equals(String.valueOf(value)))
        return pEnum;
    }
    return null;
  }

  @Override
  public void nullSafeSet(
      PreparedStatement st, Object value, int index, SharedSessionContractImplementor session)
      throws HibernateException, SQLException {
    if (value == null) {
      st.setNull(index, sqlType);
    } else {
      if (type == EnumPersistentType.BOOL) {
        IAttributeEnum pEnum = (IAttributeEnum) value;
        st.setBoolean(index, (Byte) pEnum.getValue() != 0);
      } else if (type == EnumPersistentType.BYTE) {
        IAttributeEnum pEnum = (IAttributeEnum) value;
        st.setByte(index, (Byte) pEnum.getValue());
      } else if (type == EnumPersistentType.STRING) {
        IAttributeEnum pEnum = (IAttributeEnum) value;
        st.setString(index, (String) pEnum.getValue());
      } else if (type == EnumPersistentType.STRING_COMMA) {
        SortedSet<IAttributeEnum> pEnums =
            new TreeSet<>(
                (o1, o2) -> {
                  if (o1.getValue() instanceof Byte) {
                    return (Byte) o1.getValue() - (Byte) o2.getValue();
                  } else if (o1.getValue() instanceof String) {
                    return ((String) o1.getValue()).compareTo((String) o2.getValue());
                  }
                  return 0;
                });
        ((Set<IAttributeEnum>) value).forEach(v -> pEnums.add(v));
        st.setString(index, toCommaSeparated(pEnums));
      }
    }
  }

  private String toCommaSeparated(SortedSet<IAttributeEnum> values) {
    StringBuilder sb = new StringBuilder();
    String delim = "";
    for (IAttributeEnum current : values) {
      sb.append(delim).append(current.getValue());
      delim = ",";
    }
    return sb.toString();
  }

  @Override
  public Object deepCopy(Object value) throws HibernateException {
    return value;
  }

  @Override
  public boolean isMutable() {
    return false;
  }

  @Override
  public Serializable disassemble(Object value) throws HibernateException {
    return Objects.toString(value);
  }

  @Override
  public Object assemble(Serializable cached, Object owner) throws HibernateException {
    return cached;
  }

  @Override
  public Object replace(Object original, Object target, Object owner) throws HibernateException {
    return original;
  }
}

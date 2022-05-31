package io.awesome.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;

public class JsonObjectMapper {
  public static String toJson(Object object, Logger logger) {
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      if (object == null) {
        return "null";
      }
      return objectMapper.writeValueAsString(object);
    } catch (Throwable tr) {
      logger.info(String.format("Error convert object to json %s", object.getClass().getName()));
      return object.getClass().getName();
    }
  }
}

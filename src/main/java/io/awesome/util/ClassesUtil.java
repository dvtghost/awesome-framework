package io.awesome.util;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ClassesUtil {
  public static Set<Class> findAllClassesUsingClassLoader(String packageName) {
    InputStream stream =
        ClassLoader.getSystemClassLoader().getResourceAsStream(packageName.replaceAll("[.]", "/"));
    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
    return reader
        .lines()
        .filter(line -> line.endsWith(".class"))
        .map(line -> getClass(line, packageName))
        .collect(Collectors.toSet());
  }

  public static Set<Class<? extends Enum>> findAllEnums(String packageName) {
    Reflections reflections = new Reflections(packageName, new SubTypesScanner(false));
    return new HashSet<>(reflections.getSubTypesOf(Enum.class));
  }

  public static <T> Set<Class<? extends T>> findAllClasses(Class<T> clazz, String packageName) {
    Reflections reflections = new Reflections(packageName, new SubTypesScanner(false));
    return reflections.getSubTypesOf(clazz);
  }

  private static Class getClass(String className, String packageName) {
    try {
      return Class.forName(packageName + "." + className.substring(0, className.lastIndexOf('.')));
    } catch (ClassNotFoundException e) {
      // handle the exception
    }
    return null;
  }
}

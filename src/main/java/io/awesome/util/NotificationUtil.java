package io.awesome.util;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;

public class NotificationUtil {

  public static void info(String message) {
    show(message, NotificationVariant.LUMO_PRIMARY);
  }

  public static void success(String message) {
    show(message, NotificationVariant.LUMO_SUCCESS);
  }

  public static void error(String message) {
    show(message, NotificationVariant.LUMO_ERROR);
  }

  public static void show(String message) {
    show(message, NotificationVariant.LUMO_CONTRAST);
  }

  public static void show(String message, NotificationVariant theme) {
    show(message, 5000, Notification.Position.TOP_END, theme);
  }

  public static void show(
      String message, int duration, Notification.Position position, NotificationVariant theme) {
    Notification notification = Notification.show(message, duration, position);
    notification.addThemeVariants(theme);
  }
}

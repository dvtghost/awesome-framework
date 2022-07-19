package io.awesome.ui.errors;

import com.google.common.base.Throwables;
import com.vaadin.flow.server.ErrorEvent;
import com.vaadin.flow.server.ErrorHandler;
import io.awesome.util.NotificationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

public class CustomErrorHandler implements ErrorHandler {
  private static final Logger log = LoggerFactory.getLogger(CustomErrorHandler.class);

  @Override
  public void error(ErrorEvent errorEvent) {
    log.error("Uncaught UI exception", errorEvent.getThrowable());
    NotificationUtil.error(
        "We are sorry, but an internal error occurred: " + getMessage(errorEvent.getThrowable()));
  }

  private String getMessage(Throwable throwable) {
    if (throwable.getCause() != null) {
      Throwable nestedError = throwable.getCause();
      if (nestedError instanceof DataAccessException) {
        String rootMessage = Throwables.getRootCause(nestedError).getMessage();
        if (rootMessage.contains("Detail: ")) {
          return rootMessage.substring(rootMessage.indexOf("Detail: "));
        }
        return rootMessage;
      }
    }
    return throwable.getMessage();
  }
}

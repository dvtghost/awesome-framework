package io.awesome.service;

import io.awesome.dto.UserSessionDto;

//@Service
public class UserSessionService {
  private static final long SESSION_TIMEOUT_IN_MILLIS = 1000 * 60 * 20; // 20mins
  private static final String COOKIE_TOKEN_VALUE_DELIM = "|";
  private final ThreadLocal<UserSessionDto> userSession = new ThreadLocal<UserSessionDto>();
  private final BasicUserService userService;

  public UserSessionService(BasicUserService userService) {
    this.userService = userService;
  }
}

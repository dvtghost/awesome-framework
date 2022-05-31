package io.awesome.util;

import com.vaadin.flow.server.HandlerHelper;
import com.vaadin.flow.shared.ApplicationConstants;
import io.awesome.dto.UserSessionDto;
import io.awesome.exception.BaseException;
import io.awesome.model.BaseRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public final class SecurityUtil {

  private static SecurityUtil me;
  private final Logger logger = LoggerFactory.getLogger(SecurityUtil.class);

  private SecurityUtil() {}

  public static SecurityUtil getInstance() {
    if (me == null) me = new SecurityUtil();
    return me;
  }

  public String firstLevelEncode(String rawPassword, String hashSalt) {
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      md.update(hashSalt.getBytes(StandardCharsets.UTF_8));
      byte[] hashedRawPassword = md.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
      return new String(hashedRawPassword);
    } catch (Exception e) {
      LoggerFactory.getLogger(SecurityUtil.class).error("Error encoding password");
      throw new BaseException("Error getting MD5 hash", e);
    }
  }

  public boolean isFrameworkInternalRequest(HttpServletRequest request) {
    final String parameterValue = request.getParameter(ApplicationConstants.REQUEST_TYPE_PARAMETER);
    return parameterValue != null
        && Stream.of(HandlerHelper.RequestType.values())
            .anyMatch(r -> r.getIdentifier().equals(parameterValue));
  }

  public boolean isUserLoggedIn() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication != null
        && !(authentication instanceof AnonymousAuthenticationToken)
        && authentication.isAuthenticated();
  }

  public boolean checkRole(List<? extends BaseRole> allowedRoles, BaseRole role) {
    return allowedRoles.contains(role);
  }

  public boolean isContainCurrentUserRole(BaseRole... roles) {
    var userSession =
        (UserSessionDto) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (userSession == null) return false;
    BaseRole role = userSession.getRole();
    return Arrays.asList(roles).contains(role);
  }

  public boolean isNotContainCurrentUserRole(BaseRole... roles) {
    return !isContainCurrentUserRole(roles);
  }
}

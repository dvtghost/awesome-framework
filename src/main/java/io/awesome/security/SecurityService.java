package io.awesome.security;

import io.awesome.dto.UserSessionDto;
import io.awesome.model.CredentialModel;
import io.awesome.model.BaseUser;
import io.awesome.security.authentication.CredentialDelegate;
import io.awesome.service.BaseUserService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

public class SecurityService implements AuthenticationProvider {
  private final BaseUserService baseUserService;
  private final CredentialDelegate credentialDelegate;

  public SecurityService(BaseUserService baseUserService,
                         CredentialDelegate credentialDelegate) {
    this.baseUserService = baseUserService;
    this.credentialDelegate = credentialDelegate;
  }

  private boolean verifyPassword(CredentialModel credentialModel, String rawPassword) {
    return credentialDelegate.verifyPassword(credentialModel, rawPassword);
  }

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    String username = authentication.getName();
    String rawPassword = authentication.getCredentials().toString();
    Optional<? extends BaseUser> optionalUser = baseUserService.findByEmail(username);
    if (optionalUser.isPresent()) {
      BaseUser user = optionalUser.get();
      if (Objects.nonNull(user.getUserStatus()) && user.getUserStatus().isEnabled()) {
        UserSessionDto dto =
            new UserSessionDto(
                user.getId(),
                user.fullName(),
                user.getRole(),
                user,
                null,
                null,
                user.getPassword());
        if (verifyPassword(user, rawPassword)) {
          user.setFirstTimeLoggedIn(false);
          return new UsernamePasswordAuthenticationToken(
              dto, rawPassword, Collections.singleton(user.getRole()));
        }
      }
    }
    throw new BadCredentialsException("Authentication failed");
  }

  @Override
  public boolean supports(Class<?> aClass) {
    return aClass.equals(UsernamePasswordAuthenticationToken.class);
  }

}

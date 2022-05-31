package io.awesome.security;

import io.awesome.dto.UserSessionDto;
import io.awesome.model.CredentialModel;
import io.awesome.model.BasicUser;
import io.awesome.security.authentication.CredentialDelegate;
import io.awesome.service.BasicUserService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

public class SecurityService implements AuthenticationProvider {
  private final BasicUserService basicUserService;
  private final CredentialDelegate credentialDelegate;

  public SecurityService(BasicUserService basicUserService,
                         CredentialDelegate credentialDelegate) {
    this.basicUserService = basicUserService;
    this.credentialDelegate = credentialDelegate;
  }

  private boolean verifyPassword(CredentialModel credentialModel, String rawPassword) {
    return credentialDelegate.verifyPassword(credentialModel, rawPassword);
  }

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    String username = authentication.getName();
    String rawPassword = authentication.getCredentials().toString();
    Optional<? extends BasicUser> optionalUser = basicUserService.findByEmail(username);
    if (optionalUser.isPresent()) {
      BasicUser user = optionalUser.get();
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

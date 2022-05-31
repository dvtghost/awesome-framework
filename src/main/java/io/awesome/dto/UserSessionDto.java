package io.awesome.dto;

import io.awesome.model.Role;
import io.awesome.model.BasicUser;
import io.awesome.util.HashUtil;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
public class UserSessionDto extends AbstractDto implements UserDetails {

  /* necessary for signature signing */
  private String userId;
  private Role role;
  private String username;

  private Timestamp loggedInOn;

  private Timestamp lastAccessedOn;

  private String hashSalt;
  /* end signature signing */

  private Map<String, Object> valuesToPersistAcrossRequests = new LinkedHashMap<String, Object>();

  private Boolean isAccessingNonAuth = false;

  private Boolean isFromSecureCookieParameter = false;

  private BasicUser user;

  public UserSessionDto(
      String userId,
      String username,
      Role role,
      BasicUser user,
      Timestamp loggedInOn,
      Timestamp lastAccessedOn,
      String hashSalt) {
    super();
    this.userId = userId;
    this.username = username;
    this.role = role;
    this.loggedInOn = loggedInOn;
    this.lastAccessedOn = lastAccessedOn;
    this.hashSalt = hashSalt;
  }

  public String buildValiditySignature() {
    String toSign = getUserId() + (getLoggedInOn().getTime() + getLastAccessedOn().getTime());
    return HashUtil.getInstance().getHash(toSign, getHashSalt());
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return null;
  }

  @Override
  public String getPassword() {
    return null;
  }

  @Override
  public String getUsername() {
    return this.username;
  }

  @Override
  public boolean isAccountNonExpired() {
    return false;
  }

  @Override
  public boolean isAccountNonLocked() {
    return false;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return false;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}

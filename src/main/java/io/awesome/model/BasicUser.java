package io.awesome.model;

import org.apache.commons.lang.StringUtils;

public interface BasicUser extends CredentialModel {

  String getId();
  String getEmail();
  String getFirstName();
  String getLastName();
  UserStatus getUserStatus();
  Role getRole();
  void setFirstTimeLoggedIn(boolean flag);

  default String fullName() {
    return StringUtils.trimToEmpty(getFirstName()) + " " + StringUtils.trimToEmpty(getLastName());
  }
}

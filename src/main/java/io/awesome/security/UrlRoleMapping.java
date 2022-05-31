package io.awesome.security;

import io.awesome.model.Role;

import java.util.List;

public interface UrlRoleMapping {

    List<? extends Role> getRoles();
}

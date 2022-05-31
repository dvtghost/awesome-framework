package io.awesome.security;

import io.awesome.model.BaseRole;

import java.util.List;

public interface IUrlRoleMapping {

    List<? extends BaseRole> getRoles();
}

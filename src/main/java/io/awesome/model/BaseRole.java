package io.awesome.model;

import org.springframework.security.core.GrantedAuthority;

public interface BaseRole extends GrantedAuthority {
    String getLabel();

    String getType();

    String getShortName();
}

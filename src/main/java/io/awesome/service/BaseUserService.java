package io.awesome.service;

import io.awesome.model.BaseUser;

import java.util.Optional;

public interface BaseUserService {

    Optional<? extends BaseUser> findByEmail(String email);
}

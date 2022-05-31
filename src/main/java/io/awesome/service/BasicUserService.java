package io.awesome.service;

import io.awesome.model.BasicUser;

import java.util.Optional;

public interface BasicUserService {

    Optional<? extends BasicUser> findByEmail(String email);
}

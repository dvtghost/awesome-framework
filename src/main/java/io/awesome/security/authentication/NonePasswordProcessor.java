package io.awesome.security.authentication;

import io.awesome.model.CredentialModel;
import org.apache.commons.lang3.StringUtils;

public class NonePasswordProcessor implements PasswordProcessor {
    @Override
    public boolean verify(CredentialModel credentialModel, String rawPassword) {
        return credentialModel != null && StringUtils.equals(credentialModel.getPassword(), rawPassword);
    }

    @Override
    public String hash(CredentialModel credentialModel, String rawPassword) {
        return rawPassword;
    }
}

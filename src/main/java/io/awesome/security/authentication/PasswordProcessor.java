package io.awesome.security.authentication;

import io.awesome.model.CredentialModel;

public interface PasswordProcessor {

    /**
     * Verify raw password
     * @param credentialModel
     * @param rawPassword
     * @return
     */
    boolean verify(CredentialModel credentialModel, String rawPassword);

    /**
     * Hash raw password
     * @param credentialModel
     * @param rawPassword
     * @return
     */
    String hash(CredentialModel credentialModel, String rawPassword);
}

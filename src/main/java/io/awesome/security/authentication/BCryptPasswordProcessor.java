package io.awesome.security.authentication;

import io.awesome.model.CredentialModel;
import io.awesome.util.SecurityUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BCryptPasswordProcessor implements PasswordProcessor {
    @Override
    public boolean verify(CredentialModel credentialModel, String rawPassword) {
        BCryptPasswordEncoder encoder = getBCryptPasswordEncoder();
        return encoder.matches(
                SecurityUtil.getInstance().firstLevelEncode(rawPassword, credentialModel.getHashSalt()), credentialModel.getPassword());
    }

    @Override
    public String hash(CredentialModel credentialModel, String rawPassword) {
        return getBCryptPasswordEncoder()
                .encode(SecurityUtil
                        .getInstance().
                        firstLevelEncode(rawPassword, credentialModel.getHashSalt()));
    }

    private BCryptPasswordEncoder getBCryptPasswordEncoder() {
        return new BCryptPasswordEncoder(BCryptPasswordEncoder.BCryptVersion.$2Y, 8);
    }
}

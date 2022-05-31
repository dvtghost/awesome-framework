package io.awesome.security.authentication;

import io.awesome.model.CredentialModel;

public class CredentialDelegate {

    private Type type;

    public boolean verifyPassword(CredentialModel credentialModel, String password) {
        return getPasswordProcessor().verify(credentialModel, password);
    }

    public PasswordProcessor getPasswordProcessor(){
        PasswordProcessor passwordProcessor;
        switch(getType()) {
            case BCRYPT:
                passwordProcessor = new BCryptPasswordProcessor();
                break;
            default:
                passwordProcessor = new NonePasswordProcessor();
        }
        return passwordProcessor;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public static enum Type {
        NONE,
        BCRYPT;
    }
}
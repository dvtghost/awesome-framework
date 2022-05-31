package io.awesome.security;

import com.vaadin.flow.component.Component;
import io.awesome.security.authentication.CredentialDelegate;

public class SecurityConfigAdapter {

    public CredentialDelegate getCredentialDelegate() {
        CredentialDelegate credentialDelegate = new CredentialDelegate();
        credentialDelegate.setType(CredentialDelegate.Type.BCRYPT);
        configure(credentialDelegate);
        return credentialDelegate;
    }

    protected void configure(CredentialDelegate credentialDelegate) {
    }

    protected Class<? extends Component> configureUnAuthorizedPage() {
        return null;
    }

    public IUrlRoleMapping getUrlRoleMapping(Class<? extends Component> navigationTarget) {
        return null;
    }

}

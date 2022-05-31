package io.awesome.security;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.router.BeforeEnterEvent;
import io.awesome.dto.UserSessionDto;
import io.awesome.model.BaseRole;
import io.awesome.service.BaseUserService;
import io.awesome.util.SecurityUtil;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;

public class SecurityContext {

    private SecurityConfigAdapter securityConfigAdapter;
    private SecurityService securityService;

    public SecurityContext(
            BaseUserService baseUserService,
            SecurityConfigAdapter securityConfigAdapter) {
        this.securityConfigAdapter = securityConfigAdapter;
        this.securityService = new SecurityService(baseUserService, securityConfigAdapter.getCredentialDelegate());
    }

    public SecurityService getSecurityService() {
        return securityService;
    }

    public Class<? extends Component> redirectUrl(BeforeEnterEvent event) {
        var navigationTarget = (Class<? extends Component>) event.getNavigationTarget();
        if (navigationTarget != null && checkForRoleAccess(navigationTarget)) {
            return navigationTarget;
        }
        return securityConfigAdapter.configureUnAuthorizedPage();
    }

    public boolean checkForRoleAccess(Class<? extends Component> navigationTarget) {
        var user =
                (UserSessionDto) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (user != null) {
            IUrlRoleMapping urlRoleMapping = securityConfigAdapter.getUrlRoleMapping(navigationTarget);
            if (urlRoleMapping != null) {
                return SecurityUtil.getInstance().checkRole(urlRoleMapping.getRoles(), user.getRole());
            }
        }
        return false;
    }

    public List<BaseRole> getRoleAccessCurrentPage(Class<? extends Component> clazz) {
        List<BaseRole> roles = new ArrayList<>();
        IUrlRoleMapping urlRoleMapping = securityConfigAdapter.getUrlRoleMapping(clazz);
        if (urlRoleMapping != null) {
            roles.addAll(urlRoleMapping.getRoles());
        }
        return roles;
    }
}

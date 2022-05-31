package io.awesome.context;

import io.awesome.security.SecurityConfigAdapter;
import io.awesome.security.SecurityContext;
import io.awesome.service.BaseUserService;
import io.awesome.workflow.WorkflowContext;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class AwesomeContext {
    private final SecurityContext security;
    private final WorkflowContext workflow;

    public AwesomeContext(ApplicationContext applicationContext) {
        security = initSecurity(applicationContext);
        workflow = initWorkflow(applicationContext);
    }

    private SecurityContext initSecurity(ApplicationContext applicationContext) {
        try {
            return applicationContext.getBean(SecurityContext.class);
        } catch (BeansException e){
            return initNewSecurity(applicationContext);
        }
    }

    private SecurityContext initNewSecurity(ApplicationContext applicationContext) {
        try {
            SecurityConfigAdapter adapter = applicationContext.getBean(SecurityConfigAdapter.class);
            BaseUserService userService = applicationContext.getBean(BaseUserService.class);
            return new SecurityContext(userService, adapter);
        } catch (Exception e) {
            return null;
        }
    }

    private WorkflowContext initWorkflow(ApplicationContext applicationContext) {
        try {
            return applicationContext.getBean(WorkflowContext.class);
        } catch (BeansException e){
            return new WorkflowContext();
        }
    }

    public SecurityContext getSecurity() {
        return security;
    }

    public WorkflowContext getWorkflow() {
        return workflow;
    }
}

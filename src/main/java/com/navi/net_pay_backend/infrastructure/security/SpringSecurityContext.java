package com.navi.net_pay_backend.infrastructure.security;

import com.navi.net_pay_backend.domain.service.SecurityContext;
import com.navi.net_pay_backend.domain.service.model.SecurityPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class SpringSecurityContext implements SecurityContext {

    @Override
    public Optional<Long> getUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof Long) {
            return Optional.of((Long) authentication.getPrincipal());
        }
        return Optional.empty();
    }

    @Override
    public void setContext(SecurityPrincipal principal) {
    }

    @Override
    public void clear() {
        SecurityContextHolder.clearContext();
    }
}

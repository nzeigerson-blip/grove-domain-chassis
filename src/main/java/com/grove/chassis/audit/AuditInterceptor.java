package com.grove.chassis.audit;

import com.grove.chassis.logging.CorrelationIdContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Chassis interceptor that logs immutable audit events for all API requests.
 * Registered by WebConfig on /api/** paths.
 */
@Component
public class AuditInterceptor implements HandlerInterceptor {

    private final AuditLogger auditLogger;

    public AuditInterceptor(AuditLogger auditLogger) {
        this.auditLogger = auditLogger;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        String actor = extractActor();
        String action = request.getMethod() + " " + request.getRequestURI();
        String correlationId = CorrelationIdContext.get();
        String sourceIp = request.getRemoteAddr();
        String resource = request.getRequestURI();

        if (ex != null || response.getStatus() >= 400) {
            auditLogger.log(AuditEvent.failure(correlationId, actor, action, resource, sourceIp));
        } else {
            auditLogger.log(AuditEvent.success(correlationId, actor, action, resource, sourceIp));
        }
    }

    private String extractActor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            return auth.getName();
        }
        return "anonymous";
    }
}

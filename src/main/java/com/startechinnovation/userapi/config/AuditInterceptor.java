package com.startechinnovation.userapi.config;

import com.startechinnovation.userapi.entity.AuditLog;
import com.startechinnovation.userapi.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AuditInterceptor implements HandlerInterceptor {

    private final AuditLogRepository auditLogRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            String role = auth.getAuthorities().stream()
                    .map(a -> a.getAuthority())
                    .findFirst()
                    .orElse("UNKNOWN");

            AuditLog log = AuditLog.builder()
                    .username(auth.getName())
                    .role(role)
                    .action(request.getMethod() + " " + request.getRequestURI())
                    .endpoint(request.getRequestURI())
                    .timestamp(LocalDateTime.now())
                    .build();
            auditLogRepository.save(log);
        }
        return true;
    }
}

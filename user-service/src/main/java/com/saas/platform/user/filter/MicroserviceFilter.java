package com.saas.platform.user.filter;

import com.saas.platform.db.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(1) // Make sure this runs BEFORE TenantFilter
public class MicroserviceFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Set microservice for this request
        TenantContext.setMicroservice("user");

        // Handle TenantId
        String tenantId = request.getHeader("X-Tenant-Id");
        System.out.println("Tenant === " + tenantId);

        if (tenantId != null && !tenantId.isBlank()) {
            TenantContext.setTenantId(tenantId);
            MDC.put("tenantId", tenantId);
        } else {
            MDC.remove("tenantId");   // clean fallback
        }

        // Handle trace ID (independent from tenant)
        String traceId = request.getHeader("X-Trace-Id");
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
        }
        MDC.put("traceId", traceId);

        System.out.println("TraceId set in filter " + traceId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear(); // ABSOLUTELY REQUIRED to prevent leaking values to next requests
        }
    }
}


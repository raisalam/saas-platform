package com.saas.platform.user.filter;

import com.saas.platform.db.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@Order(1) // Runs BEFORE TenantFilter
public class MicroserviceFilter extends OncePerRequestFilter {

    // Define public paths (no JWT required)
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/user/login",
            "/api/user/register",
            "/api/user/refresh"
    );

    private static boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(p -> path.equals(p) || path.matches(p.replace("**", ".*")));
    }



    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        // Public paths: only tenantId required
        boolean publicPath = isPublicPath(path);
        boolean isInternalCall = request.getHeader("X-Internal-call") != null;

        String gatewayId = request.getHeader("X-Gateway-Id");
        String tenantId = request.getHeader("X-Tenant-Id");
        String traceId = request.getHeader("X-Trace-Id");

        // Reject any request without X-Gateway-Id (except public endpoints if you allow direct)
        if (!isInternalCall && !publicPath && (gatewayId == null || gatewayId.isBlank() || !gatewayId.equals("841428-001"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or missing gateway ID");
            return;
        }

        // TenantId is always required
        if (tenantId == null || tenantId.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing X-Tenant-Id");
            return;
        }

        TenantContext.setTenantId(tenantId);
        TenantContext.setMicroservice("user");
        MDC.put("tenantId", tenantId);



        // For non-public endpoints, sellerId and userRole must exist
        if (!publicPath) {
            String sellerId = request.getHeader("X-Seller-Id");
            String userRole = request.getHeader("X-User-Role");
            if (sellerId == null || sellerId.isBlank()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing X-Seller-Id");
                return;
            }
            if (userRole == null || userRole.isBlank()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing X-User-Role");
                return;
            }
            MDC.put("sellerId", sellerId);
            MDC.put("userRole", userRole);
        }

        // Trace ID: generate if missing
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
        }
        MDC.put("traceId", traceId);
        System.out.println("==== MDC BEFORE filterChain ====");
        MDC.getCopyOfContextMap().forEach((key, value) -> System.out.println(key + " = " + value));
        System.out.println("================================");

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear(); // clean up for next request
        }
    }
}

package com.saas.platform.catalog.client;

import com.saas.platform.catalog.dto.UserResponse;
import com.saas.platform.common.dto.CachedUserDTO;
import com.saas.platform.common.redis.RedisService;
import com.saas.platform.db.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserClient {

    private final RedisService redisService;
    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;

    @Value("${saas.user-service.url}")
    private String userServiceBaseUrl;

    private static final String REDIS_BALANCE_KEY = "tenant/%s/user/%d";

    /* ============================================================
       READ — Get Balance (Redis → User Service fallback)
       ============================================================ */
    public double getBalance(String tenantId, Long userId) {
        String redisKey = redisKey(tenantId, userId);
        System.out.println("==================redisKey========"+redisKey);

        // 1️⃣ Try Redis
        try {
            Optional<CachedUserDTO> cached = redisService.get(redisKey, CachedUserDTO.class);
            System.out.println("Data read from Redis");

            if (cached.isPresent()) {

                CachedUserDTO cachedUserDTO =  cached.get();
                System.out.println("Balance in redis = "+cachedUserDTO.getBalance());
               return cachedUserDTO.getBalance();
            }
        } catch (Exception e) {
            log.warn("Redis unavailable for balance read user={}", userId, e);
        }

        // 2️⃣ Fallback to User Service
        return fetchBalanceFromUserServiceAndCache(redisKey, userId);
    }

    /* ============================================================
       WRITE — Set Balance (User Service → Redis)
       ============================================================ */
    public void setBalance(String tenantId, Long userId, double newBalance) {
        String redisKey = redisKey(tenantId, userId);

        try {

            Optional<CachedUserDTO> cached =
                    redisService.get(redisKey, CachedUserDTO.class);
            System.out.println("Setting Data to Redis");

            if (cached.isPresent()) {

                CachedUserDTO cachedUserDTO = cached.get();
                cachedUserDTO.setBalance(newBalance);
                redisService.setAsync(redisKey, cachedUserDTO);
            }

         //   postBalanceUpdateToUserService(userId, newBalance);

            // write-through cache

        } catch (Exception e) {
            log.error("Failed to update balance user={}", userId, e);
            throw new IllegalStateException("Balance update failed", e);
        }
    }

    /* ============================================================
       INTERNAL HELPERS
       ============================================================ */

    private double fetchBalanceFromUserServiceAndCache(
            String redisKey,
            Long userId
    ) {
        try {
            String url = userServiceBaseUrl + userId;

            // Set headers (e.g., auth headers)
            HttpHeaders headers = defaultHeaders(userId);

            HttpEntity<Void> entity = new HttpEntity<>(headers); // No body for GET

            ResponseEntity<UserResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,   // Use GET instead of POST
                    entity,
                    UserResponse.class
            );

            UserResponse userResponse = response.getBody();

            if (userResponse == null) {
                throw new IllegalStateException("User Service returned null balance");
            }
            if(!userResponse.isActive()){
                throw new IllegalStateException("User is inactive");
            }
            Double balance = userResponse.getBalance();

            try{
                redisService.setAsync(redisKey, balance);
            }catch (Exception e){
                System.out.println("Ignoring exception");
            }
            return balance;

        } catch (RestClientException e) {
            log.error("User Service balance fetch failed user={}", userId, e);
            throw new IllegalStateException("Unable to retrieve balance", e);
        }

    }

    private void postBalanceUpdateToUserService(Long userId, double balance, String tenantId) {
        String url = userServiceBaseUrl + "/users/balance/update";

        HttpHeaders headers = defaultHeaders(userId);

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(
                        Map.of(
                                "userId", userId,
                                "balance", balance
                        ),
                        headers
                );

        restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                Void.class
        );
    }

    /**
     * Fetches multiple usernames in a single batch call.
     * URL Example: /users/names?ids=1,2,3
     */
    public Map<String, String> getUsernamesBatch(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }

        try {
            // Convert Set to comma-separated String
            String idsParam = userIds.stream()
                    .map(String::valueOf)
                    .collect(java.util.stream.Collectors.joining(","));

            String url = userServiceBaseUrl + "/names?ids=" + idsParam;

            HttpEntity<Void> entity = new HttpEntity<>(defaultHeaders(0L)); // System-level or dummy ID for headers

            // Expected response from User Service: Map<Long, String>
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            return response.getBody() != null ? (Map<String, String>) response.getBody() : Map.of();
        } catch (Exception e) {
            log.error("Failed to fetch batch usernames for ids={}", userIds, e);
            return Map.of();
        }
    }

    private HttpHeaders defaultHeaders(Long userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));


        headers.set("X-Gateway-Id", "841428-001");
        headers.set("X-Tenant-Id", TenantContext.getTenantId());
        headers.set("X-Trace-Id", MDC.get("traceId"));
        headers.set("X-Internal-call", "true");
        headers.set("X-User-Role", MDC.get("userRole"));
        headers.set("X-Seller-Id", MDC.get("sellerId"));
        return headers;
    }

    private String redisKey(String tenantId, Long userId) {
        return String.format(REDIS_BALANCE_KEY, tenantId, userId);
    }
}

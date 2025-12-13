// src/main/java/com/example/demo/filter/JwtFilterFunctions.java
package com.saas.platform.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

public class JwtFilterFunctions {

    private static final String JWT_SECRET = "4d0e47912ec2f5313b35510b75630f95edcd45afee883427af7b83c0696c2170ec074a4a17b74ed2dae0a817eb8aa884759706a280d736e80ce696103fe55756";  // your secret
    private static final SecretKey KEY = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));

    // Public endpoints – no JWT required (add more as needed)
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/user/login", "/api/user/register", "/api/auth/login", "/api/auth/register", "/actuator/**"
    );

    public static HandlerFilterFunction<ServerResponse, ServerResponse> authenticate() {
        return (request, next) -> {
            // 1. Skip for public paths

            String path = request.uri().getPath();
            System.out.println("PATH === "+path);
            if (isPublicPath(path)) {
                System.out.println("Inside public path");
                return next.handle(request);
            }

            request.headers().asHttpHeaders().forEach((key, values) -> {
                System.out.println(key + " = " + values);
            });
            // 2. Get token from Authorization: Bearer <token>
            String authHeader = request.headers().firstHeader(HttpHeaders.AUTHORIZATION);
            System.out.println("Auth header ===== "+authHeader);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ServerResponse.status(HttpStatus.UNAUTHORIZED).build();
            }

            String token = authHeader.substring(7);
            System.out.println("Token  "+token);
            try {
                System.out.println("Going to verify ");
                // 3. Validate token (signature, expired, nbf)
                Claims claims = Jwts.parser()
                        .verifyWith(KEY)           // your exact key
                        .clockSkewSeconds(30)      // allow 30s clock drift
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                System.out.println("Verification success");
                System.out.println(claims.toString());


                // Optional: forward user info downstream
                ServerRequest modifiedRequest = ServerRequest.from(request)
                        .header("X-User-Id", claims.getSubject())                                      // e.g. tenant/T1001/user/1
                        .header("X-Seller-Id", String.valueOf(claims.get("sellerId")))                 // safe for Integer/null
                        .header("X-Tenant-Id", String.valueOf(claims.get("tenantId")))                   // extract T1001
                        .header("X-User-Role", String.valueOf(claims.get( "role")))                       // handles String or List
                        .build();

                // 4. Proceed with modified request
                return next.handle(modifiedRequest);

            } catch (ExpiredJwtException e) {
                System.out.println("JWT expired");
                return ServerResponse.status(HttpStatus.UNAUTHORIZED)
                        .header("X-Reason", "jwt-expired")
                        .build();
            } catch (Exception e) {
                e.printStackTrace();
                return ServerResponse.status(HttpStatus.UNAUTHORIZED)
                        .header("X-Reason", "UnknownError")
                        .build();
            }
        };
    }

    private static boolean isPublicPath(String path) {
        return PUBLIC_PATHS.contains(path);    }

    private static String getClaimAsString(Claims claims, String key) {
        Object value = claims.get(key);
        if (value == null) return null;
        if (value instanceof String s) return s;
        if (value instanceof List<?> list) return String.join(",", list.stream()
                .map(Object::toString)
                .toList());
        return String.valueOf(value);
    }
}
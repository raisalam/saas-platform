package com.saas.platform.user.service;

import com.saas.platform.common.events.DomainEventPublisher;
import com.saas.platform.common.jwt.JwtService;
import com.saas.platform.common.kafka.KafkaPublisher;
import com.saas.platform.common.mqtt.MqttService;
import com.saas.platform.common.redis.RedisService;
import com.saas.platform.db.TenantContext;
import com.saas.platform.user.domain.event.user.UserLoggedInEvent;
import com.saas.platform.user.domain.event.user.UserRegisteredEvent;
import com.saas.platform.user.dto.LoginRequest;
import com.saas.platform.user.dto.RegisterRequest;
import com.saas.platform.user.dto.TokenResponse;
import com.saas.platform.user.dto.UserResponse;
import com.saas.platform.user.entity.Role;
import com.saas.platform.user.entity.User;
import com.saas.platform.user.mapper.UserMapper;
import com.saas.platform.user.repository.RoleRepository;
import com.saas.platform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repo;
    private final RoleRepository roleRepo;
    private final JwtService jwtService;
    private final UserMapper mapper;
    private final DomainEventPublisher eventPublisher;


    public User register(RegisterRequest dto) {

        if (repo.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Username already taken: " + dto.getUsername());
        }

        if (dto.getEmail() != null && !dto.getEmail().isEmpty() && repo.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + dto.getEmail());
        }

        User user = mapper.toEntity(dto);

        // Hash password
        user.setPassword(dto.getPassword());

        Role sellerRole = roleRepo.findByName("SELLER")
                .orElseThrow(() -> new RuntimeException("SELLER role not found"));

        // Set default role
        user.setRole(sellerRole);


        // Save
         user =  repo.save(user);
        UserRegisteredEvent event = UserRegisteredEvent.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .role(sellerRole.getName())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .deviceId(user.getDeviceId())
                .androidId(user.getAndroidId())
                .build();

        // 🔥 publish once → all enabled handlers will run automatically
        eventPublisher.publish(event);


        return user;
    }

    public TokenResponse login(LoginRequest dto) {
        // Find user by username or email
        User user = repo.findByUsername(dto.getUsernameOrEmail())
                .or(() -> repo.findByEmail(dto.getUsernameOrEmail()))
                .orElseThrow(() -> new IllegalArgumentException("Invalid username/email or password"));

        // Verify password (use encoder if hashed)
        if (!dto.getPassword().equals(user.getPassword())) {
            throw new IllegalArgumentException("Invalid username/email or password");
        }

        // Verify device
        if (!dto.getAndroidId().equals(user.getAndroidId()) || !dto.getDeviceId().equals(user.getDeviceId())) {
            throw new IllegalArgumentException("Device not recognized");
        }
        /**

        Double balance = user.getBalance();
        mqttService.publishAsync( "user/" + user.getId(), balance.toString());
        redisService.setAsync("user/" + user.getId(), balance.toString());
        kafkaPublisher.publishAsync(new SellerLogin(
                user.getId(),
                user.getDeviceId(),
                "IpAddress",
                "UserAgent",
                Instant.now()));
         **/

        UserLoggedInEvent event = UserLoggedInEvent.builder()
                .userId(user.getId())
                .balance(user.getBalance())
                .androidId(user.getAndroidId())
                .build();

        // 🔥 publish once → all enabled handlers will run automatically
        eventPublisher.publish(event);

        String accessToken = generateUserToken( user);
        // Generate refresh token
        System.out.printf(accessToken);
        String refreshToken = jwtService.generateRefreshToken(user.getUsername());

        // Save refresh token to user
        user.setRefreshToken(refreshToken);
        repo.save(user);


        return new TokenResponse(
                accessToken,
                refreshToken,
                user.getFullName(),
                user.getRole().getName()
        );
    }

    public UserResponse getProfile(String username) {
        return repo.findByUsername(username)
                .map(mapper::toResponse)
                .orElseThrow();
    }

    public TokenResponse refresh(String refreshToken) {
        // Validate token format and expiration
        /**
         if (!jwtService.validate(refreshToken) || jwtService.isExpired(refreshToken)) {
         throw new IllegalArgumentException("Invalid or expired refresh token");
         }

         // Extract username from token
         String username = jwtService.getSubject(refreshToken);
         */
        // Find user
        User user = repo.findByUsername("username")
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Check stored refresh token matches
        if (!refreshToken.equals(user.getRefreshToken())) {
            throw new IllegalArgumentException("Refresh token mismatch");
        }
/**
 // Generate new tokens
 String newAccessToken = jwtService.generateAccessToken(
 user.getUsername(),
 Map.of(
 "role", user.getRole().getName(),
 "userId", user.getId()
 )
 );

 String newRefreshToken = jwtService.generateRefreshToken(user.getUsername());
 **/
        // Update stored refresh token
        user.setRefreshToken("newRefreshToken");
        repo.save(user);

        return new TokenResponse(
                "newAccessToken",
                "newRefreshToken",
                user.getUsername(),
                user.getRole().getName()
        );
    }

    public String generateUserToken(User user) {
        String tenantId = TenantContext.getTenantId();
        String subject = "tenant/" + tenantId + "/user/" + user.getId();
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", subject);
        claims.put("sellerId", user.getId());
        claims.put("role", user.getRole().getName());
        // --- ACL: subscribe-only ---
        List<Map<String, Object>> acl = List.of(
                // Allow subscribe to public notifications
                Map.of(
                        "permission", "allow",
                        "action", "subscribe",
                        "topic", "public/notification"
                ),
                // Allow subscribe to tenant public notifications
                Map.of(
                        "permission", "allow",
                        "action", "subscribe",
                        "topic", "tenant/" + tenantId + "/public/notification"
                ),
                // Allow subscribe to user's own events
                Map.of(
                        "permission", "allow",
                        "action", "subscribe",
                        "topic", "tenant/" + tenantId + "/user/" + user.getId() + "/#"
                ),
                // OPTIONAL: explicitly deny publish everywhere (recommended)
                Map.of(
                        "permission", "deny",
                        "action", "publish",
                        "topic", "#"
                )
        );
        claims.put("acl", acl);

        return jwtService.generateAccessToken(subject, claims);
    }

}

package com.saas.platform.user.service;

import com.saas.platform.common.events.DomainEventPublisher;
import com.saas.platform.common.jwt.JwtService;
import com.saas.platform.db.TenantContext;
import com.saas.platform.user.domain.event.key.BalanceUpdatedEvent;
import com.saas.platform.user.domain.event.key.UserLoggedInEvent;
import com.saas.platform.user.domain.event.key.UserRegisteredEvent;
import com.saas.platform.user.dto.LoginRequest;
import com.saas.platform.user.dto.RegisterRequest;
import com.saas.platform.user.dto.TokenResponse;
import com.saas.platform.user.dto.UserResponse;
import com.saas.platform.user.entity.Role;
import com.saas.platform.user.entity.User;
import com.saas.platform.user.entity.UserAttribute;
import com.saas.platform.user.mapper.UserActivityMapper;
import com.saas.platform.user.mapper.UserMapper;
import com.saas.platform.user.repository.RoleRepository;
import com.saas.platform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.util.StringUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repo;
    private final RoleRepository roleRepo;
    private final JwtService jwtService;
    private final UserMapper mapper;
    private final DomainEventPublisher eventPublisher;


    public UserResponse register(RegisterRequest dto) {

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
        user = repo.save(user);
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


        return mapper.toResponse(user);
    }

    @Transactional(value = "transactionManager")
    public TokenResponse login(LoginRequest dto) {
        // Find user by username or email
        User user = repo.findByUsername(dto.getEmail())
                .or(() -> repo.findByEmail(dto.getEmail()))
                .orElseThrow(() -> new IllegalArgumentException("Invalid username/email or password"));

        // Verify password (use encoder if hashed)
        if (!dto.getPassword().equals(user.getPassword())) {
            throw new IllegalArgumentException("Invalid username/email or password");
        }

        if (StringUtils.hasText(user.getAndroidId())) {
           user.setAndroidId(user.getAndroidId());
        } else if (!dto.getAndroidId().equals(user.getAndroidId())) {
            throw new IllegalArgumentException("Android not recognized");
        }
        UserLoggedInEvent event = UserLoggedInEvent.builder()
                .tenantId(TenantContext.getTenantId())
                .userId(user.getId())
                .total(user.getTotal())
                .role(user.getRole().getName())
                .balance(user.getBalance())
                .androidId(user.getAndroidId())
                .active(user.isActive())
                .build();

        // 🔥 publish once → all enabled handlers will run automatically
        eventPublisher.publish(event);

        String token = generateUserToken(user);
        // Generate refresh token
        String refreshToken = UUID.randomUUID().toString();

        // Save refresh token to user
        user.setRefreshToken(refreshToken);
        return new TokenResponse(
                user.getId(),
                token,
                refreshToken,
                user.getFullName(),
                user.getRole().getName(),
                getAvatar(user)
        );
    }

    @Transactional(value = "transactionManager") // 👈 Specify the bean name
    public UserResponse getProfile(String username) {
        return repo.findByUsername(username)
                .map(mapper::toResponse)
                .orElseThrow();
    }

    @Transactional(value = "transactionManager") // 👈 Specify the bean name
    public UserResponse getProfile(Long userId) {
        return repo.findById(userId)
                .map(mapper::toResponse)
                .orElseThrow();
    }

    @Transactional(value = "transactionManager")
    public TokenResponse refresh(String refreshToken) {
        // Validate token format and expiration
        /**

         // Extract username from token
         String username = jwtService.getSubject(refreshToken);
         */
        // Find user
        User user = repo.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));


        // Generate new tokens
        String newAccessToken = generateUserToken(user);
        String newRefreshToken = UUID.randomUUID().toString();

        // Update stored refresh token
        user.setRefreshToken(newRefreshToken);
        repo.save(user);

        return new TokenResponse(
                user.getId(),
                newAccessToken,
                newRefreshToken,
                user.getFullName(),
                user.getRole().getName(),
                getAvatar(user)
        );
    }

    public String generateUserToken(User user) {
        String tenantId = TenantContext.getTenantId();
        String subject = "tenant/" + tenantId + "/user/" + user.getId();
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", subject);
        claims.put("sellerId", user.getId());
        claims.put("tenantId", tenantId);
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
                        "topic", "tenant/" + tenantId + "/public/#"
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

    public String getAvatar(User user) {
        return user.getAttributes()
                .stream()
                .filter(attr -> "avatar".equals(attr.getKey()))
                .map(UserAttribute::getValue)
                .findFirst()
                .orElse("https://res.cloudinary.com/dfcy1i11m/image/upload/v1763439718/avatars/avatar_1.jpg");
    }


    @Transactional(value = "transactionManager") // 👈 Specify the bean name
    public UserResponse updateBalance(Long userId, Double totalCost) {
        User user = repo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        System.out.println("User current balance === " + user.getBalance());
        System.out.println("Total cost is " + totalCost);
        user.setBalance(user.getBalance() - totalCost);
        System.out.println("Final user gabalnce = " + user.getBalance());
        return mapper.toResponse(user);
    }

    public void deductBalance(String tenantId, Long userId, Double totalCost, String correlationId) {
    }
}

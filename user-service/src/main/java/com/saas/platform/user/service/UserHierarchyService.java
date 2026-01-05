package com.saas.platform.user.service;// UserHierarchyService.java

import com.fasterxml.jackson.core.JsonProcessingException;
import com.saas.platform.common.events.DomainEventPublisher;
import com.saas.platform.db.TenantContext;
import com.saas.platform.user.domain.event.key.UserRechargedEvent;
import com.saas.platform.user.dto.RechargeCalc;
import com.saas.platform.user.dto.UserResponse;
import com.saas.platform.user.entity.User;
import com.saas.platform.user.entity.UserAttribute;
import com.saas.platform.user.entity.UserHierarchy;
import com.saas.platform.user.factory.ActivityFactory;
import com.saas.platform.user.mapper.UserMapper;
import com.saas.platform.user.repository.UserHierarchyRepository;
import com.saas.platform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserHierarchyService {

    private final UserHierarchyRepository hierarchyRepo;
    private final UserRepository userRepo;
    private final UserMapper userMapper;
    private final DomainEventPublisher eventPublisher;
    private final UserActivityService userActivityService;


    @Transactional(value = "transactionManager", readOnly = true) // 👈 Specify the bean name
    public List<UserResponse> getChildSellers(Long parentId) {

        return hierarchyRepo.findByParentIdWithChild(parentId)
                .stream()
                // 2. Map from UserHierarchy to User Entity
                .map(UserHierarchy::getChild)
                // 3. Map from User Entity to UserResponse DTO
                .map(userMapper::toResponse) // 👈 Use the injected mapper instance
                // 4. Collect into a list
                .toList(); // Using Java 16+ .toList()
        // If using Java 11/8, use .collect(Collectors.toList())

    }


    @Transactional(value = "transactionManager") // 👈 Specify the bean name
    public UserResponse addChildSeller(Long parentId, String childEmail) {
        User parent = userRepo.findById(parentId).orElseThrow(() -> new RuntimeException("User not found"));
        User child = userRepo.findByEmail(childEmail).orElseThrow(() -> new RuntimeException("User not found"));
        if (!child.isActive()) {
            throw new IllegalArgumentException("Seller is inactive");
        }

        if (parent.getId().equals(child.getId())) {
            throw new IllegalArgumentException("You cannot add yourself");
        }

        if (hierarchyRepo.existsByParentIdAndChildId(parentId, child.getId())) {
            throw new IllegalArgumentException("Already mapped with other");
        }
        hierarchyRepo.save(UserHierarchy.builder()
                .parent(parent)
                .child(child)
                .build());
        return userMapper.toResponse(child);
    }

    @Transactional(value = "transactionManager") // 👈 Specify the bean name
    public boolean removeChildSeller(Long parentId, Long childId) {
        try {
            hierarchyRepo.deleteByParentIdAndChildId(parentId, childId);
            return true; // Assume success if no exception occurred
        } catch (Exception e) {
            // Log error
            return false;
        }
    }


// Assuming User, UserResponse, and UserRechargedEvent classes/imports are defined

    @Transactional("transactionManager")
    public UserResponse rechargeSeller(Long parentId, Long childId, double amount) throws JsonProcessingException {

        if (amount <= 0) {
            throw new IllegalArgumentException("Recharge amount must be positive");
        }

        User parent = userRepo.findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("Parent not found"));

        User child = userRepo.findById(childId)
                .orElseThrow(() -> new IllegalArgumentException("Child not found"));

        if (parent.getBalance() < amount) {
            throw new IllegalArgumentException("Insufficient parent balance");
        }

        RechargeCalc calc = calculateRecharge(amount);

        // ───────── Balance Updates ─────────
        parent.setBalance(parent.getBalance() - amount);
        child.setBalance(child.getBalance() + calc.totalCredit());
        child.setTotal(child.getTotal() + amount);

        userRepo.saveAll(List.of(parent, child)); // 🚀 SINGLE FLUSH

        // ───────── Activity Logs ─────────
        Map<String, String> map = new HashMap<>();
        map.put("message","You recharged "+child.getFullName() +", with $"+amount);
        userActivityService.log(
                ActivityFactory.balanceSent(
                        parentId,
                        amount,
                        parent.getBalance(),
                        map

                )
        );

        Map<String, String> childMap = new HashMap<>();
        childMap.put("message",parent.getFullName() +", sent you $"+calc.totalCredit());
        userActivityService.log(
                ActivityFactory.balanceReceived(
                        childId,
                        calc.totalCredit(),
                        child.getBalance(),
                        childMap
                )
        );

        // ───────── Event Publishing ─────────
        eventPublisher.publish(
                UserRechargedEvent.builder().
                        tenantId(TenantContext.getTenantId())
                        .parentUserId(parentId)
                        .parentRechargeAmount(amount)
                        .parentPreviousAmount(parent.getBalance() + amount)
                        .parentCurrentAmount(parent.getBalance())
                        .parentTotalAmount(parent.getTotal())
                        .childUserId(childId)
                        .childRechargeAmount(calc.totalCredit())
                        .childPreviousAmount(child.getBalance()- calc.totalCredit())
                        .childCurrentAmount(child.getBalance())
                        .childTotalAmount(child.getTotal())
                        .build()
        );

        return userMapper.toResponse(child);
    }


    public String getAvatar(User user) {
        return user.getAttributes()
                .stream()
                .filter(attr -> "avatar".equals(attr.getKey()))
                .map(UserAttribute::getValue)
                .findFirst()
                .orElse("https://res.cloudinary.com/dfcy1i11m/image/upload/v1763439718/avatars/avatar_1.jpg");
    }

    private double bonusPercentage(double amount) {
        if (amount >= 100) return 0.50;
        if (amount >= 50) return 0.30;
        return 0.25;
    }

    private RechargeCalc calculateRecharge(double amount) {
        double bonus = amount * bonusPercentage(amount);
        return new RechargeCalc(bonus, amount + bonus);
    }


}
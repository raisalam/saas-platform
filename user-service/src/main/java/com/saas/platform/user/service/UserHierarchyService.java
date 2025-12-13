package com.saas.platform.user.service;// UserHierarchyService.java

import com.saas.platform.common.events.DomainEventPublisher;
import com.saas.platform.db.TenantContext;
import com.saas.platform.user.domain.event.key.UserRechargedEvent;
import com.saas.platform.user.dto.UserResponse;
import com.saas.platform.user.entity.User;
import com.saas.platform.user.entity.UserAttribute;
import com.saas.platform.user.entity.UserHierarchy;
import com.saas.platform.user.mapper.UserMapper;
import com.saas.platform.user.repository.UserHierarchyRepository;
import com.saas.platform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserHierarchyService {

    private final UserHierarchyRepository hierarchyRepo;
    private final UserRepository userRepo;
    private final UserMapper userMapper;
    private final DomainEventPublisher eventPublisher;


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
        User parent  = userRepo.findById(parentId).orElseThrow(()->new RuntimeException("User not found"));
        User child  = userRepo.findByEmail(childEmail).orElseThrow(()->new RuntimeException("User not found"));
       if(!child.isActive()){
           throw new RuntimeException("Seller is inactive");
       }

        if (hierarchyRepo.existsByParentIdAndChildId(parentId, child.getId())) {
            throw new RuntimeException("Already mapped with other");
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

    @Transactional(value = "transactionManager") // 👈 Specify the bean name
    public UserResponse rechargeSeller(Long parentUserId, Long childUserId, Double amount) {

        // --- 0. Initialization & Safety ---
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Recharge amount must be positive.");
        }

        // 1. Fetch Users
        User parent = userRepo.findById(parentUserId).orElseThrow(() -> new IllegalArgumentException("Parent not found"));
        User child = userRepo.findById(childUserId).orElseThrow(() -> new IllegalArgumentException("Child not found"));

        // --- State Variables Initialization (Ready to Use) ---

        // Parent Variables (Capturing initial state)
        final Double parentPreviousAmount = parent.getBalance();

        // Child Variables (Capturing initial state)
        final Double childPreviousAmount = child.getBalance();

        // 2. Define Bonus Percentage (Using Double)
        final double bonusPercentage;
        if (amount >= 100.0) {
            bonusPercentage = 0.50; // 50%
        } else if (amount >= 50.0) {
            bonusPercentage = 0.30; // 30%
        } else {
            bonusPercentage = 0.25; // 25%
        }

        // Calculate derived amounts (Using Double)
        final Double bonusAmount = amount * bonusPercentage;
        final Double totalAmountToCredit = amount + bonusAmount;

        // 3. Validation: Check Parent's Balance
        if (parent.getBalance() < amount) {
            throw new IllegalArgumentException("Parent has insufficient balance to initiate recharge.");
        }

        // 4. Perform Transaction (Update State)

        // Debit Parent
        parent.setBalance(parent.getBalance() - amount);
       // parent.setTotal(parent.getTotal() + amount); // Assuming parent total tracks funds spent
        userRepo.save(parent);

        // Credit Child
        child.setBalance(child.getBalance() + totalAmountToCredit);
        child.setTotal(child.getTotal() + amount); // Assuming child total tracks total sales/recharges
        userRepo.save(child);

        // --- Post-Transaction Variables (Ready to Use) ---

        // Parent Variables (Capturing final state)
        final Double parentCurrentAmount = parent.getBalance();
        System.out.println("================Parent balance after recharge========="+parentCurrentAmount);
        final Double parentTotalAmount = parent.getTotal(); // Final total amount

        // Child Variables (Capturing final state)
        final Double childCurrentAmount = child.getBalance();
        final Double childTotalAmount = child.getTotal(); // Final total amount

        // Timestamp
        final Instant timestamp = Instant.now();

        // 5. Publish Event (Using the ready variables)
        UserRechargedEvent event = UserRechargedEvent.builder()
                .tenantId(TenantContext.getTenantId())
                .parentUserId(parentUserId)
                .parentRechargeAmount(amount)
                .parentPreviousAmount(parentPreviousAmount)
                .parentCurrentAmount(parentCurrentAmount)
                .parentTotalAmount(parentTotalAmount) // Final Total
                .parentActive(parent.isActive())
                .parentRole(parent.getRole().getName())
                .childUserId(childUserId)
                .childRechargeAmount(totalAmountToCredit)
                .childPreviousAmount(childPreviousAmount)
                .childCurrentAmount(childCurrentAmount)
                .childTotalAmount(childTotalAmount) // Final Total
                .childActive(child.isActive())
                .childRole(child.getRole().getName())
                .timestamp(timestamp)
                .build();

        // Assuming you have an event publisher service
         eventPublisher.publish(event);

        // 6. Return response
        // String message = String.format("Recharge successful. Bonus applied: %.0f%% (%.2f)", bonusPercentage * 100, bonusAmount);
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
}
package com.saas.platform.user.controller;// UserHierarchyController.java

import com.saas.platform.user.dto.AddDeleteSellerDto;
import com.saas.platform.user.dto.UserResponse;
import com.saas.platform.user.service.UserHierarchyService;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/user-hierarchy")
@RequiredArgsConstructor
@Validated
public class UserHierarchyController {

    private final UserHierarchyService hierarchyService;


    // --- NEW / REPLACED POST ENDPOINTS ---


    // 5. ADD CHILD SELLER (Retention of old logic, but with a dedicated path)
    // POST /api/user/user-hierarchy/add-child
    @PostMapping("/add-child")
    public ResponseEntity<UserResponse> addChildSeller( @RequestBody @Validated AddDeleteSellerDto payload) {
        Long parentId = Long.valueOf(MDC.get("sellerId"));
       UserResponse response =  hierarchyService.addChildSeller(parentId, payload.getEmail());
        return ResponseEntity.ok(response);
    }
    @PostMapping("/recharge-user")
    public ResponseEntity<UserResponse> rechargeChildSeller(@RequestBody @Validated AddDeleteSellerDto payload) {
        // 1. Get the ID of the logged-in user (the parent doing the recharge)
        Long parentId = Long.valueOf(MDC.get("sellerId"));

        // 2. Call the service layer method
        UserResponse response = hierarchyService.rechargeSeller(
                parentId,
                payload.getSellerId(),
                payload.getAmount()
        );

        return ResponseEntity.ok(response);
    }

    // 6. REMOVE CHILD SELLER (Retention of old logic, with a dedicated POST path)
    // POST /api/user/user-hierarchy/remove-child
    // Changed from @DeleteMapping to @PostMapping as requested
    @PostMapping("/remove-child")
    public ResponseEntity<Boolean> removeChildSeller(
            @RequestBody @Validated AddDeleteSellerDto payload) {
        Long parentId = Long.valueOf(MDC.get("sellerId"));
        Long childId = payload.getSellerId();

        if (childId == null) {
            return ResponseEntity.badRequest().body(false);
        }

        hierarchyService.removeChildSeller(parentId, childId);
        return ResponseEntity.ok( hierarchyService.removeChildSeller(parentId, childId));
    }

    // 1. FETCH CHILD SELLERS (Uses Path Variable for parentId)
    // POST /api/user/user-hierarchy/{parentId}
    @PostMapping("/{parentId}")
    public ResponseEntity<List<UserResponse>> getChildSellers(@PathVariable Long parentId) {
        try {
            List<UserResponse> sellers = hierarchyService.getChildSellers(parentId);

            if (sellers.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            }

            return ResponseEntity.ok(sellers);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.emptyList());
        }
    }

}
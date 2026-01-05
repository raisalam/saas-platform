package com.saas.platform.catalog.controller;

import com.saas.platform.catalog.dto.KeyGenerationRequest;
import com.saas.platform.catalog.dto.KeyGenerationResponse;
import com.saas.platform.catalog.dto.*;
import com.saas.platform.catalog.service.SubscriptionKeyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/catalog/key")
@RequiredArgsConstructor
@Validated
public class KeyController {

    private final SubscriptionKeyService keyService;

    @PostMapping("/save")
    public ResponseEntity<KeyGenerationResponse> save(@Valid @RequestBody KeyGenerationRequest req) {
        System.out.println("===========================inside key save controller=======");
        System.out.println(req);

        return ResponseEntity.ok(keyService.generateKeys(req));

    }

    @PostMapping("/all")
    public ResponseEntity<KeyGenerationResponse> recentKeys() {
        System.out.println("===========================inside recent controller=======");
        return ResponseEntity.ok(keyService.fetchLastKeysForSeller());
    }
    @PostMapping("/report")
    public ResponseEntity<Map<String, Object>> getreport() {
        System.out.println("===========================inside recent controller=======");

        return ResponseEntity.ok(keyService.getSellerDashboard());

    }

}

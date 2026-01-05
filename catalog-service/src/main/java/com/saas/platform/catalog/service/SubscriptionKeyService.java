package com.saas.platform.catalog.service;


import com.saas.platform.catalog.client.UserClient;
import com.saas.platform.catalog.domain.event.key.KeyGeneratedEvent;
import com.saas.platform.catalog.dto.KeyGenerationRequest;
import com.saas.platform.catalog.dto.KeyGenerationResponse;
import com.saas.platform.catalog.dto.KeyItemResponse;
import com.saas.platform.catalog.entity.*;
import com.saas.platform.catalog.mapper.KeyMapper;
import com.saas.platform.catalog.repository.GameRepository;
import com.saas.platform.catalog.repository.OutboxEventRepository;
import com.saas.platform.catalog.repository.PlanRepository;
import com.saas.platform.catalog.repository.SubscriptionKeyRepository;
import com.saas.platform.catalog.util.AimkingKeyGenerator;
import com.saas.platform.common.events.DomainEventPublisher;
import com.saas.platform.db.TenantContext;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubscriptionKeyService {

    private final GameRepository gameRepo;
    private final PlanRepository planRepo;
    private final OutboxEventRepository outboxRepository;
    private final SubscriptionKeyRepository keyRepo;
    private final KeyMapper keyMapper;
    private final DomainEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;
    private final UserClient userClient;

    private Map<Long, Game> gameMap = new ConcurrentHashMap<>();
    private Map<Long, Plan> planMap = new ConcurrentHashMap<>();

    @Transactional(value = "transactionManager") // 👈 Specify the bean name
    public KeyGenerationResponse generateKeys(KeyGenerationRequest req) {
        Long sellerId = getSellerId();

        double balance = userClient.getBalance(TenantContext.getTenantId(), sellerId);

        loadGamesIfNeeded();
        loadPlansIfNeeded();

        double totalCost = calculateTotalCost(req, gameMap, planMap);
        if (balance < totalCost) {
            throw new RuntimeException("Insufficient balance. Need: " + totalCost + " | Available: " + balance);
        }
        List<SubscriptionKey> keysToSave = generateKeysToSave(req, sellerId);
        List<SubscriptionKey> savedKeys = keyRepo.saveAll(keysToSave);
        KeyGenerationResponse response = mapToResponse(savedKeys, planMap);

        KeyGeneratedEvent eventPayload = KeyGeneratedEvent.builder()
                .tenantId(TenantContext.getTenantId())
                .userId(sellerId)
                .balance(balance)
                .totalCost(totalCost)
                .keys(response)
                .correlationId(MDC.get("traceId"))
                .build();

        userClient.setBalance(TenantContext.getTenantId(), sellerId, (balance - totalCost));
        saveOutboxEvent(sellerId, eventPayload);
        eventPublisher.publish(eventPayload);

        return response;
    }

    public KeyGenerationResponse fetchLastKeysForSeller() {
        Long sellerId = getSellerId(); // same MDC usage as in generateKeys

        // Fetch last 20 keys for this seller
        List<SubscriptionKey> lastKeys = keyRepo.findTop20BySellerIdOrderByCreatedDateDesc(sellerId);

        // Fetch all plans needed to map keys to gameId
        loadGamesIfNeeded();
        loadPlansIfNeeded();
        return mapToResponse(lastKeys, planMap);
    }

// ──────────────── PRIVATE HELPERS ────────────────

    private Long getSellerId() {
        return Long.valueOf(MDC.get("sellerId"));
    }

    private Set<Long> extractGameIds(KeyGenerationRequest req) {
        return req.getCart().keySet().stream()
                .map(Long::valueOf)
                .collect(Collectors.toSet());
    }

    private Set<Long> extractPlanIds(KeyGenerationRequest req) {
        return req.getCart().values().stream()
                .flatMap(map -> map.keySet().stream().map(Long::valueOf))
                .collect(Collectors.toSet());
    }


    private double calculateTotalCost(KeyGenerationRequest req, Map<Long, Game> gameMap, Map<Long, Plan> planMap) {
        double totalCost = 0;

        for (Map.Entry<String, Map<String, Integer>> gameEntry : req.getCart().entrySet()) {
            Long gameId = Long.valueOf(gameEntry.getKey());
            Game game = Optional.ofNullable(gameMap.get(gameId))
                    .orElseThrow(() -> new RuntimeException("Game disabled or not found: " + gameId));

            for (Map.Entry<String, Integer> planEntry : gameEntry.getValue().entrySet()) {
                Long planId = Long.valueOf(planEntry.getKey());
                Integer qty = planEntry.getValue();
                System.out.println(planId + "===Going to search plan entry from map ====" + planMap);
                Plan plan = Optional.ofNullable(planMap.get(planId))
                        .orElseThrow(() -> new RuntimeException("Plan disabled or not found: " + planId));

                if (!plan.getGameId().equals(game.getId())) {
                    throw new RuntimeException("Plan " + planId + " does not belong to game " + gameId);
                }

                totalCost += plan.getPrice() * qty;
            }
        }
        return totalCost;
    }

    private List<SubscriptionKey> generateKeysToSave(KeyGenerationRequest req, Long sellerId) {
        List<SubscriptionKey> keys = new ArrayList<>();
        for (Map.Entry<String, Map<String, Integer>> gameEntry : req.getCart().entrySet()) {
            for (Map.Entry<String, Integer> planEntry : gameEntry.getValue().entrySet()) {
                Long planId = Long.valueOf(planEntry.getKey());
                Plan p = planMap.get(planId);
                int days = p.getDurationMinutes() / (24 * 60);
                Integer qty = planEntry.getValue();
                for (int i = 0; i < qty; i++) {
                    keys.add(SubscriptionKey.builder()
                            .keyValue(AimkingKeyGenerator.generate(days))
                            .planId(planId)
                            .sellerId(sellerId)
                            .isUsed(false)
                            .isBlocked(false)
                            .build());
                }
            }
        }
        return keys;
    }

    private KeyGenerationResponse mapToResponse(List<SubscriptionKey> savedKeys, Map<Long, Plan> planMap) {
        List<KeyItemResponse> responseList = new ArrayList<>();
        for (SubscriptionKey key : savedKeys) {
            Plan plan = planMap.get(key.getPlanId());
            responseList.add(keyMapper.toDto(key, plan.getGameId(), plan.getDurationMinutes()));
        }
        return KeyGenerationResponse.builder().keys(responseList).build();
    }


    private void saveOutboxEvent(Long sellerId, KeyGeneratedEvent eventPayload) {
        // Fetch current balance for the event payload
        String serializedPayload = convertToJson(eventPayload); // Implement this

        // Create and save the Outbox record in the SAME transaction
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .tenantId(TenantContext.getTenantId())
                .aggregateType("Seller") // e.g., Seller
                .aggregateId(sellerId.toString())
                .eventType("KeyGenerated")
                .payload(serializedPayload)
                .eventCorrelationId(eventPayload.getCorrelationId())
                .createdAt(LocalDateTime.now())
                .status("PENDING")
                .retryCount(0)
                .topic("keys.events") // Add the destination topic/queue name
                .build();

        outboxRepository.save(outboxEvent);
    }
    public Map<String, Object> getSellerDashboard() {
        Long sellerId = getSellerId();
        String userRole = MDC.get("userRole");
        boolean isAdmin = "ADMIN".equals(userRole);

        // Fetch data based on role
        List<ReportStatsProjection> stats = isAdmin ?
                keyRepo.getComprehensiveReport(null) :
                keyRepo.getComprehensiveReport(sellerId);

        LocalDate today = LocalDate.now();

        // 1. Find today's stats
        ReportStatsProjection todayStats = stats.stream()
                .filter(s -> s.getStatDate() != null && s.getStatDate().isEqual(today))
                .findFirst()
                .orElse(null);

        // 2. Aggregate totals & Prepare Trend List in one pass
        double totalGeneratedSum = 0;
        double totalUsedSum = 0;
        double totalSavingsSum = 0;

        List<Map<String, Object>> trendList = new ArrayList<>();

        for (ReportStatsProjection s : stats) {
            double gen = s.getTotalGenerated() != null ? s.getTotalGenerated() : 0.0;
            double used = s.getTotalUsed() != null ? s.getTotalUsed() : 0.0;
            double sav = s.getTotalDiscount() != null ? s.getTotalDiscount() : 0.0;

            totalGeneratedSum += gen;
            totalUsedSum += used;
            totalSavingsSum += sav;

            if (s.getStatDate() != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("date", s.getStatDate().toString());
                map.put("generated", gen);
                map.put("used", used);
                map.put("savings", sav);
                trendList.add(map);
            }
        }

        // 3. Build Final Response
        Map<String, Object> data = new HashMap<>();
        data.put("totalGeneratedToday", todayStats != null ? todayStats.getTotalGenerated() : 0.0);
        data.put("totalUsedToday", todayStats != null ? todayStats.getTotalUsed() : 0.0);
        data.put("last30Days", trendList);

        // Status Breakdown (for Pie Chart)
        data.put("groupedByDate", List.of(
                Map.of("status", "Generated", "count", totalGeneratedSum),
                Map.of("status", "Used", "count", totalUsedSum),
                Map.of("status", "Savings", "count", totalSavingsSum)
        ));

        // 4. Admin Enrichment logic
        if (isAdmin) {
            List<Map<String, Object>> rawStats = keyRepo.getAdminRawStats();

            Set<Long> sellerIds = rawStats.stream()
                    .map(s -> ((Number) s.get("sellerId")).longValue()) // Use Number to avoid cast exceptions
                    .collect(Collectors.toSet());

            // Batch call User Microservice
            Map<String, String> idToNameMap = userClient.getUsernamesBatch(sellerIds);
            List<Map<String, Object>> enrichedStats = rawStats.stream().map(stat -> {
                Map<String, Object> enriched = new HashMap<>(stat);
                String sIdStr = String.valueOf(stat.get("sellerId"));
                // FIXED: Use sId (the seller in the list) not sellerId (the admin)
                String name = idToNameMap.getOrDefault(sIdStr, "Seller " + sIdStr);                enriched.put("sellerName", name);

                // Rename 'statDate' to 'date' to match your Flutter model if necessary
                if(stat.containsKey("statDate")) {
                    enriched.put("date", stat.get("statDate").toString());
                }

                return enriched;
            }).collect(Collectors.toList());

            data.put("sellerStats", enrichedStats);
        }

        return data;
    }

    // Placeholder for JSON serialization
    private String convertToJson(Object object) {
        // Use Jackson ObjectMapper or similar to serialize the event object
        return objectMapper.writeValueAsString(object);
    }

    private void loadGamesIfNeeded() {
        if (gameMap.isEmpty()) {
            gameRepo.findAllByEnabled(true)
                    .forEach(g -> gameMap.put(g.getId(), g));
        }
    }

    private void loadPlansIfNeeded() {
        if (planMap.isEmpty()) {
            planRepo.findAllByEnabled(true)
                    .forEach(p -> planMap.put(p.getId(), p));
        }
    }

}
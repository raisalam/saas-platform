package com.saas.platform.user.service;

import com.saas.platform.user.dto.TodayStatsResponse;
import com.saas.platform.user.entity.UserActivity;
import com.saas.platform.user.repository.UserActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserActivityService {

    private final UserActivityRepository repository;

    @Transactional(value = "transactionManager") // 👈 Specify the bean name
    public void log(UserActivity activity) {
        repository.save(activity);
    }

    @Transactional(value = "transactionManager") // 👈 Specify the bean name
    public List<UserActivity> recentActivities(Long userId) {
        return repository.findTop20ByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(value = "transactionManager") // 👈 Specify the bean name
    public TodayStatsResponse todayStats() {
        return TodayStatsResponse.builder()
                .keysGenerated(repository.todayGeneratedKeys())
                .keysUsed(repository.todayUsedKeys())
                .totalSpent(repository.todayTotalSpent())
                .build();
    }
}

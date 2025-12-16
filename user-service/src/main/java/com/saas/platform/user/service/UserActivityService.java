package com.saas.platform.user.service;

import com.saas.platform.user.dto.TodayStatsResponse;
import com.saas.platform.user.dto.UserActivityDto;
import com.saas.platform.user.entity.UserActivity;
import com.saas.platform.user.mapper.UserActivityMapper;
import com.saas.platform.user.repository.UserActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserActivityService {

    private final UserActivityRepository repository;
    private final UserActivityMapper mapper;


    @Transactional(value = "transactionManager") // 👈 Specify the bean name
    public void log(UserActivity activity) {
        repository.save(activity);
    }

    @Transactional(value = "transactionManager", readOnly = true ) // 👈 Specify the bean name
    public List<UserActivityDto> recentActivities(Long userId) {
        return repository.findTop20ByUserIdOrderByCreatedAtDesc(userId).stream().map(mapper::toResponse).toList();
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

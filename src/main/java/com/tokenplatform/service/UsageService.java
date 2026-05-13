package com.tokenplatform.service;

import com.tokenplatform.model.UsageRecord;
import com.tokenplatform.model.User;
import com.tokenplatform.repository.UsageRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UsageService {

    private final UsageRecordRepository usageRepository;

    public UsageService(UsageRecordRepository usageRepository) {
        this.usageRepository = usageRepository;
    }

    @Transactional
    public UsageRecord recordUsage(User user, com.tokenplatform.model.ApiKey apiKey,
                                    String model, int inputTokens, int outputTokens,
                                    long costFen, String requestId) {
        UsageRecord record = new UsageRecord();
        record.setUser(user);
        record.setApiKey(apiKey);
        record.setModel(model);
        record.setInputTokens(inputTokens);
        record.setOutputTokens(outputTokens);
        record.setTotalTokens(inputTokens + outputTokens);
        record.setCostFen(costFen);
        record.setRequestId(requestId);
        return usageRepository.save(record);
    }

    public List<UsageRecord> getUserUsage(Long userId) {
        return usageRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<UsageRecord> getUserUsageBetween(Long userId, LocalDateTime start, LocalDateTime end) {
        return usageRepository.findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(userId, start, end);
    }

    public Long getUserTotalCost(Long userId) {
        Long cost = usageRepository.totalCostByUserId(userId);
        return cost != null ? cost : 0L;
    }

    public Long getUserCostSince(User user, LocalDateTime since) {
        Long cost = usageRepository.totalCostByUserSince(user, since);
        return cost != null ? cost : 0L;
    }

    public Long getTotalTokens() {
        Long tokens = usageRepository.totalTokensAll();
        return tokens != null ? tokens : 0L;
    }

    public long getTotalRequests() {
        return usageRepository.count();
    }

    public Map<String, Long> getUsageByModel(Long userId) {
        List<UsageRecord> records = usageRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return records.stream()
                .collect(Collectors.groupingBy(
                        UsageRecord::getModel,
                        Collectors.summingLong(r -> r.getTotalTokens().longValue())
                ));
    }
}

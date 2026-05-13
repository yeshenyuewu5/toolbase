package com.tokenplatform.repository;

import com.tokenplatform.model.UsageRecord;
import com.tokenplatform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UsageRecordRepository extends JpaRepository<UsageRecord, Long> {
    List<UsageRecord> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<UsageRecord> findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long userId, LocalDateTime start, LocalDateTime end);
    long countByUserId(Long userId);

    @Query("SELECT COALESCE(SUM(u.costFen), 0) FROM UsageRecord u WHERE u.user.id = ?1")
    Long totalCostByUserId(Long userId);

    @Query("SELECT COALESCE(SUM(u.costFen), 0) FROM UsageRecord u WHERE u.user = ?1 AND u.createdAt >= ?2")
    Long totalCostByUserSince(User user, LocalDateTime since);

    @Query("SELECT COALESCE(SUM(u.totalTokens), 0) FROM UsageRecord u")
    Long totalTokensAll();
}

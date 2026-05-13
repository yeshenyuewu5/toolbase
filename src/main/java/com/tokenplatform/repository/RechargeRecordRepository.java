package com.tokenplatform.repository;

import com.tokenplatform.model.RechargeRecord;
import com.tokenplatform.model.RechargeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RechargeRecordRepository extends JpaRepository<RechargeRecord, Long> {
    List<RechargeRecord> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<RechargeRecord> findByStatus(RechargeStatus status);
    long countByStatus(RechargeStatus status);
}

package com.tokenplatform.service;

import com.tokenplatform.model.*;
import com.tokenplatform.repository.RechargeRecordRepository;
import com.tokenplatform.repository.UsageRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class RechargeService {

    private final RechargeRecordRepository rechargeRepository;
    private final UserService userService;

    public RechargeService(RechargeRecordRepository rechargeRepository, UserService userService) {
        this.rechargeRepository = rechargeRepository;
        this.userService = userService;
    }

    @Transactional
    public RechargeRecord createRecharge(Long userId, Long amountFen, String paymentMethod, String remark) {
        User user = userService.getById(userId);

        RechargeRecord record = new RechargeRecord();
        record.setUser(user);
        record.setAmountFen(amountFen);
        record.setStatus(RechargeStatus.PENDING);
        record.setPaymentMethod(paymentMethod);
        record.setRemark(remark);
        return rechargeRepository.save(record);
    }

    @Transactional
    public void approveRecharge(Long rechargeId) {
        RechargeRecord record = rechargeRepository.findById(rechargeId)
                .orElseThrow(() -> new RuntimeException("Recharge record not found"));

        if (record.getStatus() != RechargeStatus.PENDING) {
            throw new RuntimeException("Recharge already processed");
        }

        record.setStatus(RechargeStatus.SUCCESS);
        record.setProcessedAt(LocalDateTime.now());
        rechargeRepository.save(record);

        userService.addBalance(record.getUser().getId(), record.getAmountFen());
    }

    @Transactional
    public void rejectRecharge(Long rechargeId) {
        RechargeRecord record = rechargeRepository.findById(rechargeId)
                .orElseThrow(() -> new RuntimeException("Recharge record not found"));

        record.setStatus(RechargeStatus.FAILED);
        record.setProcessedAt(LocalDateTime.now());
        rechargeRepository.save(record);
    }
}

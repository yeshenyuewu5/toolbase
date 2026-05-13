package com.tokenplatform.controller;

import com.tokenplatform.model.RechargeRecord;
import com.tokenplatform.model.User;
import com.tokenplatform.repository.RechargeRecordRepository;
import com.tokenplatform.service.RechargeService;
import com.tokenplatform.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/recharge")
public class RechargeController {

    private final UserService userService;
    private final RechargeService rechargeService;
    private final RechargeRecordRepository rechargeRepository;

    public RechargeController(UserService userService, RechargeService rechargeService,
                               RechargeRecordRepository rechargeRepository) {
        this.userService = userService;
        this.rechargeService = rechargeService;
        this.rechargeRepository = rechargeRepository;
    }

    @GetMapping
    public String rechargePage(Principal principal, Model model) {
        User user = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<RechargeRecord> rechargeHistory = rechargeRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("rechargeHistory", rechargeHistory);
        model.addAttribute("activePage", "recharge");
        return "recharge/index";
    }

    @PostMapping("/submit")
    public String submitRecharge(Principal principal,
                                  @RequestParam Long amount,
                                  @RequestParam String paymentMethod,
                                  @RequestParam(required = false) String remark) {
        User user = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        rechargeService.createRecharge(user.getId(), amount, paymentMethod, remark);
        return "redirect:/recharge?submitted";
    }
}

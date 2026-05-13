package com.tokenplatform.controller;

import com.tokenplatform.model.*;
import com.tokenplatform.repository.RechargeRecordRepository;
import com.tokenplatform.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final RechargeService rechargeService;
    private final RechargeRecordRepository rechargeRepository;
    private final UsageService usageService;

    public AdminController(UserService userService, RechargeService rechargeService,
                            RechargeRecordRepository rechargeRepository,
                            UsageService usageService) {
        this.userService = userService;
        this.rechargeService = rechargeService;
        this.rechargeRepository = rechargeRepository;
        this.usageService = usageService;
    }

    @GetMapping
    public String adminDashboard(Model model) {
        long userCount = userService.countUsers();
        long totalRequests = usageService.getTotalRequests();
        long totalTokens = usageService.getTotalTokens();
        long pendingRecharges = rechargeRepository.countByStatus(RechargeStatus.PENDING);

        model.addAttribute("userCount", userCount);
        model.addAttribute("totalRequests", totalRequests);
        model.addAttribute("totalTokens", totalTokens);
        model.addAttribute("pendingRecharges", pendingRecharges);
        model.addAttribute("activePage", "admin");
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.findAll());
        model.addAttribute("activePage", "admin");
        return "admin/users";
    }

    @GetMapping("/recharges")
    public String listRecharges(Model model) {
        List<RechargeRecord> pending = rechargeRepository.findByStatus(RechargeStatus.PENDING);
        model.addAttribute("pendingRecharges", pending);
        model.addAttribute("activePage", "admin");
        return "admin/recharges";
    }

    @PostMapping("/recharges/approve/{id}")
    public String approveRecharge(@PathVariable Long id) {
        rechargeService.approveRecharge(id);
        return "redirect:/admin/recharges";
    }

    @PostMapping("/recharges/reject/{id}")
    public String rejectRecharge(@PathVariable Long id) {
        rechargeService.rejectRecharge(id);
        return "redirect:/admin/recharges";
    }
}

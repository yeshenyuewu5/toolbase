package com.tokenplatform.controller;

import com.tokenplatform.model.*;
import com.tokenplatform.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final UserService userService;
    private final ApiKeyService apiKeyService;
    private final UsageService usageService;

    public DashboardController(UserService userService, ApiKeyService apiKeyService,
                                UsageService usageService) {
        this.userService = userService;
        this.apiKeyService = apiKeyService;
        this.usageService = usageService;
    }

    @GetMapping
    public String dashboard(Principal principal, Model model) {
        User user = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<ApiKey> apiKeys = apiKeyService.findByUserId(user.getId());
        List<UsageRecord> recentUsage = usageService.getUserUsage(user.getId());
        if (recentUsage.size() > 20) {
            recentUsage = recentUsage.subList(0, 20);
        }
        Long totalCost = usageService.getUserTotalCost(user.getId());
        Map<String, Long> usageByModel = usageService.getUsageByModel(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("apiKeys", apiKeys);
        model.addAttribute("recentUsage", recentUsage);
        model.addAttribute("totalCost", totalCost);
        model.addAttribute("usageByModel", usageByModel);
        model.addAttribute("activePage", "dashboard");

        return "dashboard/index";
    }

    @PostMapping("/apikey/create")
    public String createApiKey(Principal principal, @RequestParam(required = false) String name) {
        User user = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        apiKeyService.createApiKey(user, name != null ? name : "Default Key");
        return "redirect:/dashboard";
    }

    @PostMapping("/apikey/toggle/{id}")
    public String toggleApiKey(Principal principal, @PathVariable Long id) {
        User user = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        apiKeyService.toggleEnabled(id, user.getId());
        return "redirect:/dashboard";
    }

    @PostMapping("/apikey/delete/{id}")
    public String deleteApiKey(Principal principal, @PathVariable Long id) {
        User user = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        apiKeyService.deleteKey(id, user.getId());
        return "redirect:/dashboard";
    }
}

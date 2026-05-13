package com.tokenplatform.blog.controller;

import com.tokenplatform.blog.entity.BlogPost;
import com.tokenplatform.blog.repository.BlogPostRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/blog")
public class AdminBlogController {

    private final BlogPostRepository postRepo;

    public AdminBlogController(BlogPostRepository postRepo) {
        this.postRepo = postRepo;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("posts", postRepo.findByPublishedTrueOrderByCreatedAtDesc());
        model.addAttribute("activePage", "admin");
        model.addAttribute("section", "blog");
        return "blog/admin/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("post", new BlogPost());
        model.addAttribute("activePage", "admin");
        return "blog/admin/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute BlogPost post,
                        @RequestParam(required = false) String action) {
        if (post.getId() != null) {
            BlogPost existing = postRepo.findById(post.getId()).orElse(null);
            if (existing != null) {
                post.setCreatedAt(existing.getCreatedAt());
                post.setViewCount(existing.getViewCount());
            }
        }
        if (post.getSlug() == null || post.getSlug().isEmpty()) {
            post.setSlug(post.getTitle().toLowerCase()
                    .replaceAll("[^a-z0-9\\s]", "").replaceAll("\\s+", "-")
                    .replaceAll("-+", "-").replaceAll("^-|-$", ""));
        }
        if (post.getAuthor() == null) post.setAuthor("ToolBase");
        post.setPublished(true);
        postRepo.save(post);
        return "redirect:/admin/blog";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        Optional<BlogPost> post = postRepo.findById(id);
        if (post.isEmpty()) return "redirect:/admin/blog";
        model.addAttribute("post", post.get());
        model.addAttribute("activePage", "admin");
        return "blog/admin/form";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        postRepo.deleteById(id);
        return "redirect:/admin/blog";
    }

    @PostMapping("/toggle-ad/{id}")
    public String toggleAd(@PathVariable Long id) {
        postRepo.findById(id).ifPresent(p -> {
            p.setAdEnabled(!p.getAdEnabled());
            postRepo.save(p);
        });
        return "redirect:/admin/blog";
    }

    @GetMapping("/analytics")
    public String analytics(Model model) {
        List<BlogPost> allPosts = postRepo.findByPublishedTrueOrderByCreatedAtDesc();
        
        long totalPosts = allPosts.size();
        long totalViews = allPosts.stream().mapToLong(BlogPost::getViewCount).sum();
        long adsEnabled = allPosts.stream().filter(BlogPost::getAdEnabled).count();
        
        // Top 10 most viewed
        List<BlogPost> topViewed = allPosts.stream()
                .sorted((a,b) -> b.getViewCount().compareTo(a.getViewCount()))
                .limit(10).collect(Collectors.toList());
        
        // Posts by category
        Map<String, Long> byCategory = allPosts.stream()
                .collect(Collectors.groupingBy(BlogPost::getCategory, Collectors.counting()));
        
        // Recent 7 days
        long lastWeek = allPosts.stream()
                .filter(p -> p.getCreatedAt().isAfter(LocalDateTime.now().minusDays(7)))
                .count();
        
        model.addAttribute("totalPosts", totalPosts);
        model.addAttribute("totalViews", totalViews);
        model.addAttribute("adsEnabled", adsEnabled);
        model.addAttribute("adsDisabled", totalPosts - adsEnabled);
        model.addAttribute("topViewed", topViewed);
        model.addAttribute("byCategory", byCategory);
        model.addAttribute("lastWeek", lastWeek);
        model.addAttribute("activePage", "admin");
        return "blog/admin/analytics";
    }
}

package com.tokenplatform.blog.controller;

import com.tokenplatform.blog.entity.BlogPost;
import com.tokenplatform.blog.repository.BlogPostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/blog")
public class BlogController {

    private final BlogPostRepository postRepo;

    public BlogController(BlogPostRepository postRepo) {
        this.postRepo = postRepo;
    }

    @GetMapping
    public String index(@RequestParam(defaultValue = "0") int page,
                         @RequestParam(required = false) String category,
                         Model model, Principal principal) {
        PageRequest pr = PageRequest.of(page, 12);
        Page<BlogPost> posts;
        if (category != null && !category.isEmpty()) {
            posts = postRepo.findByPublishedTrueAndCategoryOrderByCreatedAtDesc(category, pr);
            model.addAttribute("currentCategory", category);
        } else {
            posts = postRepo.findByPublishedTrueOrderByCreatedAtDesc(pr);
        }

        List<BlogPost> popular = postRepo.findTop6ByPublishedTrueOrderByViewCountDesc();
        List<String> categories = postRepo.findDistinctCategoryByPublishedTrue();

        model.addAttribute("posts", posts);
        model.addAttribute("popular", popular);
        model.addAttribute("categories", categories);
        model.addAttribute("activePage", "blog");
        model.addAttribute("isAdmin", principal != null && hasAdminRole(principal));
        return "blog/index";
    }

    @GetMapping("/{slug}")
    public String post(@PathVariable String slug, Model model, HttpServletRequest request, Principal principal) {
        Optional<BlogPost> opt = postRepo.findBySlugAndPublishedTrue(slug);
        if (opt.isEmpty()) return "redirect:/blog";

        BlogPost post = opt.get();
        post.setViewCount(post.getViewCount() + 1);
        postRepo.save(post);

        List<BlogPost> related = postRepo.findTop6ByPublishedTrueOrderByViewCountDesc();

        model.addAttribute("post", post);
        model.addAttribute("related", related);
        model.addAttribute("activePage", "blog");
        model.addAttribute("isAdmin", principal != null && hasAdminRole(principal));
        return "blog/post";
    }

    private boolean hasAdminRole(Principal principal) {
        if (principal instanceof org.springframework.security.core.Authentication) {
            return ((org.springframework.security.core.Authentication) principal).getAuthorities()
                    .stream().anyMatch(a -> a.getAuthority().equals("ADMIN"));
        }
        return false;
    }
}

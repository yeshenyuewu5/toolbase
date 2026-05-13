package com.tokenplatform.blog.repository;

import com.tokenplatform.blog.entity.BlogPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {
    Optional<BlogPost> findBySlugAndPublishedTrue(String slug);
    Optional<BlogPost> findBySlug(String slug);
    Page<BlogPost> findByPublishedTrueOrderByCreatedAtDesc(Pageable pageable);
    Page<BlogPost> findByPublishedTrueAndCategoryOrderByCreatedAtDesc(String category, Pageable pageable);
    List<BlogPost> findTop6ByPublishedTrueOrderByViewCountDesc();
    long countByPublishedTrue();
    List<BlogPost> findByPublishedTrueOrderByCreatedAtDesc();

    @Query("SELECT DISTINCT p.category FROM BlogPost p WHERE p.published = true ORDER BY p.category")
    List<String> findDistinctCategoryByPublishedTrue();
}

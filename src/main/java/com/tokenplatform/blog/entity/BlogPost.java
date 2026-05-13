package com.tokenplatform.blog.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "blog_posts")
public class BlogPost {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 500)
    private String title;
    @Column(unique = true, nullable = false, length = 500)
    private String slug;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    @Column(length = 500)
    private String excerpt;
    @Column(length = 100)
    private String category;
    @Column(length = 500)
    private String keywords;
    private String tags;
    @Column(length = 100)
    private String author = "ToolBase";
    @Column(length = 500)
    private String featuredImage;
    private Boolean adEnabled = true;
    private Boolean adBeforeContent = true;
    private Boolean adAfterContent = true;
    private Boolean adMidContent = false;
    private Boolean published = false;
    private Integer viewCount = 0;
    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() { createdAt = LocalDateTime.now(); updatedAt = LocalDateTime.now(); }
    @PreUpdate
    void onUpdate() { updatedAt = LocalDateTime.now(); }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getExcerpt() { return excerpt; }
    public void setExcerpt(String excerpt) { this.excerpt = excerpt; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getKeywords() { return keywords; }
    public void setKeywords(String keywords) { this.keywords = keywords; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getFeaturedImage() { return featuredImage; }
    public void setFeaturedImage(String featuredImage) { this.featuredImage = featuredImage; }
    public Boolean getAdEnabled() { return adEnabled; }
    public void setAdEnabled(Boolean adEnabled) { this.adEnabled = adEnabled; }
    public Boolean getAdBeforeContent() { return adBeforeContent; }
    public void setAdBeforeContent(Boolean adBeforeContent) { this.adBeforeContent = adBeforeContent; }
    public Boolean getAdAfterContent() { return adAfterContent; }
    public void setAdAfterContent(Boolean adAfterContent) { this.adAfterContent = adAfterContent; }
    public Boolean getAdMidContent() { return adMidContent; }
    public void setAdMidContent(Boolean adMidContent) { this.adMidContent = adMidContent; }
    public Boolean getPublished() { return published; }
    public void setPublished(Boolean published) { this.published = published; }
    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String[] getTagArray() {
        if (tags == null || tags.isEmpty()) return new String[0];
        return tags.split(",");
    }
}

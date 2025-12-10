package com.zstu.math.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "positive_review")
public class PositiveReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "positive_id")
    private Long positiveId;

    @Column(length = 50)
    private String reviewer;

    @Column(name = "review_opinion", length = 500)
    private String reviewOpinion;

    @Column(name = "review_result", length = 20)
    private String reviewResult; // 属实/不确定/打回

    @Column(name = "review_time")
    private LocalDateTime reviewTime;

    public PositiveReview() {
        this.reviewTime = LocalDateTime.now();
    }

    public PositiveReview(Long positiveId, String reviewer) {
        this();
        this.positiveId = positiveId;
        this.reviewer = reviewer;
    }

    // Getter和Setter方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPositiveId() { return positiveId; }
    public void setPositiveId(Long positiveId) { this.positiveId = positiveId; }

    public String getReviewer() { return reviewer; }
    public void setReviewer(String reviewer) { this.reviewer = reviewer; }

    public String getReviewOpinion() { return reviewOpinion; }
    public void setReviewOpinion(String reviewOpinion) { this.reviewOpinion = reviewOpinion; }

    public String getReviewResult() { return reviewResult; }
    public void setReviewResult(String reviewResult) { this.reviewResult = reviewResult; }

    public LocalDateTime getReviewTime() { return reviewTime; }
    public void setReviewTime(LocalDateTime reviewTime) { this.reviewTime = reviewTime; }
}
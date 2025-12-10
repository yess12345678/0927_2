package com.zstu.math.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "drug_reaction_positive")
public class DrugReactionPositive {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "drug_name", length = 100)
    private String drugName;

    @Column(name = "reaction_name", length = 100)
    private String reactionName;

    @Column(name = "is_positive")
    private Boolean isPositive;

    @Column(name = "confidence")
    private Double confidence;

    @Column(name = "prr_value")  // 新增字段
    private Double prrValue;

    @Column(name = "occurrence_count")  // 新增字段
    private Integer occurrenceCount;

    @Column(name = "algorithm", length = 50)
    private String algorithm;

    @Column(name = "analysis_time")
    private LocalDateTime analysisTime;

    public DrugReactionPositive() {
        this.analysisTime = LocalDateTime.now();
    }

    public DrugReactionPositive(String drugName, String reactionName) {
        this();
        this.drugName = drugName;
        this.reactionName = reactionName;
    }

    // Getter和Setter方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDrugName() { return drugName; }
    public void setDrugName(String drugName) { this.drugName = drugName; }

    public String getReactionName() { return reactionName; }
    public void setReactionName(String reactionName) { this.reactionName = reactionName; }

    public Boolean getIsPositive() { return isPositive; }
    public void setIsPositive(Boolean isPositive) { this.isPositive = isPositive; }

    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }

    // 新增的getter和setter方法
    public Double getPrrValue() { return prrValue; }
    public void setPrrValue(Double prrValue) { this.prrValue = prrValue; }

    public Integer getOccurrenceCount() { return occurrenceCount; }
    public void setOccurrenceCount(Integer occurrenceCount) { this.occurrenceCount = occurrenceCount; }

    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }

    public LocalDateTime getAnalysisTime() { return analysisTime; }
    public void setAnalysisTime(LocalDateTime analysisTime) { this.analysisTime = analysisTime; }
}
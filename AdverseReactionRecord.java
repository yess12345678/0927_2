package com.zstu.math.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "adverse_reaction_record")
public class AdverseReactionRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "reaction_time", nullable = false)
    private LocalDate reactionTime;

    @Column(length = 50)
    private String duration;

    @Column(length = 50)
    private String timing;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "reaction_symptoms", joinColumns = @JoinColumn(name = "reaction_id"))
    @Column(name = "symptom", length = 100)
    private List<String> symptoms = new ArrayList<>();

    @Column(name = "reaction_type", length = 100)
    private String reactionType;

    @Column(name = "severity", length = 20)
    private String severity;

    @Column(name = "outcome", length = 50)
    private String outcome;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "location", length = 200)
    private String location;

    @Column(name = "related_drug", length = 100)
    private String relatedDrug;

    @Column(name = "created_time")
    private LocalDateTime createdTime = LocalDateTime.now();

    public AdverseReactionRecord() {}

    // Getter和Setter方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public LocalDate getReactionTime() { return reactionTime; }
    public void setReactionTime(LocalDate reactionTime) { this.reactionTime = reactionTime; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public String getTiming() { return timing; }
    public void setTiming(String timing) { this.timing = timing; }

    public List<String> getSymptoms() { return symptoms; }
    public void setSymptoms(List<String> symptoms) { this.symptoms = symptoms; }

    public String getReactionType() { return reactionType; }
    public void setReactionType(String reactionType) { this.reactionType = reactionType; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getOutcome() { return outcome; }
    public void setOutcome(String outcome) { this.outcome = outcome; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getRelatedDrug() { return relatedDrug; }
    public void setRelatedDrug(String relatedDrug) { this.relatedDrug = relatedDrug; }

    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }

    public void addSymptom(String symptom) {
        this.symptoms.add(symptom);
    }
}
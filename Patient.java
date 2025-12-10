package com.zstu.math.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "patient")
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_code", nullable = false, unique = true, length = 20)
    private String patientCode;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 10)
    private String gender;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(length = 100)
    private String region;

    // 急加载
    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<MedicationRecord> medicationRecords = new ArrayList<>();

    // 急加载
    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<AdverseReactionRecord> adverseReactionRecords = new ArrayList<>();

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    public Patient() {
        this.createdTime = LocalDateTime.now();
        this.updatedTime = LocalDateTime.now();
    }

    // Getter和Setter方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPatientCode() { return patientCode; }
    public void setPatientCode(String patientCode) {
        this.patientCode = patientCode;
        this.updatedTime = LocalDateTime.now();
    }

    public String getName() { return name; }
    public void setName(String name) {
        this.name = name;
        this.updatedTime = LocalDateTime.now();
    }

    public String getGender() { return gender; }
    public void setGender(String gender) {
        this.gender = gender;
        this.updatedTime = LocalDateTime.now();
    }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
        this.updatedTime = LocalDateTime.now();
    }

    public String getRegion() { return region; }
    public void setRegion(String region) {
        this.region = region;
        this.updatedTime = LocalDateTime.now();
    }

    public List<MedicationRecord> getMedicationRecords() { return medicationRecords; }
    public void setMedicationRecords(List<MedicationRecord> medicationRecords) {
        this.medicationRecords = medicationRecords;
    }

    public List<AdverseReactionRecord> getAdverseReactionRecords() { return adverseReactionRecords; }
    public void setAdverseReactionRecords(List<AdverseReactionRecord> adverseReactionRecords) {
        this.adverseReactionRecords = adverseReactionRecords;
    }

    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }

    public LocalDateTime getUpdatedTime() { return updatedTime; }
    public void setUpdatedTime(LocalDateTime updatedTime) { this.updatedTime = updatedTime; }

    // 便捷方法
    public void addMedicationRecord(MedicationRecord record) {
        medicationRecords.add(record);
        record.setPatient(this);
        this.updatedTime = LocalDateTime.now();
    }

    public void addAdverseReactionRecord(AdverseReactionRecord record) {
        adverseReactionRecords.add(record);
        record.setPatient(this);
        this.updatedTime = LocalDateTime.now();
    }
}
package com.zstu.math.entity.vo;

import java.time.LocalDate;

public class PatientSummaryVO {
    private String patientCode;
    private LocalDate startMedicationDate;
    private LocalDate endMedicationDate;
    private String allDrugs;
    private String allReactions;

    public PatientSummaryVO() {}

    // Getter和Setter
    public String getPatientCode() { return patientCode; }
    public void setPatientCode(String patientCode) { this.patientCode = patientCode; }

    public LocalDate getStartMedicationDate() { return startMedicationDate; }
    public void setStartMedicationDate(LocalDate startMedicationDate) { this.startMedicationDate = startMedicationDate; }

    public LocalDate getEndMedicationDate() { return endMedicationDate; }
    public void setEndMedicationDate(LocalDate endMedicationDate) { this.endMedicationDate = endMedicationDate; }

    public String getAllDrugs() { return allDrugs; }
    public void setAllDrugs(String allDrugs) { this.allDrugs = allDrugs; }

    public String getAllReactions() { return allReactions; }
    public void setAllReactions(String allReactions) { this.allReactions = allReactions; }
}
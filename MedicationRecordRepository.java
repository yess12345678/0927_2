package com.zstu.math.dao;

import com.zstu.math.entity.MedicationRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MedicationRecordRepository extends JpaRepository<MedicationRecord, Long> {

    // 基础查询
    List<MedicationRecord> findByPatientPatientCode(String patientCode);
    List<MedicationRecord> findByDrugContaining(String drugName);

    // 统计查询
    @Query("SELECT DISTINCT m.drug FROM MedicationRecord m")
    List<String> findAllDrugs();

    @Query("SELECT m.drug, COUNT(m) FROM MedicationRecord m GROUP BY m.drug ORDER BY COUNT(m) DESC")
    List<Object[]> countByDrug();

    @Query("SELECT COUNT(m) FROM MedicationRecord m")
    long countTotalMedications();

    // 分页查询
    Page<MedicationRecord> findByPatientId(Long patientId, Pageable pageable);
    Page<MedicationRecord> findByDrugContaining(String drugName, Pageable pageable);
    Page<MedicationRecord> findByMedicationTimeAfter(LocalDate time, Pageable pageable);

    @Query("SELECT DISTINCT m.patient.id FROM MedicationRecord m WHERE m.drug = :drugName")
    List<Long> findPatientIdsByDrug(@Param("drugName") String drugName);
}
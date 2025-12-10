package com.zstu.math.dao;

import com.zstu.math.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    // 基础查询
    Optional<Patient> findByPatientCode(String patientCode);

    // 新增：患者编号模糊查询
    Page<Patient> findByPatientCodeContaining(String patientCode, Pageable pageable);

    boolean existsByPatientCode(String patientCode);

    // 分页查询
    Page<Patient> findAll(Pageable pageable);

    // 统计查询
    @Query("SELECT COUNT(p) FROM Patient p")
    long countTotalPatients();

    @Query("SELECT DISTINCT p.region FROM Patient p")
    List<String> findAllRegions();

    @Query("SELECT p FROM Patient p LEFT JOIN FETCH p.medicationRecords WHERE p.patientCode = :patientCode")
    Optional<Patient> findByPatientCodeWithMedications(@Param("patientCode") String patientCode);

    // 批量查询（统计用）
    List<Patient> findByIdIn(List<Long> patientIds);
    List<Patient> findByIdInAndGender(List<Long> patientIds, String gender);
    List<Patient> findByIdInAndRegion(List<Long> patientIds, String region);

    @Query("SELECT p FROM Patient p WHERE p.id IN :patientIds " +
            "AND TIMESTAMPDIFF(YEAR, p.birthDate, CURRENT_DATE) BETWEEN :startAge AND :endAge")
    List<Patient> findByIdInAndAgeBetween(@Param("patientIds") List<Long> patientIds,
                                          @Param("startAge") int startAge,
                                          @Param("endAge") int endAge);
}
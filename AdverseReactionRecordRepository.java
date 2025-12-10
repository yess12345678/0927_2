package com.zstu.math.dao;

import com.zstu.math.entity.AdverseReactionRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AdverseReactionRecordRepository extends JpaRepository<AdverseReactionRecord, Long> {

    // 统计查询
    @Query("SELECT COUNT(ar) FROM AdverseReactionRecord ar")
    long countTotalReactions();

    @Query("SELECT ar.timing, COUNT(ar) FROM AdverseReactionRecord ar GROUP BY ar.timing ORDER BY COUNT(ar) DESC")
    List<Object[]> countByTiming();

    @Query("SELECT s, COUNT(ar) FROM AdverseReactionRecord ar JOIN ar.symptoms s GROUP BY s ORDER BY COUNT(ar) DESC")
    List<Object[]> countBySymptom();

    @Query("SELECT COUNT(DISTINCT ar.patient) FROM AdverseReactionRecord ar")
    long countPatientsWithReactions();

    // 分页查询
    Page<AdverseReactionRecord> findByPatientId(Long patientId, Pageable pageable);
    Page<AdverseReactionRecord> findByTimingContaining(String timing, Pageable pageable);
    Page<AdverseReactionRecord> findByReactionTimeAfter(LocalDate time, Pageable pageable);

    // 增强查询方法
    Page<AdverseReactionRecord> findByPatientPatientCodeContaining(String patientCode, Pageable pageable);

    @Query("SELECT ar FROM AdverseReactionRecord ar WHERE :symptom MEMBER OF ar.symptoms")
    Page<AdverseReactionRecord> findBySymptom(@Param("symptom") String symptom, Pageable pageable);

    Page<AdverseReactionRecord> findByReactionTimeBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

    // 地理位置查询
    @Query("SELECT ar FROM AdverseReactionRecord ar WHERE ar.latitude IS NOT NULL AND ar.longitude IS NOT NULL")
    List<AdverseReactionRecord> findRecordsWithLocation();

    // 时间序列统计
    @Query("SELECT FUNCTION('DATE_FORMAT', ar.reactionTime, '%Y-%m'), COUNT(ar) " +
            "FROM AdverseReactionRecord ar " +
            "GROUP BY FUNCTION('DATE_FORMAT', ar.reactionTime, '%Y-%m') " +
            "ORDER BY FUNCTION('DATE_FORMAT', ar.reactionTime, '%Y-%m')")
    List<Object[]> countMonthlyReactions();

    // 药品-不良反应组合查询
    @Query("SELECT ar.relatedDrug, ar.reactionType, COUNT(ar) " +
            "FROM AdverseReactionRecord ar " +
            "WHERE ar.relatedDrug IS NOT NULL " +
            "GROUP BY ar.relatedDrug, ar.reactionType " +
            "ORDER BY COUNT(ar) DESC")
    List<Object[]> findDrugReactionCombinations();

    // 特定药品和反应的定位数据
    @Query("SELECT ar FROM AdverseReactionRecord ar " +
            "WHERE ar.relatedDrug = :drugName AND ar.reactionType = :reactionType " +
            "AND ar.latitude IS NOT NULL AND ar.longitude IS NOT NULL")
    List<AdverseReactionRecord> findByDrugAndReactionWithLocation(
            @Param("drugName") String drugName,
            @Param("reactionType") String reactionType);
}
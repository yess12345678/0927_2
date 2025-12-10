package com.zstu.math.dao;

import com.zstu.math.entity.DrugReactionPositive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DrugReactionPositiveRepository extends JpaRepository<DrugReactionPositive, Long> {

    List<DrugReactionPositive> findByIsPositiveTrue();

    @Query("SELECT DISTINCT d.drugName FROM DrugReactionPositive d WHERE d.isPositive = true")
    List<String> findPositiveDrugs();

    @Query("SELECT d FROM DrugReactionPositive d WHERE d.drugName LIKE %:keyword% OR d.reactionName LIKE %:keyword%")
    List<DrugReactionPositive> findByKeyword(@Param("keyword") String keyword);
}
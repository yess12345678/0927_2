package com.zstu.math.dao;

import com.zstu.math.entity.PositiveReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PositiveReviewRepository extends JpaRepository<PositiveReview, Long> {

    List<PositiveReview> findByPositiveId(Long positiveId);

    @Query("SELECT pr FROM PositiveReview pr WHERE pr.positiveId IN :positiveIds")
    List<PositiveReview> findByPositiveIds(@Param("positiveIds") List<Long> positiveIds);
}
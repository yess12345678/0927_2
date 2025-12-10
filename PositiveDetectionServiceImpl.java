package com.zstu.math.service.impl;

import com.zstu.math.dao.DrugReactionPositiveRepository;
import com.zstu.math.dao.PositiveReviewRepository;
import com.zstu.math.entity.DrugReactionPositive;
import com.zstu.math.entity.PositiveReview;
import com.zstu.math.entity.vo.DrugReactionStatVO;
import com.zstu.math.service.DrugReactionStatService;
import com.zstu.math.service.PositiveDetectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PositiveDetectionServiceImpl implements PositiveDetectionService {

    @Autowired
    private DrugReactionPositiveRepository positiveRepository;

    @Autowired
    private PositiveReviewRepository reviewRepository;

    @Autowired
    private DrugReactionStatService statService;

    @Override
    public void analyzePositiveReactions() {
        // 清空之前的阳性判定结果
        positiveRepository.deleteAll();

        // 使用PRR算法进行阳性判定
        List<DrugReactionStatVO> stats = statService.getDrugReactionStat(null);

        for (DrugReactionStatVO stat : stats) {
            // 简单阈值判定：出现次数≥3且置信度计算
            if (stat.getTotalCount() >= 3) {
                double confidence = calculateConfidence(stat);

                DrugReactionPositive positive = new DrugReactionPositive();
                positive.setDrugName(stat.getDrug());
                positive.setReactionName(stat.getSymptom());
                positive.setIsPositive(confidence > 0.6); // 置信度阈值
                positive.setConfidence(confidence);
                positive.setAlgorithm("PRR");
                positive.setAnalysisTime(LocalDateTime.now());

                positiveRepository.save(positive);
            }
        }
    }

    private double calculateConfidence(DrugReactionStatVO stat) {
        // 简化的置信度计算
        double baseConfidence = Math.min(stat.getTotalCount() / 10.0, 0.9);

        // 考虑性别分布的影响
        double genderBalance = 1.0 - Math.abs(stat.getMaleCount() - stat.getFemaleCount()) /
                (double) Math.max(stat.getTotalCount(), 1);

        return baseConfidence * genderBalance;
    }

    @Override
    public List<DrugReactionPositive> getPositiveResults() {
        return positiveRepository.findByIsPositiveTrue();
    }

    @Override
    public void saveReview(PositiveReview review) {
        review.setReviewTime(LocalDateTime.now());
        reviewRepository.save(review);
    }

    @Override
    public List<DrugReactionPositive> getPendingReview() {
        List<DrugReactionPositive> positives = positiveRepository.findByIsPositiveTrue();
        List<Long> positiveIds = positives.stream().map(DrugReactionPositive::getId).collect(Collectors.toList());

        if (positiveIds.isEmpty()) {
            return positives;
        }

        List<PositiveReview> reviews = reviewRepository.findByPositiveIds(positiveIds);

        // 返回未审核的记录
        return positives.stream()
                .filter(p -> reviews.stream().noneMatch(r -> r.getPositiveId().equals(p.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public List<DrugReactionPositive> searchPositiveResults(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getPositiveResults();
        }
        return positiveRepository.findByKeyword(keyword.trim());
    }
}
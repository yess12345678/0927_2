package com.zstu.math.service;

import com.zstu.math.entity.DrugReactionPositive;
import com.zstu.math.entity.PositiveReview;
import java.util.List;

public interface PositiveDetectionService {
    void analyzePositiveReactions();
    List<DrugReactionPositive> getPositiveResults();
    void saveReview(PositiveReview review);
    List<DrugReactionPositive> getPendingReview();
    List<DrugReactionPositive> searchPositiveResults(String keyword);
}
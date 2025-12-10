package com.zstu.math.service;

import com.zstu.math.dao.DrugReactionPositiveRepository;
import com.zstu.math.dao.AdverseReactionRecordRepository;
import com.zstu.math.dao.MedicationRecordRepository;
import com.zstu.math.dao.PatientRepository;
import com.zstu.math.entity.DrugReactionPositive;
import com.zstu.math.entity.AdverseReactionRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DataMiningService {

    @Autowired
    private AdverseReactionRecordRepository adverseReactionRecordRepository;

    @Autowired
    private MedicationRecordRepository medicationRecordRepository;

    @Autowired
    private DrugReactionPositiveRepository drugReactionPositiveRepository;

    @Autowired
    private PatientRepository patientRepository;

    // 每月执行阳性判定分析
    @Scheduled(cron = "0 0 2 1 * ?")
    public void analyzePositiveReactions() {
        System.out.println("[" + LocalDateTime.now() + "] 开始执行阳性判定分析...");

        // 清空之前的阳性判定结果
        drugReactionPositiveRepository.deleteAll();

        // 获取所有药品-不良反应组合
        List<Object[]> drugReactionCombinations = adverseReactionRecordRepository.findDrugReactionCombinations();

        int positiveCount = 0;
        for (Object[] combination : drugReactionCombinations) {
            String drugName = (String) combination[0];
            String reactionName = (String) combination[1];
            Long count = (Long) combination[2];

            // 使用PRR算法计算
            double prr = calculatePRR(drugName, reactionName);
            double confidence = calculateConfidence(drugName, reactionName, count);

            // 判定条件：PRR > 2 且 置信度 > 0.8 且 出现次数 >= 3
            if (prr > 2.0 && confidence > 0.8 && count >= 3) {
                DrugReactionPositive positive = new DrugReactionPositive();
                positive.setDrugName(drugName);
                positive.setReactionName(reactionName);
                positive.setIsPositive(true);
                positive.setConfidence(confidence);
                positive.setPrrValue(prr);
                positive.setOccurrenceCount(count.intValue());
                positive.setAlgorithm("PRR");
                positive.setAnalysisTime(LocalDateTime.now());

                drugReactionPositiveRepository.save(positive);
                positiveCount++;
            }
        }

        System.out.println("阳性判定分析完成，共发现 " + positiveCount + " 个阳性关联");
    }

    // 计算PRR（比例报告比）
    private double calculatePRR(String drugName, String reactionName) {
        try {
            // 计算使用该药品并出现该反应的患者数
            Long a = adverseReactionRecordRepository.findByDrugAndReactionWithLocation(drugName, reactionName).stream()
                    .map(record -> record.getPatient().getId())
                    .distinct()
                    .count();

            // 计算使用该药品但未出现该反应的患者数
            List<Long> drugPatientIds = medicationRecordRepository.findPatientIdsByDrug(drugName);
            Long b = drugPatientIds.size() - a;

            // 计算未使用该药品但出现该反应的患者数
            Long c = adverseReactionRecordRepository.findAll().stream()
                    .filter(record -> record.getReactionType() != null &&
                            record.getReactionType().equals(reactionName) &&
                            !drugPatientIds.contains(record.getPatient().getId()))
                    .map(record -> record.getPatient().getId())
                    .distinct()
                    .count();

            // 计算未使用该药品也未出现该反应的患者数（估算）
            Long totalPatients = patientRepository.countTotalPatients();
            Long d = totalPatients - a - b - c;

            if (a == 0 || c == 0 || (a + b) == 0 || (c + d) == 0) {
                return 0.0;
            }

            // PRR = (a/(a+b)) / (c/(c+d))
            return (a.doubleValue() / (a + b)) / (c.doubleValue() / (c + d));
        } catch (Exception e) {
            return 0.0;
        }
    }

    // 计算置信度
    private double calculateConfidence(String drugName, String reactionName, Long occurrenceCount) {
        double baseConfidence = Math.min(occurrenceCount / 10.0, 0.9);

        // 考虑报告完整性
        double completenessScore = calculateCompletenessScore(drugName, reactionName);

        return baseConfidence * completenessScore;
    }

    // 计算报告完整性得分
    private double calculateCompletenessScore(String drugName, String reactionName) {
        List<AdverseReactionRecord> records = adverseReactionRecordRepository.findByDrugAndReactionWithLocation(drugName, reactionName);
        if (records.isEmpty()) return 0.5;

        long completeRecords = records.stream()
                .filter(record -> record.getSeverity() != null &&
                        record.getOutcome() != null &&
                        record.getLatitude() != null &&
                        record.getLongitude() != null)
                .count();

        return (double) completeRecords / records.size();
    }

    // 获取阳性判定结果
    public List<DrugReactionPositive> getPositiveResults() {
        return drugReactionPositiveRepository.findByIsPositiveTrue();
    }

    // 根据关键词搜索阳性结果
    public List<DrugReactionPositive> searchPositiveResults(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getPositiveResults();
        }
        return drugReactionPositiveRepository.findByKeyword(keyword.trim());
    }

    // 获取地理位置数据用于地图显示
    public List<AdverseReactionRecord> getLocationDataForPositiveReaction(String drugName, String reactionName) {
        return adverseReactionRecordRepository.findByDrugAndReactionWithLocation(drugName, reactionName);
    }
}
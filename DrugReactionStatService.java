package com.zstu.math.service;

import com.zstu.math.dao.AdverseReactionRecordRepository;
import com.zstu.math.dao.MedicationRecordRepository;
import com.zstu.math.dao.PatientRepository;
import com.zstu.math.entity.AdverseReactionRecord;
import com.zstu.math.entity.Patient;
import com.zstu.math.entity.vo.DrugReactionStatVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DrugReactionStatService {
    @Autowired
    private MedicationRecordRepository medicationRepo;
    @Autowired
    private AdverseReactionRecordRepository reactionRepo;
    @Autowired
    private PatientRepository patientRepo;

    // 每周执行药品-不良反应统计
    @Scheduled(cron = "0 0 0 * * MON")
    public void generateWeeklyDrugReactionStats() {
        System.out.println("[" + LocalDate.now() + "] 开始执行每周药品-不良反应统计...");
        List<DrugReactionStatVO> stats = getDrugReactionStat(null);
        System.out.println("每周统计完成，共生成 " + stats.size() + " 条药品-不良反应统计记录");
    }

    // 药品-不良反应关联详细统计
    public List<DrugReactionStatVO> getDrugReactionStat(String drugName) {
        List<String> targetDrugs = new ArrayList<>();
        if (drugName != null && !drugName.isEmpty()) {
            targetDrugs.add(drugName);
        } else {
            targetDrugs = medicationRepo.findAllDrugs();
        }

        List<DrugReactionStatVO> stats = new ArrayList<>();
        for (String drug : targetDrugs) {
            // 获取该药品关联的所有患者ID
            List<Long> patientIds = medicationRepo.findPatientIdsByDrug(drug);
            if (patientIds.isEmpty()) continue;

            // 获取这些患者的所有不良反应 - 修正方法调用
            List<AdverseReactionRecord> allReactions = new ArrayList<>();
            for (Long patientId : patientIds) {
                // 使用无分页的方式获取所有记录
                Pageable pageable = Pageable.unpaged();
                List<AdverseReactionRecord> patientReactions = reactionRepo.findByPatientId(patientId, pageable).getContent();
                allReactions.addAll(patientReactions);
            }

            if (allReactions.isEmpty()) continue;

            // 按反应类型分组
            Map<String, Set<Long>> reactionPatientMap = new HashMap<>();
            for (AdverseReactionRecord reaction : allReactions) {
                if (reaction.getReactionType() != null) {
                    Long pId = reaction.getPatient().getId();
                    reactionPatientMap.computeIfAbsent(reaction.getReactionType(), k -> new HashSet<>()).add(pId);
                }
            }

            // 为每个反应类型创建统计VO
            for (Map.Entry<String, Set<Long>> entry : reactionPatientMap.entrySet()) {
                String reactionType = entry.getKey();
                Set<Long> symptomPIds = entry.getValue();
                List<Long> pIdList = new ArrayList<>(symptomPIds);

                // 修正方法调用
                List<Patient> patients = new ArrayList<>();
                for (Long patientId : pIdList) {
                    patientRepo.findById(patientId).ifPresent(patients::add);
                }

                // 统计核心数据
                DrugReactionStatVO vo = new DrugReactionStatVO();
                vo.setDrug(drug);
                vo.setSymptom(reactionType);
                vo.setTotalCount(symptomPIds.size());

                // 性别统计
                vo.setMaleCount((int) patients.stream().filter(p -> "男".equals(p.getGender())).count());
                vo.setFemaleCount((int) patients.stream().filter(p -> "女".equals(p.getGender())).count());

                // 年龄段统计
                Map<String, Integer> ageStats = calculateAgeGroupStats(patients);
                vo.setChildCount(ageStats.getOrDefault("child", 0));
                vo.setYouthCount(ageStats.getOrDefault("youth", 0));
                vo.setMiddleAgeCount(ageStats.getOrDefault("middle", 0));
                vo.setElderlyCount(ageStats.getOrDefault("elderly", 0));

                // 主要年龄段和地区
                vo.setMainAgeGroup(getMainAgeGroup(patients));
                vo.setMainRegion(getMainRegion(patients));

                stats.add(vo);
            }
        }

        return stats.stream()
                .sorted(Comparator.comparing(DrugReactionStatVO::getDrug)
                        .thenComparing(Comparator.comparing(DrugReactionStatVO::getTotalCount).reversed()))
                .collect(Collectors.toList());
    }

    // 计算年龄段统计
    private Map<String, Integer> calculateAgeGroupStats(List<Patient> patients) {
        Map<String, Integer> ageStats = new HashMap<>();
        ageStats.put("child", 0);
        ageStats.put("youth", 0);
        ageStats.put("middle", 0);
        ageStats.put("elderly", 0);

        for (Patient patient : patients) {
            if (patient.getBirthDate() != null) {
                int age = Period.between(patient.getBirthDate(), LocalDate.now()).getYears();
                if (age < 18) {
                    ageStats.put("child", ageStats.get("child") + 1);
                } else if (age < 35) {
                    ageStats.put("youth", ageStats.get("youth") + 1);
                } else if (age < 60) {
                    ageStats.put("middle", ageStats.get("middle") + 1);
                } else {
                    ageStats.put("elderly", ageStats.get("elderly") + 1);
                }
            }
        }
        return ageStats;
    }

    // 获取主要年龄段 - 修正参数类型
    private String getMainAgeGroup(List<Patient> patients) {
        Map<String, Integer> ageStats = calculateAgeGroupStats(patients);

        return ageStats.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> {
                    switch (entry.getKey()) {
                        case "child": return "儿童(<18)";
                        case "youth": return "青年(18-34)";
                        case "middle": return "中年(35-59)";
                        case "elderly": return "老年(60+)";
                        default: return "未知";
                    }
                })
                .orElse("未知");
    }

    // 获取主要地区 - 修正参数类型
    private String getMainRegion(List<Patient> patients) {
        Map<String, Long> regionCountMap = patients.stream()
                .filter(p -> p.getRegion() != null && !p.getRegion().isEmpty())
                .collect(Collectors.groupingBy(Patient::getRegion, Collectors.counting()));

        if (regionCountMap.isEmpty()) return "未知";
        return regionCountMap.entrySet().stream()
                .max(Comparator.comparing(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElse("未知");
    }

    // 获取Top5药品-不良反应关联
    public List<DrugReactionStatVO> getTop5DrugReactions() {
        List<DrugReactionStatVO> allStats = getDrugReactionStat(null);
        return allStats.stream()
                .sorted(Comparator.comparing(DrugReactionStatVO::getTotalCount).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    // 实现原有的其他方法
    public List<Map<String, Object>> getGenderDistStat(String drugName) {
        List<Long> patientIds = getPatientIdsByDrug(drugName);
        if (patientIds.isEmpty()) return Collections.emptyList();

        List<Patient> patients = new ArrayList<>();
        for (Long patientId : patientIds) {
            patientRepo.findById(patientId).ifPresent(patients::add);
        }

        long male = patients.stream().filter(p -> "男".equals(p.getGender())).count();
        long female = patients.stream().filter(p -> "女".equals(p.getGender())).count();
        long unknown = patients.size() - male - female;

        List<Map<String, Object>> result = new ArrayList<>();
        if (male > 0) result.add(buildStatMap("男", male));
        if (female > 0) result.add(buildStatMap("女", female));
        if (unknown > 0) result.add(buildStatMap("未知", unknown));
        return result;
    }

    public List<Map<String, Object>> getAgeGroupDistStat(String drugName) {
        List<Long> patientIds = getPatientIdsByDrug(drugName);
        if (patientIds.isEmpty()) return Collections.emptyList();

        List<Patient> patients = new ArrayList<>();
        for (Long patientId : patientIds) {
            patientRepo.findById(patientId).ifPresent(patients::add);
        }

        Map<String, Integer> ageStats = calculateAgeGroupStats(patients);
        List<Map<String, Object>> result = new ArrayList<>();

        if (ageStats.get("child") > 0) result.add(buildStatMap("儿童", ageStats.get("child")));
        if (ageStats.get("youth") > 0) result.add(buildStatMap("青年", ageStats.get("youth")));
        if (ageStats.get("middle") > 0) result.add(buildStatMap("中年", ageStats.get("middle")));
        if (ageStats.get("elderly") > 0) result.add(buildStatMap("老年", ageStats.get("elderly")));

        return result;
    }

    public List<Map<String, Object>> getRegionDistStat(String drugName) {
        List<Long> patientIds = getPatientIdsByDrug(drugName);
        if (patientIds.isEmpty()) return Collections.emptyList();

        List<Patient> patients = new ArrayList<>();
        for (Long patientId : patientIds) {
            patientRepo.findById(patientId).ifPresent(patients::add);
        }

        Map<String, Long> regionCountMap = patients.stream()
                .filter(p -> p.getRegion() != null && !p.getRegion().isEmpty())
                .collect(Collectors.groupingBy(Patient::getRegion, Collectors.counting()));

        return regionCountMap.entrySet().stream()
                .map(entry -> buildStatMap(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(map -> (Long) map.get("value"), Comparator.reverseOrder()))
                .collect(Collectors.toList());
    }

    private List<Long> getPatientIdsByDrug(String drugName) {
        if (drugName != null && !drugName.isEmpty()) {
            return medicationRepo.findPatientIdsByDrug(drugName);
        } else {
            return medicationRepo.findAll().stream()
                    .map(record -> record.getPatient().getId())
                    .distinct()
                    .collect(Collectors.toList());
        }
    }

    private Map<String, Object> buildStatMap(String name, long value) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("value", value);
        return map;
    }
}
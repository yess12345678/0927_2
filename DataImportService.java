package com.zstu.math.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zstu.math.dao.*;
import com.zstu.math.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class DataImportService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private MedicationRecordRepository medicationRecordRepository;

    @Autowired
    private AdverseReactionRecordRepository adverseReactionRecordRepository;

    @Autowired
    private DataImportLogRepository dataImportLogRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final int BATCH_SIZE = 1000;

    public DataImportLog importDataFromJson(String filePath) {
        DataImportLog importLog = new DataImportLog(new File(filePath).getName());

        try {
            File file = new File(filePath);
            List<Map<String, Object>> patientDataList = objectMapper.readValue(file,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));

            importLog.setTotalRecords(patientDataList.size());
            System.out.println("开始导入数据，共 " + patientDataList.size() + " 名患者...");
            long startTime = System.currentTimeMillis();

            Set<String> existingPatientCodes = getExistingPatientCodes(patientDataList);
            System.out.println("已存在 " + existingPatientCodes.size() + " 名患者，将跳过导入");

            ImportResult result = processAndImportData(patientDataList, existingPatientCodes);
            long duration = System.currentTimeMillis() - startTime;

            importLog.setSuccessCount(result.successCount);
            importLog.setFailCount(result.failCount);

            if (result.failCount == 0) {
                importLog.setStatus("SUCCESS");
            } else if (result.successCount > 0) {
                importLog.setStatus("PARTIAL_SUCCESS");
            } else {
                importLog.setStatus("FAILED");
                importLog.setErrorMessage("所有记录导入失败");
            }

            System.out.println("==========================================");
            System.out.println("数据导入完成！");
            System.out.println("成功导入: " + result.successCount + " 名患者");
            System.out.println("跳过已存在: " + existingPatientCodes.size() + " 名患者");
            System.out.println("导入失败: " + result.failCount + " 条");
            System.out.println("总耗时: " + duration + " 毫秒 (" + (duration/1000.0) + " 秒)");
            System.out.println("平均速度: " + (result.successCount/(duration/1000.0)) + " 条/秒");
            System.out.println("==========================================");

        } catch (IOException e) {
            importLog.setStatus("FAILED");
            importLog.setErrorMessage("JSON文件读取失败: " + e.getMessage());
            System.err.println("JSON文件读取失败: " + e.getMessage());
            throw new RuntimeException("JSON文件读取失败: " + e.getMessage(), e);
        } finally {
            dataImportLogRepository.save(importLog);
        }

        return importLog;
    }

    private Set<String> getExistingPatientCodes(List<Map<String, Object>> patientDataList) {
        Set<String> existingCodes = new HashSet<>();
        Set<String> allCodes = new HashSet<>();

        for (Map<String, Object> patientData : patientDataList) {
            String patientCode = (String) patientData.get("patientId");
            if (patientCode != null) {
                allCodes.add(patientCode);
            }
        }

        for (String code : new ArrayList<>(allCodes)) {
            if (patientRepository.existsByPatientCode(code)) {
                existingCodes.add(code);
            }
        }

        return existingCodes;
    }

    @Transactional
    private ImportResult processAndImportData(List<Map<String, Object>> patientDataList, Set<String> existingPatientCodes) {
        int successCount = 0;
        int failCount = 0;

        List<Patient> patientsToSave = new ArrayList<>();
        List<MedicationRecord> medicationsToSave = new ArrayList<>();
        List<AdverseReactionRecord> reactionsToSave = new ArrayList<>();

        for (Map<String, Object> patientData : patientDataList) {
            try {
                String patientCode = (String) patientData.get("patientId");

                if (existingPatientCodes.contains(patientCode)) {
                    continue;
                }

                Patient patient = createPatient(patientData);
                patientsToSave.add(patient);

                prepareAssociatedRecords(patient, patientData, medicationsToSave, reactionsToSave);
                successCount++;

                if (patientsToSave.size() >= BATCH_SIZE) {
                    saveBatchData(patientsToSave, medicationsToSave, reactionsToSave);
                    patientsToSave.clear();
                    medicationsToSave.clear();
                    reactionsToSave.clear();
                    System.out.println("已批量保存 " + successCount + " 名患者...");
                }

            } catch (Exception e) {
                failCount++;
                System.err.println("导入患者失败: " + patientData.get("patientId") + " - " + e.getMessage());
            }
        }

        if (!patientsToSave.isEmpty()) {
            saveBatchData(patientsToSave, medicationsToSave, reactionsToSave);
            System.out.println("保存剩余 " + patientsToSave.size() + " 名患者...");
        }

        return new ImportResult(successCount, failCount);
    }

    private void saveBatchData(List<Patient> patients, List<MedicationRecord> medications, List<AdverseReactionRecord> reactions) {
        patientRepository.saveAll(patients);
        if (!medications.isEmpty()) {
            medicationRecordRepository.saveAll(medications);
        }
        if (!reactions.isEmpty()) {
            adverseReactionRecordRepository.saveAll(reactions);
        }
    }

    private Patient createPatient(Map<String, Object> patientData) {
        String patientCode = (String) patientData.get("patientId");

        Patient patient = new Patient();
        patient.setPatientCode(patientCode);
        patient.setName("患者_" + patientCode);
        patient.setGender((String) patientData.get("gender"));

        String birthDateStr = (String) patientData.get("birthDate");
        if (birthDateStr != null && !birthDateStr.isEmpty()) {
            try {
                patient.setBirthDate(LocalDate.parse(birthDateStr, dateFormatter));
            } catch (Exception e) {
                // 忽略日期解析错误
            }
        }

        patient.setRegion((String) patientData.get("region"));
        return patient;
    }

    private void prepareAssociatedRecords(Patient patient, Map<String, Object> patientData,
                                          List<MedicationRecord> medications, List<AdverseReactionRecord> reactions) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> medicationRecords = (List<Map<String, Object>>) patientData.get("medicationRecords");
        if (medicationRecords != null) {
            for (Map<String, Object> medData : medicationRecords) {
                MedicationRecord medicationRecord = createMedicationRecord(patient, medData);
                medications.add(medicationRecord);
            }
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> reactionRecords = (List<Map<String, Object>>) patientData.get("adverseReactionRecords");
        if (reactionRecords != null) {
            for (Map<String, Object> reactionData : reactionRecords) {
                AdverseReactionRecord reactionRecord = createAdverseReactionRecord(patient, reactionData);
                reactions.add(reactionRecord);
            }
        }
    }

    private MedicationRecord createMedicationRecord(Patient patient, Map<String, Object> medData) {
        MedicationRecord record = new MedicationRecord();
        record.setPatient(patient);

        String medicationTimeStr = (String) medData.get("medicationTime");
        if (medicationTimeStr != null && !medicationTimeStr.isEmpty()) {
            try {
                record.setMedicationTime(LocalDate.parse(medicationTimeStr, dateFormatter));
            } catch (Exception e) {
                record.setMedicationTime(LocalDate.now());
            }
        } else {
            record.setMedicationTime(LocalDate.now());
        }

        record.setDrug((String) medData.get("drug"));
        record.setDosage((String) medData.get("dosage"));
        record.setFrequency((String) medData.get("frequency"));

        return record;
    }

    private AdverseReactionRecord createAdverseReactionRecord(Patient patient, Map<String, Object> reactionData) {
        AdverseReactionRecord record = new AdverseReactionRecord();
        record.setPatient(patient);

        String reactionTimeStr = (String) reactionData.get("reactionTime");
        if (reactionTimeStr != null && !reactionTimeStr.isEmpty()) {
            try {
                record.setReactionTime(LocalDate.parse(reactionTimeStr, dateFormatter));
            } catch (Exception e) {
                record.setReactionTime(LocalDate.now());
            }
        } else {
            record.setReactionTime(LocalDate.now());
        }

        record.setDuration((String) reactionData.get("duration"));
        record.setTiming((String) reactionData.get("timing"));

        @SuppressWarnings("unchecked")
        List<String> symptoms = (List<String>) reactionData.get("symptoms");
        if (symptoms != null) {
            record.setSymptoms(symptoms);
        }

        return record;
    }

    private static class ImportResult {
        public final int successCount;
        public final int failCount;

        public ImportResult(int successCount, int failCount) {
            this.successCount = successCount;
            this.failCount = failCount;
        }
    }
}
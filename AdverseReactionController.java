package com.zstu.math.controller;

import com.zstu.math.dao.PatientRepository;
import com.zstu.math.dao.MedicationRecordRepository;
import com.zstu.math.dao.AdverseReactionRecordRepository;
import com.zstu.math.entity.Patient;
import com.zstu.math.entity.AdverseReactionRecord;
import com.zstu.math.entity.vo.PatientSummaryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class AdverseReactionController {
    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private MedicationRecordRepository medicationRecordRepository;
    @Autowired
    private AdverseReactionRecordRepository adverseReactionRecordRepository;

    // 统一认证检查
    private boolean isNotAuthenticated(HttpSession session) {
        return session.getAttribute("currentUser") == null;
    }

    // 统一添加用户信息和核心统计数据
    private void addCommonAttributes(HttpSession session, Model model) {
        model.addAttribute("currentUser", session.getAttribute("currentUser"));
    }

    private void addCoreStatistics(Model model) {
        model.addAttribute("totalPatients", patientRepository.countTotalPatients());
        model.addAttribute("totalReactions", adverseReactionRecordRepository.countTotalReactions());
        model.addAttribute("totalMedications", medicationRecordRepository.countTotalMedications());
        model.addAttribute("patientsWithReactions", adverseReactionRecordRepository.countPatientsWithReactions());
    }

    // 首页（数据概览）
    @GetMapping("/home")
    public String showHome(HttpSession session, Model model) {
        if (isNotAuthenticated(session)) return "redirect:/";

        addCommonAttributes(session, model);
        addCoreStatistics(model);
        return "dashboard/home";
    }

    // 患者列表（分页）- 增强版，支持模糊查询
    @GetMapping("/patients")
    public String showPatients(
            @RequestParam(required = false) String patientCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            HttpSession session,
            Model model) {
        if (isNotAuthenticated(session)) return "redirect:/";

        Pageable pageable = PageRequest.of(page, size);
        Page<Patient> patientPage;

        if (patientCode != null && !patientCode.isEmpty()) {
            // 支持患者编号模糊查询
            patientPage = patientRepository.findByPatientCodeContaining(patientCode, pageable);
        } else {
            patientPage = patientRepository.findAll(pageable);
        }

        model.addAttribute("patients", patientPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", patientPage.getTotalPages());
        model.addAttribute("totalItems", patientPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("patientCode", patientCode);
        addCommonAttributes(session, model);

        return "query/patients";
    }

    // 患者详情
    @GetMapping("/patients/{patientId}")
    public String showPatientDetail(@PathVariable String patientId, HttpSession session, Model model) {
        if (isNotAuthenticated(session)) return "redirect:/";

        try {
            Patient patient = patientRepository.findByPatientCodeWithMedications(patientId)
                    .orElseThrow(() -> new RuntimeException("患者不存在: " + patientId));

            // 初始化集合避免懒加载异常
            initializeLazyCollections(patient);

            model.addAttribute("patient", patient);
            addCommonAttributes(session, model);
            return "patient-detail";
        } catch (Exception e) {
            model.addAttribute("error", "加载患者详情失败: " + e.getMessage());
            return "date/error";
        }
    }

    // 患者汇总信息（显示用药和不良反应摘要）
    @GetMapping("/patients/summary")
    public String showPatientSummary(
            @RequestParam(required = false) String patientCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            HttpSession session, Model model) {
        if (isNotAuthenticated(session)) return "redirect:/";

        Pageable pageable = PageRequest.of(page, size);
        Page<Patient> patientPage;

        if (patientCode != null && !patientCode.isEmpty()) {
            patientPage = patientRepository.findByPatientCodeContaining(patientCode, pageable);
        } else {
            patientPage = patientRepository.findAll(pageable);
        }

        // 转换为PatientSummaryVO
        List<PatientSummaryVO> summaryList = patientPage.getContent().stream()
                .map(this::convertToPatientSummary)
                .collect(Collectors.toList());

        model.addAttribute("patientSummaries", summaryList);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", patientPage.getTotalPages());
        model.addAttribute("totalItems", patientPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("patientCode", patientCode);
        addCommonAttributes(session, model);

        return "patient-summary";
    }

    // 转换患者信息为汇总VO
    private PatientSummaryVO convertToPatientSummary(Patient patient) {
        PatientSummaryVO summary = new PatientSummaryVO();
        summary.setPatientCode(patient.getPatientCode());

        // 计算开始用药日期和结束用药日期
        LocalDate startDate = patient.getMedicationRecords().stream()
                .map(record -> record.getMedicationTime())
                .min(LocalDate::compareTo)
                .orElse(null);
        LocalDate endDate = patient.getMedicationRecords().stream()
                .map(record -> record.getMedicationTime())
                .max(LocalDate::compareTo)
                .orElse(null);

        summary.setStartMedicationDate(startDate);
        summary.setEndMedicationDate(endDate);

        // 所有药品名称（逗号分隔）
        String allDrugs = patient.getMedicationRecords().stream()
                .map(record -> record.getDrug())
                .distinct()
                .collect(Collectors.joining(", "));
        summary.setAllDrugs(allDrugs);

        // 所有不良反应名称（逗号分隔）
        String allReactions = patient.getAdverseReactionRecords().stream()
                .flatMap(record -> record.getSymptoms().stream())
                .distinct()
                .collect(Collectors.joining(", "));
        summary.setAllReactions(allReactions);

        return summary;
    }

    // 初始化懒加载集合
    private void initializeLazyCollections(Patient patient) {
        if (patient.getAdverseReactionRecords() != null) {
            patient.getAdverseReactionRecords().size(); // 触发初始化
            patient.getAdverseReactionRecords().forEach(record -> {
                if (record.getSymptoms() != null) record.getSymptoms().size();
            });
        }
    }

    // 统计分析页面
    @GetMapping("/analysis")
    public String showAnalysis(HttpSession session, Model model) {
        if (isNotAuthenticated(session)) return "redirect:/";

        addCommonAttributes(session, model);
        addCoreStatistics(model);

        // 添加统计分析数据
        model.addAttribute("drugStats", medicationRecordRepository.countByDrug());
        model.addAttribute("timingStats", adverseReactionRecordRepository.countByTiming());
        model.addAttribute("symptomStats", adverseReactionRecordRepository.countBySymptom());

        return "analysis/analysis";
    }
}
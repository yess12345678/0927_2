package com.zstu.math.controller;

import com.zstu.math.dao.AdverseReactionRecordRepository;
import com.zstu.math.dao.MedicationRecordRepository;
import com.zstu.math.entity.AdverseReactionRecord;
import com.zstu.math.entity.MedicationRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Controller
public class RecordQueryController {
    @Autowired
    private MedicationRecordRepository medicationRepo;
    @Autowired
    private AdverseReactionRecordRepository reactionRepo;

    private boolean isNotAuthenticated(HttpSession session) {
        return session.getAttribute("currentUser") == null;
    }

    @GetMapping("/query/medication")
    public String showMedication(@RequestParam(required = false) Long patientId,
                                 @RequestParam(required = false) String drugName,
                                 @RequestParam(required = false) String timeAfter,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "50") int size,
                                 HttpSession session, Model model) {
        if (isNotAuthenticated(session)) return "redirect:/";

        try {
            Page<MedicationRecord> medicationPage = getMedicationPage(patientId, drugName, timeAfter, page, size);
            addPageAttributes(model, medicationPage, page, size);
            addQueryAttributes(model, patientId, drugName, timeAfter);
        } catch (DateTimeParseException e) {
            handleDateTimeError(model, page, size, "medication");
        }

        model.addAttribute("currentUser", session.getAttribute("currentUser"));
        return "query/medication";
    }

    @GetMapping("/query/reaction")
    public String showReaction(@RequestParam(required = false) Long patientId,
                               @RequestParam(required = false) String timing,
                               @RequestParam(required = false) String timeAfter,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "50") int size,
                               HttpSession session, Model model) {
        if (isNotAuthenticated(session)) return "redirect:/";

        try {
            Page<AdverseReactionRecord> reactionPage = getReactionPage(patientId, timing, timeAfter, page, size);
            addPageAttributes(model, reactionPage, page, size);
            addQueryAttributes(model, patientId, timing, timeAfter);
        } catch (DateTimeParseException e) {
            handleDateTimeError(model, page, size, "reaction");
        }

        model.addAttribute("currentUser", session.getAttribute("currentUser"));
        return "query/reaction";
    }

    private Page<MedicationRecord> getMedicationPage(Long patientId, String drugName,
                                                     String timeAfter, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (patientId != null) return medicationRepo.findByPatientId(patientId, pageable);
        if (drugName != null && !drugName.isEmpty()) return medicationRepo.findByDrugContaining(drugName, pageable);
        if (timeAfter != null && !timeAfter.isEmpty()) return medicationRepo.findByMedicationTimeAfter(LocalDate.parse(timeAfter), pageable);
        return medicationRepo.findAll(pageable);
    }

    private Page<AdverseReactionRecord> getReactionPage(Long patientId, String timing,
                                                        String timeAfter, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (patientId != null) return reactionRepo.findByPatientId(patientId, pageable);
        if (timing != null && !timing.isEmpty()) return reactionRepo.findByTimingContaining(timing, pageable);
        if (timeAfter != null && !timeAfter.isEmpty()) return reactionRepo.findByReactionTimeAfter(LocalDate.parse(timeAfter), pageable);
        return reactionRepo.findAll(pageable);
    }

    private <T> void addPageAttributes(Model model, Page<T> page, int currentPage, int pageSize) {
        model.addAttribute(page.getContent().get(0) instanceof MedicationRecord ? "medicationRecords" : "reactionRecords",
                page.getContent());
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("totalItems", page.getTotalElements());
        model.addAttribute("pageSize", pageSize);
    }

    private void addQueryAttributes(Model model, Object... attributes) {
        String[] names = {"patientId", "drugName", "timeAfter", "timing"};
        for (int i = 0; i < attributes.length && i < names.length; i++) {
            model.addAttribute(names[i], attributes[i]);
        }
    }

    private void handleDateTimeError(Model model, int page, int size, String type) {
        model.addAttribute("error", "时间格式错误！请使用yyyy-MM-dd格式");
        Pageable pageable = PageRequest.of(page, size);
        Page<?> defaultPage = type.equals("medication") ?
                medicationRepo.findAll(pageable) : reactionRepo.findAll(pageable);
        addPageAttributes(model, defaultPage, page, size);
    }
}
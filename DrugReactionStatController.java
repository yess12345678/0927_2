package com.zstu.math.controller;

import com.zstu.math.dao.MedicationRecordRepository;
import com.zstu.math.entity.vo.DrugReactionStatVO;
import com.zstu.math.service.DrugReactionStatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@Controller
public class DrugReactionStatController {
    @Autowired
    private DrugReactionStatService statService;

    @Autowired
    private MedicationRecordRepository medicationRepo;

    private boolean isNotAuthenticated(HttpSession session) {
        return session.getAttribute("currentUser") == null;
    }

    @GetMapping("/analysis/drug-reaction")
    public String showDrugReactionStat(
            @RequestParam(required = false) String drugName,
            HttpSession session, Model model) {

        if (isNotAuthenticated(session)) return "redirect:/";

        model.addAttribute("allDrugs", medicationRepo.findAllDrugs());
        model.addAttribute("drugName", drugName);
        model.addAttribute("currentUser", session.getAttribute("currentUser"));

        // 统一获取统计数据
        addStatisticsData(model, drugName);

        // 添加图表数据
        addChartData(model, drugName);

        return "analysis/drug-reaction-stat";
    }

    // 月度统计页面 - 直接使用DrugReactionStatService
    @GetMapping("/analysis/monthly")
    public String showMonthlyStat(HttpSession session, Model model) {
        if (isNotAuthenticated(session)) return "redirect:/";

        // 这里可以添加月度统计逻辑，暂时留空或使用其他方法
        model.addAttribute("currentUser", session.getAttribute("currentUser"));

        return "monthly-stat";
    }

    // Top5统计页面（直方图）
    @GetMapping("/analysis/top5")
    public String showTop5Stat(HttpSession session, Model model) {
        if (isNotAuthenticated(session)) return "redirect:/";

        List<DrugReactionStatVO> top5Stats = statService.getTop5DrugReactions();
        model.addAttribute("top5Stats", top5Stats);
        model.addAttribute("currentUser", session.getAttribute("currentUser"));

        return "top5-stat";
    }

    private void addStatisticsData(Model model, String drugName) {
        model.addAttribute("drugReactionStats", statService.getDrugReactionStat(drugName));
        model.addAttribute("genderStats", statService.getGenderDistStat(drugName));
        model.addAttribute("ageStats", statService.getAgeGroupDistStat(drugName));
        model.addAttribute("regionStats", statService.getRegionDistStat(drugName));
    }

    private void addChartData(Model model, String drugName) {
        // 获取Top5数据用于图表显示
        List<DrugReactionStatVO> top5Stats = statService.getTop5DrugReactions();
        model.addAttribute("top5Stats", top5Stats);
    }
}
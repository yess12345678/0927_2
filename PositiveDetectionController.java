package com.zstu.math.controller;

import com.zstu.math.entity.DrugReactionPositive;
import com.zstu.math.entity.PositiveReview;
import com.zstu.math.entity.AdverseReactionRecord;
import com.zstu.math.service.PositiveDetectionService;
import com.zstu.math.service.DataMiningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/positive")
public class PositiveDetectionController {

    @Autowired
    private PositiveDetectionService positiveDetectionService;

    @Autowired
    private DataMiningService dataMiningService;

    private boolean isNotAuthenticated(HttpSession session) {
        return session.getAttribute("currentUser") == null;
    }

    @GetMapping("/results")
    public String showPositiveResults(@RequestParam(required = false) String keyword,
                                      HttpSession session, Model model) {
        if (isNotAuthenticated(session)) return "redirect:/";

        List<DrugReactionPositive> results = (keyword != null && !keyword.trim().isEmpty()) ?
                positiveDetectionService.searchPositiveResults(keyword) :
                positiveDetectionService.getPositiveResults();

        addCommonAttributes(model, session, results, keyword);
        return "positive/results";
    }

    @GetMapping("/pending")
    public String showPendingReview(HttpSession session, Model model) {
        if (isNotAuthenticated(session)) return "redirect:/";

        model.addAttribute("pendingReviews", positiveDetectionService.getPendingReview());
        model.addAttribute("currentUser", session.getAttribute("currentUser"));
        return "positive/pending";
    }

    @PostMapping("/review")
    public String submitReview(@RequestParam Long positiveId,
                               @RequestParam String reviewOpinion,
                               @RequestParam String reviewResult,
                               HttpSession session) {
        if (isNotAuthenticated(session)) return "redirect:/";

        PositiveReview review = createReview(positiveId, reviewOpinion, reviewResult, session);
        positiveDetectionService.saveReview(review);
        return "redirect:/positive/pending?success=审核提交成功";
    }

    @PostMapping("/analyze")
    public String triggerAnalysis(HttpSession session) {
        if (isNotAuthenticated(session)) return "redirect:/";

        dataMiningService.analyzePositiveReactions();
        return "redirect:/positive/results?success=阳性分析完成";
    }

    // 显示阳性反应地图
    @GetMapping("/map")
    public String showPositiveMap(@RequestParam String drugName,
                                  @RequestParam String reactionName,
                                  HttpSession session, Model model) {
        if (isNotAuthenticated(session)) return "redirect:/";

        List<AdverseReactionRecord> locationData =
                dataMiningService.getLocationDataForPositiveReaction(drugName, reactionName);

        model.addAttribute("locationData", locationData);
        model.addAttribute("drugName", drugName);
        model.addAttribute("reactionName", reactionName);
        model.addAttribute("currentUser", session.getAttribute("currentUser"));

        return "positive/map";
    }

    // 阳性判定详情页面
    @GetMapping("/detail/{id}")
    public String showPositiveDetail(@PathVariable Long id,
                                     HttpSession session, Model model) {
        if (isNotAuthenticated(session)) return "redirect:/";

        // 这里可以添加获取阳性判定详情的逻辑
        model.addAttribute("currentUser", session.getAttribute("currentUser"));
        return "positive/detail";
    }

    private PositiveReview createReview(Long positiveId, String opinion, String result, HttpSession session) {
        PositiveReview review = new PositiveReview();
        review.setPositiveId(positiveId);
        review.setReviewer((String) session.getAttribute("currentUser"));
        review.setReviewOpinion(opinion);
        review.setReviewResult(result);
        return review;
    }

    private void addCommonAttributes(Model model, HttpSession session,
                                     List<DrugReactionPositive> results, String keyword) {
        model.addAttribute("positiveResults", results);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentUser", session.getAttribute("currentUser"));
    }
}
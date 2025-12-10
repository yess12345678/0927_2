package com.zstu.math.service.impl;

import com.zstu.math.service.DrugReactionStatService;
import com.zstu.math.service.DataMiningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ScheduledStatService {

    @Autowired
    private DrugReactionStatService drugReactionStatService;

    @Autowired
    private DataMiningService dataMiningService;

    // 每周一凌晨执行药品-不良反应统计
    @Scheduled(cron = "0 0 0 * * MON")
    public void weeklyDrugReactionStat() {
        System.out.println("[" + java.time.LocalDateTime.now() + "] 执行每周药品-不良反应统计...");
        drugReactionStatService.generateWeeklyDrugReactionStats();
    }

    // 每月1号凌晨执行阳性判定分析
    @Scheduled(cron = "0 0 2 1 * ?")
    public void monthlyPositiveAnalysis() {
        System.out.println("[" + java.time.LocalDateTime.now() + "] 执行每月阳性判定分析...");
        dataMiningService.analyzePositiveReactions();
    }

    // 每月1号凌晨执行不良反应月度统计 - 暂时注释掉或移除
    // @Scheduled(cron = "0 0 1 1 * ?")
    // public void monthlyReactionStat() {
    //     System.out.println("[" + java.time.LocalDateTime.now() + "] 执行每月不良反应统计...");
    //     // 这里可以添加月度统计逻辑
    // }
}
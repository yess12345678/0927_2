package com.zstu.math.entity.vo;

// 药品-不良反应统计VO（用于前端展示统计详情）
public class DrugReactionStatVO {
    private String drug;          // 药品名称
    private String symptom;       // 不良反应症状
    private int totalCount;       // 涉及患者总数
    private int maleCount;        // 男性患者数
    private int femaleCount;      // 女性患者数
    private int childCount;       // 儿童患者数 (<18)
    private int youthCount;       // 青年患者数 (18-34)
    private int middleAgeCount;   // 中年患者数 (35-59)
    private int elderlyCount;     // 老年患者数 (60+)
    private String mainAgeGroup;  // 主要年龄段
    private String mainRegion;    // 主要地区

    // Getter和Setter
    public String getDrug() { return drug; }
    public void setDrug(String drug) { this.drug = drug; }

    public String getSymptom() { return symptom; }
    public void setSymptom(String symptom) { this.symptom = symptom; }

    public int getTotalCount() { return totalCount; }
    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }

    public int getMaleCount() { return maleCount; }
    public void setMaleCount(int maleCount) { this.maleCount = maleCount; }

    public int getFemaleCount() { return femaleCount; }
    public void setFemaleCount(int femaleCount) { this.femaleCount = femaleCount; }

    public int getChildCount() { return childCount; }
    public void setChildCount(int childCount) { this.childCount = childCount; }

    public int getYouthCount() { return youthCount; }
    public void setYouthCount(int youthCount) { this.youthCount = youthCount; }

    public int getMiddleAgeCount() { return middleAgeCount; }
    public void setMiddleAgeCount(int middleAgeCount) { this.middleAgeCount = middleAgeCount; }

    public int getElderlyCount() { return elderlyCount; }
    public void setElderlyCount(int elderlyCount) { this.elderlyCount = elderlyCount; }

    public String getMainAgeGroup() { return mainAgeGroup; }
    public void setMainAgeGroup(String mainAgeGroup) { this.mainAgeGroup = mainAgeGroup; }

    public String getMainRegion() { return mainRegion; }
    public void setMainRegion(String mainRegion) { this.mainRegion = mainRegion; }
}
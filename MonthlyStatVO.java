package com.zstu.math.entity.vo;

public class MonthlyStatVO {
    private String month;
    private Long count;

    public MonthlyStatVO() {}

    public MonthlyStatVO(String month, Long count) {
        this.month = month;
        this.count = count;
    }

    // Getter和Setter
    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }
    public Long getCount() { return count; }
    public void setCount(Long count) { this.count = count; }
}
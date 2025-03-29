package com.example.resume.entity;

import lombok.Data;

@Data
public class WorkExperience {
    private String startDate;    // 开始时间
    private String endDate;      // 结束时间
    private String company;      // 公司名称
    private String position;     // 担任职务
    private String description;  // 工作职责说明
} 
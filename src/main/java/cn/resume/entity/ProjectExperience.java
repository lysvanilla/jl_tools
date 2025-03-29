package cn.resume.entity;

import lombok.Data;

@Data
public class ProjectExperience {
    private String startDate;    // 开始时间
    private String endDate;      // 结束时间
    private String projectName;  // 项目名称
    private String role;         // 项目角色
    private String description;  // 项目职责说明
} 
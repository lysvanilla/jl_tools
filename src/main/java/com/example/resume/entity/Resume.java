package com.example.resume.entity;

import lombok.Data;
import java.util.List;

@Data
public class Resume {
    private String name;            // 姓名
    private String graduationDate;  // 毕业时间
    private String major;           // 专业
    private String department;      // 所在部门
    private String workYears;       // 工作年限
    private String school;          // 毕业学校
    private String education;       // 最高学历
    private String title;           // 职称
    private String personalSummary; // 个人简介
    private String technicalSkills; // 业务与技术能力
    private String certification;   // 资质认证
    private String training;        // 参与培训
    private String skillTags;       // 技能标签
    
    private List<WorkExperience> workExperiences;    // 工作经历
    private List<ProjectExperience> projectExperiences;  // 项目经历

    public String getPersonalSummary() {
        return personalSummary;
    }

    public void setPersonalSummary(String personalSummary) {
        this.personalSummary = personalSummary;
    }

    public String getTechnicalSkills() {
        return technicalSkills;
    }

    public void setTechnicalSkills(String technicalSkills) {
        this.technicalSkills = technicalSkills;
    }
} 
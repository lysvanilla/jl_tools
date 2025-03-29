package com.example.resume;

import com.example.resume.entity.ProjectExperience;
import com.example.resume.entity.Resume;
import com.example.resume.entity.WorkExperience;
import com.example.resume.util.ResumeParser;

public class ResumeParserDemo {
    public static void main(String[] args) {
        String filePath = "D:\\projects\\jl_tools\\logs\\00603+邹智+工作简历.docx";
        System.out.println("开始解析文件: " + filePath);
        
        Resume resume = ResumeParser.parseResume(filePath);

        if (resume != null) {
            // 打印基本信息
            System.out.println("姓名: " + resume.getName());
            System.out.println("毕业时间: " + resume.getGraduationDate());
            System.out.println("专业: " + resume.getMajor());
            System.out.println("所在部门: " + resume.getDepartment());
            System.out.println("工作年限: " + resume.getWorkYears());
            System.out.println("毕业学校: " + resume.getSchool());
            System.out.println("最高学历: " + resume.getEducation());
            System.out.println("职称: " + resume.getTitle());
            System.out.println("个人简介: " + resume.getPersonalSummary());
            System.out.println("业务与技术能力: " + resume.getSkillTags());
            System.out.println("资质认证: " + resume.getCertification());
            System.out.println("参与培训: " + resume.getTraining());
            System.out.println("技能标签: " + resume.getSkillTags());

            // 打印工作经历
            if (resume.getWorkExperiences() != null) {
                System.out.println("\n工作经历数量: " + resume.getWorkExperiences().size());
                for (WorkExperience exp : resume.getWorkExperiences()) {
                    System.out.println(exp.getStartDate() + " - " + exp.getEndDate());
                    System.out.println(exp.getCompany() + " " + exp.getPosition());
                    System.out.println(exp.getDescription());
                    System.out.println();
                }
            } else {
                System.out.println("\n工作经历: 无");
            }

            // 打印项目经历
            if (resume.getProjectExperiences() != null) {
                System.out.println("\n项目经历数量: " + resume.getProjectExperiences().size());
                for (ProjectExperience exp : resume.getProjectExperiences()) {
                    System.out.println(exp.getStartDate() + " - " + exp.getEndDate());
                    System.out.println(exp.getProjectName() + " " + exp.getRole());
                    System.out.println(exp.getDescription());
                    System.out.println();
                }
            } else {
                System.out.println("\n项目经历: 无");
            }
        } else {
            System.out.println("简历解析失败");
        }
    }
}
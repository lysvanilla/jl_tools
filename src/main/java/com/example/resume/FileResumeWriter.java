package com.example.resume;

import com.example.resume.entity.ProjectExperience;
import com.example.resume.entity.Resume;
import com.example.resume.entity.WorkExperience;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class FileResumeWriter {
    public static void main(String[] args) {
        String filePath = "D:\\projects\\jl_tools\\logs\\00603+邹智+工作简历.docx";
        String outputPath = "D:\\projects\\jl_tools\\logs\\resume_output.txt";
        
        System.out.println("开始解析简历文件: " + filePath);
        Resume resume = TableBasedResumeParser.parseResume(filePath);
        
        if (resume != null) {
            writeResumeToFile(resume, outputPath);
            System.out.println("简历解析结果已写入: " + outputPath);
        } else {
            System.out.println("简历解析失败");
        }
    }
    
    private static void writeResumeToFile(Resume resume, String outputPath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputPath))) {
            writer.println("====== 基本信息 ======");
            writer.println("姓名: " + getValue(resume.getName()));
            writer.println("工作年限: " + getValue(resume.getWorkYears()));
            writer.println("毕业时间: " + getValue(resume.getGraduationDate()));
            writer.println("毕业学校: " + getValue(resume.getSchool()));
            writer.println("专业: " + getValue(resume.getMajor()));
            writer.println("最高学历: " + getValue(resume.getEducation()));
            writer.println("所在部门: " + getValue(resume.getDepartment()));
            writer.println("职称: " + getValue(resume.getTitle()));
            
            writer.println("\n====== 工作经历 ======");
            if (resume.getWorkExperiences() != null && !resume.getWorkExperiences().isEmpty()) {
                writer.println("共 " + resume.getWorkExperiences().size() + " 条工作经历");
                for (WorkExperience exp : resume.getWorkExperiences()) {
                    writer.println("时间段: " + exp.getStartDate() + " - " + exp.getEndDate());
                    writer.println("公司: " + exp.getCompany());
                    writer.println("职位: " + exp.getPosition());
                    writer.println("工作描述: " + exp.getDescription());
                    writer.println("------");
                }
            } else {
                writer.println("无工作经历");
            }
            
            writer.println("\n====== 项目经历 ======");
            if (resume.getProjectExperiences() != null && !resume.getProjectExperiences().isEmpty()) {
                writer.println("共 " + resume.getProjectExperiences().size() + " 条项目经历");
                for (ProjectExperience exp : resume.getProjectExperiences()) {
                    writer.println("时间段: " + exp.getStartDate() + " - " + exp.getEndDate());
                    writer.println("项目名称: " + exp.getProjectName());
                    writer.println("项目角色: " + exp.getRole());
                    writer.println("项目描述: " + exp.getDescription());
                    writer.println("------");
                }
            } else {
                writer.println("无项目经历");
            }
            
            writer.println("\n====== 其他信息 ======");
            writer.println("个人简介: " + getValue(resume.getPersonalSummary()));
            writer.println("业务与技术能力: " + getValue(resume.getSkillTags()));
            writer.println("资质认证: " + getValue(resume.getCertification()));
            writer.println("参与培训: " + getValue(resume.getTraining()));
            writer.println("技能标签: " + getValue(resume.getSkillTags()));
            
            writer.println("\n====== 解析完成 ======");
        } catch (IOException e) {
            System.out.println("写入文件时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static String getValue(String value) {
        return value != null ? value : "未解析";
    }
} 
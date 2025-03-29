package com.example.resume;

import com.example.resume.entity.Resume;

public class BasicInfoPrinterDemo {
    public static void main(String[] args) {
        String filePath = "D:\\projects\\jl_tools\\logs\\00603+邹智+工作简历.docx";
        
        System.out.println("开始解析简历文件: " + filePath);
        Resume resume = TableBasedResumeParser.parseResume(filePath);
        
        if (resume != null) {
            System.out.println("\n====== 基本信息解析结果 ======");
            
            System.out.println("姓名: " + (resume.getName() != null ? resume.getName() : "未解析"));
            System.out.println("工作年限: " + (resume.getWorkYears() != null ? resume.getWorkYears() : "未解析"));
            System.out.println("毕业时间: " + (resume.getGraduationDate() != null ? resume.getGraduationDate() : "未解析"));
            System.out.println("毕业学校: " + (resume.getSchool() != null ? resume.getSchool() : "未解析"));
            System.out.println("专业: " + (resume.getMajor() != null ? resume.getMajor() : "未解析"));
            System.out.println("最高学历: " + (resume.getEducation() != null ? resume.getEducation() : "未解析"));
            System.out.println("所在部门: " + (resume.getDepartment() != null ? resume.getDepartment() : "未解析"));
            System.out.println("职称: " + (resume.getTitle() != null ? resume.getTitle() : "未解析"));
            
            System.out.println("\n====== 解析完成 ======");
        } else {
            System.out.println("简历解析失败");
        }
    }
} 
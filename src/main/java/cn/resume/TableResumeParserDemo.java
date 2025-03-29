package cn.resume;

import cn.resume.entity.ProjectExperience;
import cn.resume.entity.Resume;
import cn.resume.entity.WorkExperience;

public class TableResumeParserDemo {
    public static void main(String[] args) {
        String filePath = "D:\\projects\\jl_tools\\logs\\00603+邹智+工作简历.docx";
        
        System.out.println("开始解析简历文件: " + filePath);
        Resume resume = TableBasedResumeParser.parseResume(filePath);
        
        if (resume != null) {
            printResumeData(resume);
        } else {
            System.out.println("简历解析失败");
        }
    }
    
    private static void printResumeData(Resume resume) {
        System.out.println("\n====== 基本信息 ======");
        System.out.println("姓名: " + getValue(resume.getName()));
        System.out.println("工作年限: " + getValue(resume.getWorkYears()));
        System.out.println("毕业时间: " + getValue(resume.getGraduationDate()));
        System.out.println("毕业学校: " + getValue(resume.getSchool()));
        System.out.println("专业: " + getValue(resume.getMajor()));
        System.out.println("最高学历: " + getValue(resume.getEducation()));
        System.out.println("所在部门: " + getValue(resume.getDepartment()));
        System.out.println("职称: " + getValue(resume.getTitle()));
        
        System.out.println("\n====== 工作经历 ======");
        if (resume.getWorkExperiences() != null && !resume.getWorkExperiences().isEmpty()) {
            System.out.println("共 " + resume.getWorkExperiences().size() + " 条工作经历");
            for (WorkExperience exp : resume.getWorkExperiences()) {
                System.out.println("时间段: " + exp.getStartDate() + " - " + exp.getEndDate());
                System.out.println("公司: " + exp.getCompany());
                System.out.println("职位: " + exp.getPosition());
                System.out.println("工作描述: " + exp.getDescription());
                System.out.println("------");
            }
        } else {
            System.out.println("无工作经历");
        }
        
        System.out.println("\n====== 项目经历 ======");
        if (resume.getProjectExperiences() != null && !resume.getProjectExperiences().isEmpty()) {
            System.out.println("共 " + resume.getProjectExperiences().size() + " 条项目经历");
            for (ProjectExperience exp : resume.getProjectExperiences()) {
                System.out.println("时间段: " + exp.getStartDate() + " - " + exp.getEndDate());
                System.out.println("项目名称: " + exp.getProjectName());
                System.out.println("项目角色: " + exp.getRole());
                System.out.println("项目描述: " + exp.getDescription());
                System.out.println("------");
            }
        } else {
            System.out.println("无项目经历");
        }
        
        System.out.println("\n====== 其他信息 ======");
        System.out.println("个人简介: " + getValue(resume.getPersonalSummary()));
        System.out.println("业务与技术能力: " + getValue(resume.getSkillTags()));
        System.out.println("资质认证: " + getValue(resume.getCertification()));
        System.out.println("参与培训: " + getValue(resume.getTraining()));
        System.out.println("技能标签: " + getValue(resume.getSkillTags()));
        
        System.out.println("\n====== 解析完成 ======");
    }
    
    private static String getValue(String value) {
        return value != null ? value : "未解析";
    }
} 
package com.example.resume;

import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.config.Configure;
import com.example.resume.entity.Resume;
import com.example.resume.policy.ProjectExperienceTablePolicy;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 简历导出器 - 使用poi-tl将解析的Resume对象按模板导出为Word文档
 */
public class ResumeWordExporter {

    public static void main(String[] args) {
        // 测试导出功能
        String inputFilePath = "D:\\projects\\jl_tools\\logs\\00603+邹智+工作简历.docx";
        String templatePath = "D:\\projects\\jl_tools\\template\\doc\\简历模版1.docx";
        String outputPath = "D:\\projects\\jl_tools\\logs\\导出简历_" + System.currentTimeMillis() + ".docx";
        
        System.out.println("开始解析简历文件: " + inputFilePath);
        Resume resume = TableBasedResumeParser.parseResume(inputFilePath);
        
        if (resume != null) {
            System.out.println("开始导出简历到Word文档");
            exportResumeToWord(resume, templatePath, outputPath);
            System.out.println("简历已成功导出到: " + outputPath);
        } else {
            System.out.println("简历解析失败，无法导出");
        }
    }
    
    /**
     * 将Resume对象导出到Word模板
     * 
     * @param resume 简历对象
     * @param templatePath 模板文件路径
     * @param outputPath 输出文件路径
     */
    public static void exportResumeToWord(Resume resume, String templatePath, String outputPath) {
        try {
            // 准备数据
            Map<String, Object> data = prepareDataForTemplate(resume);
            
            // 使用自定义的项目经验表格渲染策略
            ProjectExperienceTablePolicy tablePolicy = new ProjectExperienceTablePolicy();
            
            // 配置表格渲染策略 - 直接将resume对象作为数据传递
            Configure config = Configure.builder()
                    .bind("projectExperiences", tablePolicy)
                    .build();
            
            // 直接将resume对象设置为projectExperiences标记的值
            data.put("projectExperiences", resume);
            
            XWPFTemplate template = XWPFTemplate.compile(templatePath, config)
                    .render(data);
            
            // 写入文件
            try (FileOutputStream out = new FileOutputStream(outputPath)) {
                template.write(out);
                template.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("导出简历时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 准备用于模板渲染的数据
     * 
     * @param resume 简历对象
     * @return 用于模板的数据映射
     */
    private static Map<String, Object> prepareDataForTemplate(Resume resume) {
        Map<String, Object> data = new HashMap<>();
        
        // 1. 基本信息
        data.put("name", getValueOrDefault(resume.getName(), ""));
        data.put("title", getValueOrDefault(resume.getTitle(), ""));
        data.put("education", getValueOrDefault(resume.getEducation(), ""));
        data.put("major", getValueOrDefault(resume.getMajor(), ""));
        data.put("graduationDate", getValueOrDefault(resume.getGraduationDate(), ""));
        data.put("certification", getValueOrDefault(resume.getCertification(), ""));
        data.put("school", getValueOrDefault(resume.getSchool(), ""));
        
        // 2. 计算本单位任职时间（查找包含"长亮"的公司名称的工作经历中最早的开始时间）
        String employmentPeriod = calculateEmploymentPeriod(resume);
        data.put("employmentPeriod", employmentPeriod);
        
        // 3. 工作年限
        data.put("workYears", getValueOrDefault(resume.getWorkYears(), ""));
        
        // 4. 尝试从毕业时间计算出生年月（假设大学毕业时22岁）
        String birthMonth = calculateBirthMonth(resume.getGraduationDate());
        data.put("birthMonth", birthMonth);
        
        // 5. 项目角色（取最近一个项目的角色或默认值）
        String projectRole = getLatestProjectRole(resume);
        data.put("projectRole", projectRole);
        
        return data;
    }
    
    /**
     * 计算本单位任职时间（查找包含"长亮"的公司名称的工作经历中最早的开始时间）
     */
    private static String calculateEmploymentPeriod(Resume resume) {
        if (resume.getWorkExperiences() != null && !resume.getWorkExperiences().isEmpty()) {
            String earliestDate = null;
            
            // 遍历工作经历，查找包含"长亮"的公司名称
            for (int i = 0; i < resume.getWorkExperiences().size(); i++) {
                String company = resume.getWorkExperiences().get(i).getCompany();
                if (company != null && company.contains("长亮")) {
                    String startDate = resume.getWorkExperiences().get(i).getStartDate();
                    if (startDate != null && !startDate.isEmpty()) {
                        // 如果是第一个找到的日期，或者比当前最早的日期更早，则更新
                        if (earliestDate == null || startDate.compareTo(earliestDate) < 0) {
                            earliestDate = startDate;
                        }
                    }
                }
            }
            
            // 如果找到了包含"长亮"的公司的最早开始时间，则返回
            if (earliestDate != null) {
                return earliestDate ;
            }
            
            // 如果没有找到包含"长亮"的公司，则返回第一条工作经历
            String startDate = resume.getWorkExperiences().get(0).getStartDate();
            if (startDate != null && !startDate.isEmpty()) {
                return startDate ;
            }
        }
        return "";
    }
    
    /**
     * 尝试计算出生年月（假设大学本科毕业时22岁）
     */
    private static String calculateBirthMonth(String graduationDate) {
        if (graduationDate != null && !graduationDate.isEmpty()) {
            try {
                // 提取毕业时间中的年份
                String yearStr = graduationDate.replaceAll("[^0-9]", "");
                if (yearStr.length() >= 4) {
                    yearStr = yearStr.substring(0, 4);
                    int graduationYear = Integer.parseInt(yearStr);
                    int birthYear = graduationYear - 22; // 假设本科毕业时22岁
                    
                    return birthYear + "年";
                }
            } catch (Exception e) {
                System.out.println("计算出生年月时出错: " + e.getMessage());
            }
        }
        return "";
    }
    
    /**
     * 获取最近一个项目的角色
     */
    private static String getLatestProjectRole(Resume resume) {
        if (resume.getProjectExperiences() != null && !resume.getProjectExperiences().isEmpty()) {
            String role = resume.getProjectExperiences().get(0).getRole();
            if (role != null && !role.isEmpty()) {
                return role;
            }
        }
        return "";
    }
    
    /**
     * 获取值或默认值
     */
    private static String getValueOrDefault(String value, String defaultValue) {
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }
} 
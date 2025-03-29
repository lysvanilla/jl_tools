package com.example.resume.util;

import com.example.resume.entity.Resume;
import com.example.resume.entity.WorkExperience;
import com.example.resume.entity.ProjectExperience;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Range;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class ResumeParser {
    
    /**
     * 解析Word文档
     */
    public static Resume parseResume(String filePath) {
        Resume resume = new Resume();
        
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                log.error("文件不存在: {}", filePath);
                System.out.println("文件不存在: " + filePath);
                return null;
            }

            if (filePath.endsWith(".docx")) {
                System.out.println("开始解析.docx文件...");
                parseDocx(file, resume);
            } else if (filePath.endsWith(".doc")) {
                System.out.println("开始解析.doc文件...");
                parseDoc(file, resume);
            } else {
                log.error("不支持的文件格式: {}", filePath);
                System.out.println("不支持的文件格式: " + filePath);
                return null;
            }
            
        } catch (Exception e) {
            log.error("解析简历文件时发生错误", e);
            System.out.println("解析简历文件时发生错误: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        
        return resume;
    }
    
    /**
     * 解析.docx文件
     */
    private static void parseDocx(File file, Resume resume) throws Exception {
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument document = new XWPFDocument(fis)) {
            
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            System.out.println("文档中包含 " + paragraphs.size() + " 个段落");
            
            List<WorkExperience> workExperiences = new ArrayList<>();
            List<ProjectExperience> projectExperiences = new ArrayList<>();
            
            String currentSection = "";
            StringBuilder contentBuilder = new StringBuilder();
            
            int paragraphCount = 0;
            for (XWPFParagraph paragraph : paragraphs) {
                String text = paragraph.getText().trim();
                paragraphCount++;
                
                if (text.isEmpty()) {
                    System.out.println("段落 #" + paragraphCount + ": [空]");
                    continue;
                }
                
                System.out.println("段落 #" + paragraphCount + ": " + text);
                
                // 检查是否是新的段落标题
                if (isSection(text)) {
                    System.out.println("识别到段落标题: " + text);
                    // 处理上一个段落的内容
                    processSection(currentSection, contentBuilder.toString(), resume, 
                                 workExperiences, projectExperiences);
                    // 重置内容
                    currentSection = text;
                    contentBuilder = new StringBuilder();
                } else {
                    // 累积内容
                    contentBuilder.append(text).append("\n");
                }
            }
            
            // 处理最后一个段落
            System.out.println("处理最后一个段落: " + currentSection);
            processSection(currentSection, contentBuilder.toString(), resume, 
                         workExperiences, projectExperiences);
            
            resume.setWorkExperiences(workExperiences);
            resume.setProjectExperiences(projectExperiences);
            
            System.out.println("解析完成, 工作经历: " + workExperiences.size() + ", 项目经历: " + projectExperiences.size());
        }
    }
    
    /**
     * 解析.doc文件
     */
    private static void parseDoc(File file, Resume resume) throws Exception {
        try (FileInputStream fis = new FileInputStream(file);
             HWPFDocument document = new HWPFDocument(fis)) {
            
            Range range = document.getRange();
            String text = range.text();
            System.out.println("提取的文本内容长度: " + text.length());
            
            // 按段落分割文本
            String[] paragraphs = text.split("\\r\\n");
            System.out.println("文档中包含 " + paragraphs.length + " 个段落");
            
            List<WorkExperience> workExperiences = new ArrayList<>();
            List<ProjectExperience> projectExperiences = new ArrayList<>();
            
            String currentSection = "";
            StringBuilder contentBuilder = new StringBuilder();
            
            int paragraphCount = 0;
            for (String paragraph : paragraphs) {
                String line = paragraph.trim();
                paragraphCount++;
                
                if (line.isEmpty()) {
                    System.out.println("段落 #" + paragraphCount + ": [空]");
                    continue;
                }
                
                System.out.println("段落 #" + paragraphCount + ": " + line);
                
                if (isSection(line)) {
                    System.out.println("识别到段落标题: " + line);
                    processSection(currentSection, contentBuilder.toString(), resume, 
                                 workExperiences, projectExperiences);
                    currentSection = line;
                    contentBuilder = new StringBuilder();
                } else {
                    contentBuilder.append(line).append("\n");
                }
            }
            
            System.out.println("处理最后一个段落: " + currentSection);
            processSection(currentSection, contentBuilder.toString(), resume, 
                         workExperiences, projectExperiences);
            
            resume.setWorkExperiences(workExperiences);
            resume.setProjectExperiences(projectExperiences);
            
            System.out.println("解析完成, 工作经历: " + workExperiences.size() + ", 项目经历: " + projectExperiences.size());
        }
    }
    
    /**
     * 判断是否是段落标题
     */
    private static boolean isSection(String text) {
        // 扩展段落标题识别
        String[] sections = {
            "基本信息", "个人信息", "个人资料", "简历", "简介", "个人简介", 
            "工作经历", "工作经验", "专业", "从业经历", "职业经历",
            "项目经历", "项目经验", "主要项目", "项目描述", "参与项目",
            "教育背景", "教育经历", "学历", "培训", "培训经历",
            "技能", "技能标签", "专业技能", "技术技能", 
            "资质", "资质认证", "证书", "证书资质",
            "自我评价", "其他说明"
        };
        
        // 匹配段落标题
        for (String section : sections) {
            if (text.contains(section)) {
                System.out.println("匹配到段落标题: " + section + " 在文本: " + text);
                return true;
            }
        }
        
        // 检查标题格式：数字+点+文字，如"1. 个人信息"或"一、个人信息"
        if (text.matches("^\\d+\\..*") || 
            text.matches("^[一二三四五六七八九十]+[、.].*")) {
            System.out.println("匹配到编号式段落标题: " + text);
            return true;
        }
        
        return false;
    }
    
    /**
     * 处理段落内容
     */
    private static void processSection(String section, String content, Resume resume,
                                     List<WorkExperience> workExperiences,
                                     List<ProjectExperience> projectExperiences) {
        if (section.isEmpty() || content.isEmpty()) {
            return;
        }
        
        System.out.println("处理段落: [" + section + "], 内容长度: " + content.length());
        
        switch (section) {
            case "基本信息":
                parseBasicInfo(content, resume);
                break;
            case "个人简介":
                resume.setPersonalSummary(content.trim());
                break;
            case "工作经历":
                List<WorkExperience> parsed = parseWorkExperience(content);
                System.out.println("解析出 " + parsed.size() + " 条工作经历");
                workExperiences.addAll(parsed);
                break;
            case "项目经历":
                List<ProjectExperience> projects = parseProjectExperience(content);
                System.out.println("解析出 " + projects.size() + " 条项目经历");
                projectExperiences.addAll(projects);
                break;
            case "技能标签":
                resume.setSkillTags(content.trim());
                break;
            case "资质认证":
                resume.setCertification(content.trim());
                break;
            case "培训经历":
                resume.setTraining(content.trim());
                break;
            default:
                System.out.println("未知段落类型: " + section);
                break;
        }
    }
    
    /**
     * 解析基本信息
     */
    private static void parseBasicInfo(String content, Resume resume) {
        System.out.println("解析基本信息: " + content);
        String[] lines = content.split("\n");
        for (String line : lines) {
            String[] parts = line.split("[:：]", 2);
            if (parts.length != 2) {
                System.out.println("  无法解析行: " + line);
                continue;
            }
            
            String key = parts[0].trim();
            String value = parts[1].trim();
            System.out.println("  解析到键值对: [" + key + "] = [" + value + "]");
            
            switch (key) {
                case "姓名":
                    resume.setName(value);
                    break;
                case "毕业时间":
                    resume.setGraduationDate(value);
                    break;
                case "专业":
                    resume.setMajor(value);
                    break;
                case "所在部门":
                    resume.setDepartment(value);
                    break;
                case "工作年限":
                    resume.setWorkYears(value);
                    break;
                case "毕业学校":
                    resume.setSchool(value);
                    break;
                case "最高学历":
                    resume.setEducation(value);
                    break;
                case "职称":
                    resume.setTitle(value);
                    break;
                default:
                    System.out.println("  未知的基本信息字段: " + key);
                    break;
            }
        }
    }
    
    /**
     * 解析工作经历
     */
    private static List<WorkExperience> parseWorkExperience(String content) {
        System.out.println("解析工作经历内容: " + content);
        List<WorkExperience> experiences = new ArrayList<>();
        String[] blocks = content.split("\n\n");
        System.out.println("工作经历包含 " + blocks.length + " 个块");
        
        for (int i = 0; i < blocks.length; i++) {
            String block = blocks[i];
            if (block.trim().isEmpty()) {
                System.out.println("  跳过空块 #" + i);
                continue;
            }
            
            System.out.println("  处理工作经历块 #" + i + ": " + block);
            WorkExperience exp = new WorkExperience();
            String[] lines = block.split("\n");
            
            // 解析时间段
            Pattern timePattern = Pattern.compile("(\\d{4}\\.\\d{2})\\s*[-~至]\\s*(\\d{4}\\.\\d{2}|至今)");
            Matcher matcher = timePattern.matcher(lines[0]);
            if (matcher.find()) {
                exp.setStartDate(matcher.group(1));
                exp.setEndDate(matcher.group(2));
                System.out.println("    解析到时间段: " + exp.getStartDate() + " - " + exp.getEndDate());
            } else {
                System.out.println("    无法解析时间段: " + lines[0]);
            }
            
            // 解析公司和职位
            if (lines.length > 1) {
                String[] companyInfo = lines[1].split("\\s+");
                if (companyInfo.length >= 2) {
                    exp.setCompany(companyInfo[0]);
                    exp.setPosition(companyInfo[1]);
                    System.out.println("    解析到公司和职位: " + exp.getCompany() + " " + exp.getPosition());
                } else {
                    System.out.println("    无法解析公司和职位: " + lines[1]);
                }
            }
            
            // 解析工作描述
            StringBuilder description = new StringBuilder();
            for (int j = 2; j < lines.length; j++) {
                description.append(lines[j]).append("\n");
            }
            exp.setDescription(description.toString().trim());
            System.out.println("    工作描述长度: " + exp.getDescription().length());
            
            experiences.add(exp);
        }
        
        return experiences;
    }
    
    /**
     * 解析项目经历
     */
    private static List<ProjectExperience> parseProjectExperience(String content) {
        System.out.println("解析项目经历内容: " + content);
        List<ProjectExperience> experiences = new ArrayList<>();
        String[] blocks = content.split("\n\n");
        System.out.println("项目经历包含 " + blocks.length + " 个块");
        
        for (int i = 0; i < blocks.length; i++) {
            String block = blocks[i];
            if (block.trim().isEmpty()) {
                System.out.println("  跳过空块 #" + i);
                continue;
            }
            
            System.out.println("  处理项目经历块 #" + i + ": " + block);
            ProjectExperience exp = new ProjectExperience();
            String[] lines = block.split("\n");
            
            // 解析时间段
            Pattern timePattern = Pattern.compile("(\\d{4}\\.\\d{2})\\s*[-~至]\\s*(\\d{4}\\.\\d{2}|至今)");
            Matcher matcher = timePattern.matcher(lines[0]);
            if (matcher.find()) {
                exp.setStartDate(matcher.group(1));
                exp.setEndDate(matcher.group(2));
                System.out.println("    解析到时间段: " + exp.getStartDate() + " - " + exp.getEndDate());
            } else {
                System.out.println("    无法解析时间段: " + lines[0]);
            }
            
            // 解析项目名称和角色
            if (lines.length > 1) {
                String[] projectInfo = lines[1].split("\\s+");
                if (projectInfo.length >= 2) {
                    exp.setProjectName(projectInfo[0]);
                    exp.setRole(projectInfo[1]);
                    System.out.println("    解析到项目和角色: " + exp.getProjectName() + " " + exp.getRole());
                } else {
                    System.out.println("    无法解析项目和角色: " + lines[1]);
                }
            }
            
            // 解析项目描述
            StringBuilder description = new StringBuilder();
            for (int j = 2; j < lines.length; j++) {
                description.append(lines[j]).append("\n");
            }
            exp.setDescription(description.toString().trim());
            System.out.println("    项目描述长度: " + exp.getDescription().length());
            
            experiences.add(exp);
        }
        
        return experiences;
    }
} 
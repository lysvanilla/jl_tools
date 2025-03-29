package cn.resume;

import cn.resume.entity.ProjectExperience;
import cn.resume.entity.Resume;
import cn.resume.entity.WorkExperience;
import org.apache.poi.xwpf.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class TableBasedResumeParser {
    
    /**
     * 解析Word文档中的表格简历
     */
    public static Resume parseResume(String filePath) {
        Resume resume = new Resume();
        
        try {
            System.out.println("开始解析表格式简历: " + filePath);
            File file = new File(filePath);
            
            if (!file.exists()) {
                System.out.println("文件不存在: " + filePath);
                return null;
            }
            
            try (FileInputStream fis = new FileInputStream(file);
                 XWPFDocument document = new XWPFDocument(fis)) {
                
                List<XWPFTable> tables = document.getTables();
                System.out.println("文档中包含 " + tables.size() + " 个表格");
                
                if (tables.isEmpty()) {
                    System.out.println("文档中不包含表格，无法解析");
                    return null;
                }
                
                // 处理第一个表格，它通常包含所有简历信息
                XWPFTable mainTable = tables.get(0);
                List<XWPFTableRow> rows = mainTable.getRows();
                System.out.println("主表格包含 " + rows.size() + " 行");
                
                // 解析基本信息
                parseBasicInfo(rows, resume);
                
                // 解析工作经历
                List<WorkExperience> workExperiences = parseWorkExperience(rows);
                resume.setWorkExperiences(workExperiences);
                
                // 解析项目经历
                List<ProjectExperience> projectExperiences = parseProjectExperience(rows);
                resume.setProjectExperiences(projectExperiences);
                
                System.out.println("简历解析完成");
                return resume;
                
            } catch (Exception e) {
                System.out.println("解析文件时发生错误: " + e.getMessage());
                e.printStackTrace();
            }
            
        } catch (Exception e) {
            System.out.println("打开文件时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * 从单元格获取保留换行符的完整文本
     * 
     * @param cell 表格单元格
     * @return 包含原始换行符的文本
     */
    private static String getCellTextWithLineBreaks(XWPFTableCell cell) {
        if (cell == null) return "";
        
        List<XWPFParagraph> paragraphs = cell.getParagraphs();
        if (paragraphs == null || paragraphs.isEmpty()) return "";
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < paragraphs.size(); i++) {
            if (i > 0) {
                sb.append("\n"); // 在段落之间添加换行符
            }
            sb.append(paragraphs.get(i).getText());
        }
        
        return sb.toString().trim();
    }
    
    /**
     * 解析基本信息
     */
    private static void parseBasicInfo(List<XWPFTableRow> rows, Resume resume) {
        System.out.println("开始解析基本信息...");
        
        // 对表格的每一行进行遍历
        for (XWPFTableRow row : rows) {
            List<XWPFTableCell> cells = row.getTableCells();
            if (cells.size() < 2) continue;
            
            String fieldName = getCellTextWithLineBreaks(cells.get(0));
            
            // 处理基本信息字段
            if (fieldName.startsWith("姓") && fieldName.endsWith("名")) {
                if (cells.size() >= 2) {
                    resume.setName(getCellTextWithLineBreaks(cells.get(1)));
                    System.out.println("解析到姓名: " + resume.getName());
                }
            } else if (fieldName.contains("毕业时间")) {
                if (cells.size() >= 2) {
                    resume.setGraduationDate(getCellTextWithLineBreaks(cells.get(1)));
                    System.out.println("解析到毕业时间: " + resume.getGraduationDate());
                }
            } else if (fieldName.startsWith("专")&&fieldName.endsWith("业")) {
                if (cells.size() >= 2) {
                    resume.setMajor(getCellTextWithLineBreaks(cells.get(1)));
                    System.out.println("解析到专业: " + resume.getMajor());
                }
            } else if (fieldName.contains("所在部门")) {
                if (cells.size() >= 2) {
                    resume.setDepartment(getCellTextWithLineBreaks(cells.get(1)));
                    System.out.println("解析到所在部门: " + resume.getDepartment());
                }
            } else if (fieldName.contains("个人简介") || fieldName.contains("个人总结")) {
                if (cells.size() >= 2) {
                    resume.setPersonalSummary(getCellTextWithLineBreaks(cells.get(1)));
                    System.out.println("解析到个人简介: " + (resume.getPersonalSummary().length() > 50 ? 
                        resume.getPersonalSummary().substring(0, 50) + "..." : resume.getPersonalSummary()));
                }
            } else if (fieldName.contains("业务与技术能力") || fieldName.contains("技术技能")) {
                if (cells.size() >= 2) {
                    resume.setTechnicalSkills(getCellTextWithLineBreaks(cells.get(1)));
                    System.out.println("解析到业务与技术能力: " + (resume.getTechnicalSkills().length() > 50 ? 
                        resume.getTechnicalSkills().substring(0, 50) + "..." : resume.getTechnicalSkills()));
                }
            } else if (fieldName.contains("资质") || fieldName.contains("认证")) {
                if (cells.size() >= 2) {
                    resume.setCertification(getCellTextWithLineBreaks(cells.get(1)));
                    System.out.println("解析到资质认证: " + (resume.getCertification().length() > 50 ? 
                        resume.getCertification().substring(0, 50) + "..." : resume.getCertification()));
                }
            } else if (fieldName.contains("参与培训")) {
                if (cells.size() >= 2) {
                    resume.setTraining(getCellTextWithLineBreaks(cells.get(1)));
                    System.out.println("解析到参与培训: " + (resume.getTraining().length() > 50 ? 
                        resume.getTraining().substring(0, 50) + "..." : resume.getTraining()));
                }
            } else if (fieldName.contains("技能标签")) {
                if (cells.size() >= 2) {
                    resume.setSkillTags(getCellTextWithLineBreaks(cells.get(1)));
                    System.out.println("解析到技能标签: " + (resume.getSkillTags().length() > 50 ? 
                        resume.getSkillTags().substring(0, 50) + "..." : resume.getSkillTags()));
                }
            } else if (fieldName.contains("毕业学校")) {
                if (cells.size() >= 2) {
                    resume.setSchool(getCellTextWithLineBreaks(cells.get(1)));
                    System.out.println("解析到毕业学校: " + resume.getSchool());
                }
            } else if (fieldName.contains("最高学历")) {
                if (cells.size() >= 2) {
                    resume.setEducation(getCellTextWithLineBreaks(cells.get(1)));
                    System.out.println("解析到最高学历: " + resume.getEducation());
                }
            } else if (fieldName.startsWith("职") && fieldName.endsWith("称")) {
                if (cells.size() >= 2) {
                    resume.setTitle(getCellTextWithLineBreaks(cells.get(1)));
                    System.out.println("解析到职称: " + resume.getTitle());
                }
            }
            
            if (cells.size() >= 4) {
                String rightFieldName = getCellTextWithLineBreaks(cells.get(2));
                
                if (rightFieldName.contains("毕业时间")) {
                    resume.setGraduationDate(getCellTextWithLineBreaks(cells.get(3)));
                    System.out.println("解析到毕业时间(右侧): " + resume.getGraduationDate());
                } else if (rightFieldName.contains("工作年限")) {
                    resume.setWorkYears(getCellTextWithLineBreaks(cells.get(3)));
                    System.out.println("解析到工作年限(右侧): " + resume.getWorkYears());
                } else if (rightFieldName.contains("最高学历")) {
                    resume.setEducation(getCellTextWithLineBreaks(cells.get(3)));
                    System.out.println("解析到最高学历(右侧): " + resume.getEducation());
                } else if (rightFieldName.contains("毕业学校")) {
                    resume.setSchool(getCellTextWithLineBreaks(cells.get(3)));
                    System.out.println("解析到毕业学校(右侧): " + resume.getSchool());
                } else if (rightFieldName.contains("职") && rightFieldName.contains("称")) {
                    resume.setTitle(getCellTextWithLineBreaks(cells.get(3)));
                    System.out.println("解析到职称(右侧): " + resume.getTitle());
                }
            }
        }
    }
    
    /**
     * 解析工作经历
     */
    private static List<WorkExperience> parseWorkExperience(List<XWPFTableRow> rows) {
        System.out.println("开始解析工作经历...");
        List<WorkExperience> experiences = new ArrayList<>();
        
        boolean inWorkExperienceSection = false;
        boolean inWorkExperienceHeader = false;
        int startTimeIndex = -1;
        int endTimeIndex = -1;
        int companyIndex = -1;
        int positionIndex = -1;
        int descriptionIndex = -1;
        
        // 对表格的每一行进行遍历
        for (int i = 0; i < rows.size(); i++) {
            XWPFTableRow row = rows.get(i);
            List<XWPFTableCell> cells = row.getTableCells();
            if (cells.isEmpty()) continue;
            
            String cellText = cells.get(0).getText().trim();
            
            // 检查是否到达工作经历部分
            if (cellText.contains("工作经历") || cellText.contains("工作经验")) {
                System.out.println("找到工作经历部分: " + cellText);
                inWorkExperienceSection = true;
                inWorkExperienceHeader = true;
                continue;
            }
            
            // 如果已经到达下一个主要部分，则退出工作经历解析
            if (inWorkExperienceSection && (cellText.contains("项目经历") || 
                                           cellText.contains("项目经验") || 
                                           cellText.contains("教育背景") || 
                                           cellText.contains("培训经历"))) {
                System.out.println("工作经历部分结束，找到新部分: " + cellText);
                break;
            }
            
            // 根据用户的说明，直接设置列的顺序
            if (inWorkExperienceSection && inWorkExperienceHeader) {
                // 假设表头行的列顺序是固定的：开始时间，结束时间，公司名称，职位，工作描述
                if (cells.size() >= 5) {
                    startTimeIndex = 0;    // 第1列：开始时间
                    endTimeIndex = 1;      // 第2列：结束时间
                    companyIndex = 2;      // 第3列：公司名称
                    positionIndex = 3;     // 第4列：职位
                    descriptionIndex = 4;  // 第5列：工作描述
                    
                    System.out.println("工作经历表头行识别: 固定5列格式");
                    System.out.println("列1(开始时间), 列2(结束时间), 列3(公司名称), 列4(职位), 列5(工作描述)");
                } else {
                    // 为防止表头行格式不同，仍然进行检查
                    for (int j = 0; j < cells.size(); j++) {
                        String headerText = cells.get(j).getText().trim();
                        System.out.println("  检查表头单元格[" + j + "]: '" + headerText + "'");
                        if (headerText.contains("开始时间") || headerText.contains("起始时间") || headerText.contains("开始")) {
                            startTimeIndex = j;
                        } else if (headerText.contains("结束时间") || headerText.contains("终止时间") || headerText.contains("结束")) {
                            endTimeIndex = j;
                        } else if (headerText.contains("公司") || headerText.contains("单位")) {
                            companyIndex = j;
                        } else if (headerText.contains("职务") || headerText.contains("职位") || headerText.contains("担任")) {
                            positionIndex = j;
                        } else if (headerText.contains("工作职责") || headerText.contains("描述") || headerText.contains("说明")) {
                            descriptionIndex = j;
                        }
                    }
                    
                    System.out.println("工作经历标题行解析完成: 开始时间(" + startTimeIndex + 
                                    "), 结束时间(" + endTimeIndex + 
                                    "), 公司(" + companyIndex + 
                                    "), 职位(" + positionIndex + 
                                    "), 描述(" + descriptionIndex + ")");
                }
                
                inWorkExperienceHeader = false;
                continue;
            }
            
            // 解析工作经历数据行
            if (inWorkExperienceSection && !inWorkExperienceHeader && cells.size() >= 3) {
                WorkExperience experience = new WorkExperience();
                StringBuilder debugInfo = new StringBuilder("解析工作行: ");
                
                // 获取开始时间
                if (startTimeIndex >= 0 && startTimeIndex < cells.size()) {
                    String startDate = getCellTextWithLineBreaks(cells.get(startTimeIndex));
                    experience.setStartDate(startDate);
                    debugInfo.append("开始时间=").append(startDate).append(", ");
                }
                
                // 获取结束时间
                if (endTimeIndex >= 0 && endTimeIndex < cells.size()) {
                    String endDate = getCellTextWithLineBreaks(cells.get(endTimeIndex));
                    experience.setEndDate(endDate);
                    debugInfo.append("结束时间=").append(endDate).append(", ");
                }
                
                // 获取公司名称
                if (companyIndex >= 0 && companyIndex < cells.size()) {
                    String company = getCellTextWithLineBreaks(cells.get(companyIndex));
                    experience.setCompany(company);
                    debugInfo.append("公司=").append(company).append(", ");
                }
                
                // 获取职位
                if (positionIndex >= 0 && positionIndex < cells.size()) {
                    String position = getCellTextWithLineBreaks(cells.get(positionIndex));
                    experience.setPosition(position);
                    debugInfo.append("职位=").append(position).append(", ");
                }
                
                // 获取描述
                if (descriptionIndex >= 0 && descriptionIndex < cells.size()) {
                    String description = getCellTextWithLineBreaks(cells.get(descriptionIndex));
                    experience.setDescription(description);
                    debugInfo.append("描述=").append(description.length() > 20 ? description.substring(0, 20) + "..." : description);
                }
                
                System.out.println(debugInfo.toString());
                
                // 只添加有效的工作经历
                if ((experience.getStartDate() != null && !experience.getStartDate().isEmpty()) &&
                    (experience.getCompany() != null && !experience.getCompany().isEmpty() ||
                     cells.size() >= 3 && !cells.get(2).getText().trim().isEmpty())) {
                    
                    // 如果公司名称为空但第3列有内容，使用第3列作为公司名称
                    if ((experience.getCompany() == null || experience.getCompany().isEmpty()) &&
                        cells.size() > 2 && !cells.get(2).getText().trim().isEmpty()) {
                        experience.setCompany(cells.get(2).getText().trim());
                    }
                    
                    experiences.add(experience);
                    System.out.println("添加工作经历: " + experience.getCompany() + " (" + 
                                     experience.getStartDate() + " - " + experience.getEndDate() + ")");
                }
            }
        }
        
        System.out.println("完成工作经历解析，共找到 " + experiences.size() + " 条经历");
        return experiences;
    }
    
    /**
     * 解析项目经历
     */
    private static List<ProjectExperience> parseProjectExperience(List<XWPFTableRow> rows) {
        System.out.println("开始解析项目经历...");
        List<ProjectExperience> experiences = new ArrayList<>();
        
        boolean inProjectExperienceSection = false;
        boolean inProjectExperienceHeader = false;
        int startTimeIndex = -1;
        int endTimeIndex = -1;
        int projectNameIndex = -1;
        int roleIndex = -1;
        int descriptionIndex = -1;
        
        // 对表格的每一行进行遍历
        for (int i = 0; i < rows.size(); i++) {
            XWPFTableRow row = rows.get(i);
            List<XWPFTableCell> cells = row.getTableCells();
            if (cells.isEmpty()) continue;
            
            String cellText = cells.get(0).getText().trim();
            
            // 检查是否到达项目经历部分
            if (cellText.contains("项目经历") || cellText.contains("项目经验") || cellText.contains("参与项目")) {
                System.out.println("找到项目经历部分: " + cellText);
                inProjectExperienceSection = true;
                inProjectExperienceHeader = true;
                continue;
            }
            
            // 如果已经到达下一个主要部分，则退出项目经历解析
            if (inProjectExperienceSection && (cellText.contains("教育背景") || 
                                              cellText.contains("教育经历") || 
                                              cellText.contains("培训经历") || 
                                              cellText.contains("能力与资质") ||
                                              cellText.contains("技能标签"))) {
                System.out.println("项目经历部分结束，找到新部分: " + cellText);
                break;
            }
            
            // 根据用户的说明，直接设置列的顺序
            if (inProjectExperienceSection && inProjectExperienceHeader) {
                // 假设表头行的列顺序是固定的：开始时间，结束时间，项目名称，项目角色，项目职责说明
                if (cells.size() >= 5) {
                    startTimeIndex = 0;    // 第1列：开始时间
                    endTimeIndex = 1;      // 第2列：结束时间
                    projectNameIndex = 2;  // 第3列：项目名称
                    roleIndex = 3;         // 第4列：项目角色
                    descriptionIndex = 4;  // 第5列：项目职责说明
                    
                    System.out.println("项目经历表头行识别: 固定5列格式");
                    System.out.println("列1(开始时间), 列2(结束时间), 列3(项目名称), 列4(项目角色), 列5(项目职责说明)");
                } else {
                    // 为防止表头行格式不同，仍然进行检查
                    for (int j = 0; j < cells.size(); j++) {
                        String headerText = cells.get(j).getText().trim();
                        System.out.println("  检查表头单元格[" + j + "]: '" + headerText + "'");
                        if (headerText.contains("开始时间") || headerText.contains("起始时间") || headerText.contains("开始")) {
                            startTimeIndex = j;
                        } else if (headerText.contains("结束时间") || headerText.contains("终止时间") || headerText.contains("结束")) {
                            endTimeIndex = j;
                        } else if (headerText.contains("项目名称") || (headerText.contains("项目") && !headerText.contains("角色") && !headerText.contains("职责"))) {
                            projectNameIndex = j;
                        } else if (headerText.contains("担任角色") || headerText.contains("角色") || headerText.contains("职责")) {
                            roleIndex = j;
                        } else if (headerText.contains("项目描述") || headerText.contains("描述") || headerText.contains("说明")) {
                            descriptionIndex = j;
                        }
                    }
                    
                    System.out.println("项目经历标题行解析完成: 开始时间(" + startTimeIndex + 
                                     "), 结束时间(" + endTimeIndex + 
                                     "), 项目名称(" + projectNameIndex + 
                                     "), 角色(" + roleIndex + 
                                     "), 描述(" + descriptionIndex + ")");
                }
                
                inProjectExperienceHeader = false;
                continue;
            }
            
            // 解析项目经历数据行
            if (inProjectExperienceSection && !inProjectExperienceHeader && cells.size() >= 3) {
                ProjectExperience experience = new ProjectExperience();
                StringBuilder debugInfo = new StringBuilder("解析项目行: ");
                
                // 获取开始时间
                if (startTimeIndex >= 0 && startTimeIndex < cells.size()) {
                    String startDate = getCellTextWithLineBreaks(cells.get(startTimeIndex));
                    experience.setStartDate(startDate);
                    debugInfo.append("开始时间=").append(startDate).append(", ");
                }
                
                // 获取结束时间
                if (endTimeIndex >= 0 && endTimeIndex < cells.size()) {
                    String endDate = getCellTextWithLineBreaks(cells.get(endTimeIndex));
                    experience.setEndDate(endDate);
                    debugInfo.append("结束时间=").append(endDate).append(", ");
                }
                
                // 获取项目名称
                if (projectNameIndex >= 0 && projectNameIndex < cells.size()) {
                    String projectName = getCellTextWithLineBreaks(cells.get(projectNameIndex));
                    experience.setProjectName(projectName);
                    debugInfo.append("项目名称=").append(projectName).append(", ");
                }
                
                // 获取角色
                if (roleIndex >= 0 && roleIndex < cells.size()) {
                    String role = getCellTextWithLineBreaks(cells.get(roleIndex));
                    experience.setRole(role);
                    debugInfo.append("角色=").append(role).append(", ");
                }
                
                // 获取描述
                if (descriptionIndex >= 0 && descriptionIndex < cells.size()) {
                    String description = getCellTextWithLineBreaks(cells.get(descriptionIndex));
                    experience.setDescription(description);
                    debugInfo.append("描述=").append(description.length() > 20 ? description.substring(0, 20) + "..." : description);
                }
                
                System.out.println(debugInfo.toString());
                
                // 只添加有效的项目经历
                if ((experience.getStartDate() != null && !experience.getStartDate().isEmpty()) && 
                    (experience.getProjectName() != null && !experience.getProjectName().isEmpty() || 
                     cells.size() >= 3 && !cells.get(2).getText().trim().isEmpty())) {
                    
                    // 如果项目名称为空但第3列有内容，使用第3列作为项目名称
                    if ((experience.getProjectName() == null || experience.getProjectName().isEmpty()) && 
                        cells.size() > 2 && !cells.get(2).getText().trim().isEmpty()) {
                        experience.setProjectName(cells.get(2).getText().trim());
                    }
                    
                    experiences.add(experience);
                    System.out.println("添加项目经历: " + experience.getProjectName() + " (" + 
                                     experience.getStartDate() + " - " + experience.getEndDate() + ")");
                }
            }
        }
        
        System.out.println("完成项目经历解析，共找到 " + experiences.size() + " 条经历");
        return experiences;
    }
} 
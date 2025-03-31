package cn.resume;

import cn.hutool.core.io.FileUtil;
import cn.idev.excel.ExcelWriter;
import cn.idev.excel.FastExcel;
import cn.idev.excel.write.metadata.WriteSheet;
import cn.resume.entity.ProjectExperience;
import cn.resume.entity.Resume;
import cn.resume.entity.WorkExperience;
import cn.sunline.util.BasicInfo;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * IndexExcelWrite 类用于将指定 Excel 文件中的指标信息进行过滤，并将过滤后的数据写入到 Excel 模板文件中。
 * 整个过程包括读取源文件、过滤数据、写入模板文件等步骤，同时会记录详细的日志信息。
 */
@Slf4j
public class ResumeExcelWrite {
    // 定义模板文件路径，使用 BasicInfo 类中的 tpl_path 拼接模板文件所在目录和文件名
    private static final String TPL_PATH = BasicInfo.TPL_PATH + "excel" + File.separator + "简历清单模板.xlsx";
    // 定义基础导出路径，使用 BasicInfo 类的方法获取
    private static final String BASIC_EXPORT_PATH = BasicInfo.getBasicExportPath("");

    public static void main(String[] args) {
        String filePath = "D:\\BaiduSyncdisk\\工作目录\\商机\\202503中国银行湖南分行\\简历";
        List<File> files = FileUtil.loopFiles(filePath);
        List<Resume> resumeList = new ArrayList<>();
        for (File file:files){
            String fileName = file.getAbsolutePath();
            Resume resume = TableBasedResumeParser.parseResume(fileName);
            List<WorkExperience> workExperiences = resume.getWorkExperiences();
            List<ProjectExperience> projectExperiences = resume.getProjectExperiences();
            
            // 拼接工作经历
            resume.setWorkExperiencesStr(buildWorkExperiencesStr(workExperiences));
            resume.setProjectExperiencesStr(buildProjectExperiencesStr(projectExperiences));

            resumeList.add(resume);
        }
        writeIndexExcel(resumeList, TPL_PATH, BASIC_EXPORT_PATH + "简历清单梳理.xlsx");
        System.out.println("11");
    }

    /**
     * 构建工作经历字符串
     * @param workExperiences 工作经历列表
     * @return 拼接后的工作经历字符串
     */
    private static String buildWorkExperiencesStr(List<WorkExperience> workExperiences) {
        StringBuilder workExperiencesStr = new StringBuilder();
        for (WorkExperience work : workExperiences) {
            workExperiencesStr.append(work.getStartDate())
                .append(" - ")
                .append(work.getEndDate())
                .append(" | ")
                .append(work.getCompany())
                .append(" | ")
                .append(work.getPosition())
                .append(" | ")
                .append(work.getDescription())
                .append("\n");
        }
        return workExperiencesStr.toString();
    }

    /**
     * 构建项目经历字符串
     * @param projectExperiences 项目经历列表
     * @return 拼接后的项目经历字符串
     */
    private static String buildProjectExperiencesStr(List<ProjectExperience> projectExperiences) {
        StringBuilder projectExperiencesStr = new StringBuilder();
        for (ProjectExperience project : projectExperiences) {
            projectExperiencesStr.append(project.getStartDate())
                .append(" - ")
                .append(project.getEndDate())
                .append(" | ")
                .append(project.getProjectName())
                .append(" | ")
                .append(project.getRole())
                //.append(" | ")
                //.append(project.getDescription())
                .append("\n");
        }
        return projectExperiencesStr.toString();
    }

    public static void writeIndexExcel(List<Resume> resumeList, String templatePath, String outputPath) {
        // 创建模板文件和输出文件的 File 对象
        File templateFile = new File(templatePath);
        File outputFile = new File(outputPath);

        // 检查模板文件是否存在
        if (!templateFile.exists()) {
            // 若不存在，记录错误日志
            log.error("Excel 模板文件不存在，路径：{}", templatePath);
            return;
        }

        ExcelWriter excelWriter = null;
        try {
            // 创建 ExcelWriter 对象，使用模板文件进行写入操作
            excelWriter = FastExcel.write(outputPath).withTemplate(templatePath).build();
            // 创建写入工作表对象，指定工作表名称为 "指标数据"
            WriteSheet task_sheet = FastExcel.writerSheet("简历信息").build();
            // 记录开始向 Excel 文件写入数据的日志，包含指标信息数量和输出路径
            log.info("开始向 Excel 文件写入 {} 条指标信息，输出路径：{}", resumeList.size(), outputPath);
            excelWriter.fill(resumeList, task_sheet);
            // 记录成功向 Excel 文件写入数据的日志，包含输出路径
            log.info("成功向 Excel 文件写入数据，输出路径：{}", outputPath);
        } catch (Exception e) {
            // 记录写入 Excel 文件时出现异常的日志，包含输出路径和异常信息
            log.error("写入 Excel 文件时出现异常，输出路径：{}", outputPath, e);
        } finally {
            if (excelWriter != null) {
                try {
                    // 关闭 ExcelWriter 对象
                    excelWriter.close();
                } catch (Exception e) {
                    // 记录关闭 ExcelWriter 对象时出现异常的日志，包含输出路径和异常信息
                    log.error("关闭 ExcelWriter 时出现异常，输出路径：{}", outputPath, e);
                }
            }
        }
        // 记录转换成功的日志，包含输出路径
        log.info("转换成功：[{}]", outputPath);
    }
}
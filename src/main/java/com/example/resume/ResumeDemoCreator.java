package com.example.resume;

import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblWidth;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth;

import java.io.FileOutputStream;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

/**
 * 简历模板创建器 - 用于创建一个简单的Word模板文件
 */
public class ResumeDemoCreator {

    public static void main(String[] args) {
        String templatePath = "D:\\projects\\jl_tools\\logs\\简历模版.docx";
        
        try {
            System.out.println("正在创建简历模板示例文件...");
            createTemplateFile(templatePath);
            System.out.println("模板文件已创建: " + templatePath);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("创建模板文件时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 创建一个简单的Word模板文件
     */
    private static void createTemplateFile(String filePath) throws Exception {
        XWPFDocument document = new XWPFDocument();
        
        // 添加标题
        XWPFParagraph title = document.createParagraph();
        title.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun titleRun = title.createRun();
        titleRun.setText("个人简历");
        titleRun.setBold(true);
        titleRun.setFontSize(16);
        
        // 添加基本信息表格
        XWPFTable infoTable = document.createTable(5, 4);
        setTableWidth(infoTable, 9000);
        
        // 设置基本信息表格内容
        setCellText(infoTable, 0, 0, "姓名");
        setCellText(infoTable, 0, 1, "{{name}}");
        setCellText(infoTable, 0, 2, "本单位职务");
        setCellText(infoTable, 0, 3, "{{title}}");
        
        setCellText(infoTable, 1, 0, "学历");
        setCellText(infoTable, 1, 1, "{{education}}");
        setCellText(infoTable, 1, 2, "出生年月");
        setCellText(infoTable, 1, 3, "{{birthMonth}}");
        
        setCellText(infoTable, 2, 0, "本项目角色");
        setCellText(infoTable, 2, 1, "{{projectRole}}");
        setCellText(infoTable, 2, 2, "本单位任职时间");
        setCellText(infoTable, 2, 3, "{{employmentPeriod}}");
        
        setCellText(infoTable, 3, 0, "工作年限");
        setCellText(infoTable, 3, 1, "{{workYears}}");
        setCellText(infoTable, 3, 2, "毕业学校");
        setCellText(infoTable, 3, 3, "{{school}}");
        
        setCellText(infoTable, 4, 0, "专业");
        setCellText(infoTable, 4, 1, "{{major}}");
        setCellText(infoTable, 4, 2, "毕业时间");
        setCellText(infoTable, 4, 3, "{{graduationDate}}");
        
        // 添加空行
        document.createParagraph();
        
        // 添加项目经历标题
        XWPFParagraph projectTitle = document.createParagraph();
        XWPFRun projectTitleRun = projectTitle.createRun();
        projectTitleRun.setText("项目经历");
        projectTitleRun.setBold(true);
        projectTitleRun.setFontSize(14);
        
        // 添加项目经历表格
        XWPFTable projectTable = document.createTable(2, 2);
        setTableWidth(projectTable, 9000);
        
        // 设置项目经历表格表头
        setCellText(projectTable, 0, 0, "项目经验", true);
        setCellText(projectTable, 0, 1, "担任职务", true);
        
        // 设置数据行（模板标记）
        setCellText(projectTable, 1, 0, "{{projectExperiences}}");
        setCellText(projectTable, 1, 1, "");
        
        // 保存文档
        try (FileOutputStream out = new FileOutputStream(filePath)) {
            document.write(out);
        }
    }
    
    /**
     * 设置表格宽度
     */
    private static void setTableWidth(XWPFTable table, int width) {
        CTTblPr tblPr = table.getCTTbl().getTblPr();
        if (tblPr == null) {
            tblPr = table.getCTTbl().addNewTblPr();
        }
        CTTblWidth tblWidth = tblPr.isSetTblW() ? tblPr.getTblW() : tblPr.addNewTblW();
        tblWidth.setW(BigInteger.valueOf(width));
        tblWidth.setType(STTblWidth.DXA);
    }
    
    /**
     * 设置单元格文本
     */
    private static void setCellText(XWPFTable table, int row, int cell, String text) {
        setCellText(table, row, cell, text, false);
    }
    
    /**
     * 设置单元格文本，可设置是否加粗
     */
    private static void setCellText(XWPFTable table, int row, int cell, String text, boolean bold) {
        XWPFTableCell tableCell = table.getRow(row).getCell(cell);
        XWPFParagraph paragraph = tableCell.getParagraphs().get(0);
        XWPFRun run = paragraph.createRun();
        run.setText(text);
        run.setBold(bold);
    }
} 
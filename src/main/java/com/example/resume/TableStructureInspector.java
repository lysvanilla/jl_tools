package com.example.resume;

import org.apache.poi.xwpf.usermodel.*;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;

public class TableStructureInspector {
    public static void main(String[] args) {
        String filePath = "D:\\projects\\jl_tools\\logs\\00603+邹智+工作简历.docx";
        
        try {
            System.out.println("开始详细分析表格结构: " + filePath);
            File file = new File(filePath);
            
            if (!file.exists()) {
                System.out.println("文件不存在: " + filePath);
                return;
            }
            
            try (FileInputStream fis = new FileInputStream(file);
                 XWPFDocument document = new XWPFDocument(fis)) {
                
                List<XWPFTable> tables = document.getTables();
                if (tables.isEmpty()) {
                    System.out.println("文档中不包含表格");
                    return;
                }
                
                XWPFTable mainTable = tables.get(0);
                List<XWPFTableRow> rows = mainTable.getRows();
                System.out.println("表格包含 " + rows.size() + " 行");
                
                // 打印前10行详细内容
                int rowsToPrint = Math.min(rows.size(), 10);
                for (int i = 0; i < rowsToPrint; i++) {
                    XWPFTableRow row = rows.get(i);
                    List<XWPFTableCell> cells = row.getTableCells();
                    
                    System.out.println("\n行 #" + (i+1) + " 包含 " + cells.size() + " 个单元格:");
                    for (int j = 0; j < cells.size(); j++) {
                        XWPFTableCell cell = cells.get(j);
                        String cellText = cell.getText().trim();
                        System.out.println("  单元格 [" + (i+1) + "," + (j+1) + "]: \"" + cellText + "\"");
                        
                        // 检查单元格文本是否包含关键词
                        if (cellText.contains("毕业") || cellText.contains("学校") || 
                            cellText.contains("职称") || cellText.contains("职位")) {
                            System.out.println("    *** 发现关键字段 ***");
                        }
                    }
                }
                
                // 专门查找包含关键词的行
                System.out.println("\n\n查找包含特定关键词的行:");
                for (int i = 0; i < rows.size(); i++) {
                    XWPFTableRow row = rows.get(i);
                    List<XWPFTableCell> cells = row.getTableCells();
                    
                    for (XWPFTableCell cell : cells) {
                        String cellText = cell.getText().trim();
                        if (cellText.contains("毕业学校") || cellText.contains("职称")) {
                            System.out.println("行 #" + (i+1) + " 包含关键词: \"" + cellText + "\"");
                            
                            // 打印同一行的所有单元格
                            System.out.println("  同行单元格:");
                            for (int j = 0; j < cells.size(); j++) {
                                System.out.println("    单元格 " + (j+1) + ": \"" + cells.get(j).getText().trim() + "\"");
                            }
                        }
                    }
                }
                
                System.out.println("\n表格结构分析完成");
            }
            
        } catch (Exception e) {
            System.out.println("分析文件时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 
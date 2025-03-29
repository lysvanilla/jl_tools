package com.example.resume;

import org.apache.poi.xwpf.usermodel.*;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;

public class DocxStructureViewer {
    public static void main(String[] args) {
        String filePath = "D:\\projects\\jl_tools\\logs\\00603+邹智+工作简历.docx";
        
        try {
            System.out.println("开始分析文件: " + filePath);
            File file = new File(filePath);
            
            if (!file.exists()) {
                System.out.println("文件不存在: " + filePath);
                return;
            }
            
            try (FileInputStream fis = new FileInputStream(file);
                 XWPFDocument document = new XWPFDocument(fis)) {
                
                // 1. 分析段落
                List<XWPFParagraph> paragraphs = document.getParagraphs();
                System.out.println("\n文档包含 " + paragraphs.size() + " 个段落");
                
                for (int i = 0; i < paragraphs.size(); i++) {
                    XWPFParagraph paragraph = paragraphs.get(i);
                    String text = paragraph.getText().trim();
                    
                    if (!text.isEmpty()) {
                        System.out.println("段落 #" + (i+1) + " [样式: " + paragraph.getStyle() + 
                                           ", 文本长度: " + text.length() + "]: " + 
                                           (text.length() > 50 ? text.substring(0, 50) + "..." : text));
                        
                        // 查看段落中的样式信息
                        List<XWPFRun> runs = paragraph.getRuns();
                        if (runs.size() > 1) {
                            System.out.println("  - 包含 " + runs.size() + " 个不同样式区域");
                            for (int j = 0; j < runs.size(); j++) {
                                XWPFRun run = runs.get(j);
                                String runText = run.getText(0);
                                if (runText != null && !runText.trim().isEmpty()) {
                                    System.out.println("    样式区域 #" + (j+1) + 
                                                      " [字体: " + run.getFontFamily() + 
                                                      ", 大小: " + run.getFontSize() + 
                                                      ", 加粗: " + run.isBold() + "]: " + 
                                                      (runText.length() > 20 ? runText.substring(0, 20) + "..." : runText));
                                }
                            }
                        }
                    }
                }
                
                // 2. 分析表格
                List<XWPFTable> tables = document.getTables();
                System.out.println("\n文档包含 " + tables.size() + " 个表格");
                
                for (int i = 0; i < tables.size(); i++) {
                    XWPFTable table = tables.get(i);
                    List<XWPFTableRow> rows = table.getRows();
                    System.out.println("表格 #" + (i+1) + " 包含 " + rows.size() + " 行");
                    
                    // 打印表格的前2行数据作为示例
                    int rowsToPrint = Math.min(rows.size(), 2);
                    for (int j = 0; j < rowsToPrint; j++) {
                        XWPFTableRow row = rows.get(j);
                        List<XWPFTableCell> cells = row.getTableCells();
                        
                        System.out.print("  行 #" + (j+1) + ": ");
                        for (XWPFTableCell cell : cells) {
                            String cellText = cell.getText().trim();
                            System.out.print("[" + cellText + "] ");
                        }
                        System.out.println();
                    }
                    if (rows.size() > 2) {
                        System.out.println("  ... 及更多行");
                    }
                }
                
                // 3. 检查文档中的图片
                List<XWPFPictureData> pictures = document.getAllPictures();
                System.out.println("\n文档包含 " + pictures.size() + " 张图片");
                for (int i = 0; i < pictures.size(); i++) {
                    XWPFPictureData picture = pictures.get(i);
                    System.out.println("图片 #" + (i+1) + " [类型: " + picture.getPictureType() + 
                                      ", 大小: " + picture.getData().length + " 字节]");
                }
                
                System.out.println("\n文档结构分析完成");
            }
            
        } catch (Exception e) {
            System.out.println("分析文件时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 
package cn.resume;

import com.deepoove.poi.XWPFTemplate;
import org.apache.poi.xwpf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 批量简历导出器 - 处理目录下的多个简历文件并合并为一个Word文档
 */
public class BatchResumeExporter {
    private static final Logger logger = LoggerFactory.getLogger(BatchResumeExporter.class);

    /**
     * 处理指定目录下的所有简历文件并合并为一个Word文档
     *
     * @param inputDirectory 输入目录路径
     * @param outputPath 输出文件路径
     * @return 是否成功处理
     */
    public static boolean processDirectory(String inputDirectory, String outputPath) {
        File directory = new File(inputDirectory);
        if (!directory.exists() || !directory.isDirectory()) {
            logger.error("输入目录不存在或不是目录: {}", inputDirectory);
            return false;
        }

        // 获取所有.docx文件
        File[] files = directory.listFiles((dir, name) -> name.endsWith(".docx"));
        if (files == null || files.length == 0) {
            logger.error("目录中没有找到.docx文件");
            return false;
        }

        // 处理每个文件并收集XWPFTemplate对象
        List<XWPFTemplate> templates = new ArrayList<>();
        for (File file : files) {
            try {
                logger.info("正在处理文件: {}", file.getName());
                XWPFTemplate template = ResumeWordExporter.exportResumeToWord(file.getAbsolutePath());
                if (template != null) {
                    templates.add(template);
                }
            } catch (Exception e) {
                logger.error("处理文件失败: " + file.getName(), e);
            }
        }

        if (templates.isEmpty()) {
            logger.error("没有成功处理任何文件");
            return false;
        }

        // 合并所有模板到一个文档
        return mergeTemplates(templates, outputPath);
    }

    /**
     * 合并多个XWPFTemplate到一个Word文档
     *
     * @param templates 要合并的模板列表
     * @param outputPath 输出文件路径
     * @return 是否成功合并
     */
    private static boolean mergeTemplates(List<XWPFTemplate> templates, String outputPath) {
        try (XWPFDocument mergedDoc = new XWPFDocument()) {
            for (int i = 0; i < templates.size(); i++) {
                XWPFTemplate template = templates.get(i);
                XWPFDocument doc = template.getXWPFDocument();

                // 如果不是第一个文档，添加分页符
                if (i > 0) {
                    XWPFParagraph paragraph = mergedDoc.createParagraph();
                    paragraph.setPageBreak(true);
                }

                // 复制段落
                for (XWPFParagraph paragraph : doc.getParagraphs()) {
                    XWPFParagraph newParagraph = mergedDoc.createParagraph();
                    newParagraph.setAlignment(paragraph.getAlignment());
                    newParagraph.setStyle(paragraph.getStyle());
                    
                    for (XWPFRun run : paragraph.getRuns()) {
                        XWPFRun newRun = newParagraph.createRun();
                        newRun.setText(run.getText(0));
                        newRun.setBold(run.isBold());
                        newRun.setItalic(run.isItalic());
                        newRun.setUnderline(run.getUnderline());
                    }
                }

                // 复制表格
                for (XWPFTable table : doc.getTables()) {
                    XWPFTable newTable = mergedDoc.createTable();
                    newTable.setWidth("100%");
                    
                    // 复制行和单元格
                    for (XWPFTableRow row : table.getRows()) {
                        XWPFTableRow newRow = newTable.createRow();
                        for (XWPFTableCell cell : row.getTableCells()) {
                            XWPFTableCell newCell = newRow.createCell();
                            newCell.setText(cell.getText());
                        }
                    }
                }

                // 关闭模板
                template.close();
            }

            // 保存合并后的文档
            try (FileOutputStream out = new FileOutputStream(outputPath)) {
                mergedDoc.write(out);
            }
            logger.info("成功合并文档到: {}", outputPath);
            return true;
        } catch (IOException e) {
            logger.error("合并文档失败", e);
            return false;
        }
    }

    public static void main(String[] args) {
        // 测试用例
        String inputDir = "D:\\projects\\jl_tools\\logs\\input";
        String outputPath = "D:\\projects\\jl_tools\\logs\\merged_resumes_" + System.currentTimeMillis() + ".docx";
        
        boolean success = processDirectory(inputDir, outputPath);
        if (success) {
            System.out.println("批量处理完成，输出文件: " + outputPath);
        } else {
            System.out.println("批量处理失败");
        }
    }
} 
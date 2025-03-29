package cn.resume.util;

import org.apache.poi.xwpf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Word文档合并工具类 - 用于合并多个Word文档到一个文档中
 */
public class WordDocumentMerger {
    private static final Logger logger = LoggerFactory.getLogger(WordDocumentMerger.class);

    /**
     * 合并指定目录下的所有Word文档
     *
     * @param inputDirectory 输入目录路径
     * @param outputPath 输出文件路径
     * @param fileExtensions 要处理的文件扩展名列表（例如：".docx", ".doc"）
     * @return 是否成功合并
     */
    public static boolean mergeDirectory(String inputDirectory, String outputPath, String... fileExtensions) {
        File directory = new File(inputDirectory);
        if (!directory.exists() || !directory.isDirectory()) {
            logger.error("输入目录不存在或不是目录: {}", inputDirectory);
            return false;
        }

        // 获取所有指定扩展名的文件
        File[] files = directory.listFiles((dir, name) -> {
            String lowerName = name.toLowerCase();
            return Arrays.stream(fileExtensions)
                    .map(String::toLowerCase)
                    .anyMatch(lowerName::endsWith);
        });

        if (files == null || files.length == 0) {
            logger.error("目录中没有找到指定扩展名的文件");
            return false;
        }

        // 将数组转换为List并排序
        List<File> fileList = Arrays.asList(files);
        fileList.sort(File::compareTo);

        // 合并文档
        return mergeDocuments(fileList, outputPath);
    }

    /**
     * 合并多个Word文档
     *
     * @param files 要合并的文件列表
     * @param outputPath 输出文件路径
     * @return 是否成功合并
     */
    public static boolean mergeDocuments(List<File> files, String outputPath) {
        try (XWPFDocument mergedDoc = new XWPFDocument()) {
            for (int i = 0; i < files.size(); i++) {
                File file = files.get(i);
                try {
                    logger.info("正在处理文件: {}", file.getName());
                    
                    // 如果不是第一个文档，添加分页符
                    if (i > 0) {
                        XWPFParagraph paragraph = mergedDoc.createParagraph();
                        paragraph.setPageBreak(true);
                    }

                    // 根据文件扩展名选择不同的处理方法
                    if (file.getName().toLowerCase().endsWith(".docx")) {
                        mergeDocx(mergedDoc, file);
                    } else if (file.getName().toLowerCase().endsWith(".doc")) {
                        mergeDoc(mergedDoc, file);
                    }
                } catch (Exception e) {
                    logger.error("处理文件失败: " + file.getName(), e);
                }
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

    /**
     * 合并.docx文档
     */
    private static void mergeDocx(XWPFDocument mergedDoc, File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument doc = new XWPFDocument(fis)) {
            
            // 直接复制所有段落和表格
            for (IBodyElement element : doc.getBodyElements()) {
                if (element instanceof XWPFParagraph) {
                    XWPFParagraph paragraph = (XWPFParagraph) element;
                    mergedDoc.createParagraph().getCTP().set(paragraph.getCTP());
                } else if (element instanceof XWPFTable) {
                    XWPFTable table = (XWPFTable) element;
                    mergedDoc.createTable().getCTTbl().set(table.getCTTbl());
                }
            }
        }
    }

    /**
     * 合并.doc文档
     */
    private static void mergeDoc(XWPFDocument mergedDoc, File file) throws IOException {
        // TODO: 实现.doc文件的合并
        // 注意：.doc文件需要使用HWPFDocument类来处理
        logger.warn("暂不支持合并.doc文件: {}", file.getName());
    }

    public static void main(String[] args) {
        // 测试用例
        String inputDir = "D:\\projects\\jl_tools\\logs\\output";
        String outputPath = "D:\\projects\\jl_tools\\logs\\merged_documents_" + System.currentTimeMillis() + ".docx";
        
        // 合并.docx和.doc文件
        boolean success = mergeDirectory(inputDir, outputPath, ".docx", ".doc");
        if (success) {
            System.out.println("批量处理完成，输出文件: " + outputPath);
        } else {
            System.out.println("批量处理失败");
        }
    }
} 
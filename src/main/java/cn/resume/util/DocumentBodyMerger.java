package cn.resume.util;

import cn.sunline.util.BasicInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTbl;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblWidth;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

/**
 * 文档正文合并工具类 - 用于将多个Word文档中的正文内容（包括段落和表格）合并到一个文档中
 */
@Slf4j
public class DocumentBodyMerger {
    private static final String BASIC_EXPORT_PATH = BasicInfo.getBasicExportPath("");

    /**
     * 合并指定目录下所有Word文档中的正文内容
     *
     * @param inputDirectory 输入目录路径
     * @param outputPath 输出文件路径
     * @return 是否成功合并
     */
    public static boolean mergeBodiesFromDirectory(String inputDirectory, String outputPath) {
        File directory = new File(inputDirectory);
        if (!directory.exists() || !directory.isDirectory()) {
            log.error("输入目录不存在或不是目录: {}", inputDirectory);
            return false;
        }

        // 获取所有.docx文件
        File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".docx"));
        if (files == null || files.length == 0) {
            log.error("目录中没有找到.docx文件");
            return false;
        }

        // 将数组转换为List并排序
        List<File> fileList = Arrays.asList(files);
        // 定义指定的名字顺序
        List<String> nameOrder = Arrays.asList(
            "邹智", "徐琛", "陈俭健", "蒋娅萍", "胡亚玲", "余波", "聂晨", "彭启明", 
            "崔孝收", "谢海林", "朱明", "李志敏", "丁子豪", "何聪山", "洪幼丽", 
            "唐凯平", "资小宝", "胡有志", "刘波", "饶品德"
        );
        
        // 自定义排序
        fileList.sort((f1, f2) -> {
            String name1 = f1.getName().replace(".docx", "");
            String name2 = f2.getName().replace(".docx", "");
            int index1 = nameOrder.indexOf(name1);
            int index2 = nameOrder.indexOf(name2);
            
            // 如果名字在指定列表中，按照列表顺序排序
            if (index1 != -1 && index2 != -1) {
                return index1 - index2;
            }
            // 如果名字不在指定列表中，按照字母顺序排序
            return name1.compareTo(name2);
        });

        // 合并文档中的正文内容
        return mergeBodiesFromFiles(fileList, outputPath);
    }

    /**
     * 从多个文件中合并正文内容
     *
     * @param files 要处理的文件列表
     * @param outputPath 输出文件路径
     * @return 是否成功合并
     */
    public static boolean mergeBodiesFromFiles(List<File> files, String outputPath) {
        try (XWPFDocument mergedDoc = new XWPFDocument()) {
            boolean isFirstBody = true;

            for (File file : files) {
                try {
                    log.info("正在处理文件: {}", file.getName());
                    mergeBodyFromFile(mergedDoc, file, isFirstBody);
                    isFirstBody = false;
                } catch (Exception e) {
                    log.error("处理文件失败: " + file.getName(), e);
                }
            }

            // 保存合并后的文档
            try (FileOutputStream out = new FileOutputStream(outputPath)) {
                mergedDoc.write(out);
            }
            log.info("成功合并正文内容到: {}", outputPath);
            return true;
        } catch (IOException e) {
            log.error("合并正文内容失败", e);
            return false;
        }
    }

    /**
     * 从单个文件中合并正文内容
     */
    private static void mergeBodyFromFile(XWPFDocument mergedDoc, File file, boolean isFirstBody) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument doc = new XWPFDocument(fis)) {
            
            // 获取文档中的所有段落和表格
            List<XWPFParagraph> paragraphs = doc.getParagraphs();
            List<XWPFTable> tables = doc.getTables();
            
            if (paragraphs.isEmpty() && tables.isEmpty()) {
                log.warn("文件中没有找到段落或表格: {}", file.getName());
                return;
            }

            // 如果不是第一个文档，添加空白段落
            if (!isFirstBody) {
                XWPFParagraph separator = mergedDoc.createParagraph();
                separator.setSpacingAfter(500); // 设置段落后的间距
            }

            // 获取文档中的所有内容节点（段落和表格）
            List<IBodyElement> bodyElements = doc.getBodyElements();
            
            // 按顺序处理每个节点
            for (IBodyElement element : bodyElements) {
                if (element instanceof XWPFParagraph) {
                    // 处理段落
                    XWPFParagraph sourceParagraph = (XWPFParagraph) element;
                    XWPFParagraph newParagraph = mergedDoc.createParagraph();
                    
                    // 复制段落属性
                    if (sourceParagraph.getCTP().getPPr() != null) {
                        newParagraph.getCTP().setPPr(sourceParagraph.getCTP().getPPr());
                    }
                    
                    // 复制段落内容
                    for (XWPFRun sourceRun : sourceParagraph.getRuns()) {
                        XWPFRun newRun = newParagraph.createRun();
                        newRun.setText(sourceRun.getText(0));
                        newRun.setBold(sourceRun.isBold());
                        newRun.setItalic(sourceRun.isItalic());
                        newRun.setUnderline(sourceRun.getUnderline());
                        newRun.setFontSize(sourceRun.getFontSize());
                        newRun.setFontFamily(sourceRun.getFontFamily());
                    }
                } else if (element instanceof XWPFTable) {
                    // 处理表格
                    XWPFTable sourceTable = (XWPFTable) element;
                    XWPFTable newTable = mergedDoc.createTable();
                    
                    // 复制表格的XML结构
                    CTTbl ctTbl = sourceTable.getCTTbl();
                    newTable.getCTTbl().set(ctTbl);

                    // 设置表格宽度
                    if (ctTbl.getTblPr() == null) {
                        ctTbl.addNewTblPr();
                    }
                    CTTblPr tblPr = ctTbl.getTblPr();
                    CTTblWidth tblWidth = tblPr.addNewTblW();
                    tblWidth.setW(BigInteger.valueOf(8500));
                    tblWidth.setType(STTblWidth.DXA);
                }
            }
        }
    }

    public static void main(String[] args) {
        // 测试用例
        String inputDir = "D:\\吉林银行\\risk_20250331\\resume_export";
        String outputPath = BASIC_EXPORT_PATH + "merged_bodies_" + System.currentTimeMillis() + ".docx";
        
        boolean success = mergeBodiesFromDirectory(inputDir, outputPath);
        if (success) {
            System.out.println("正文合并完成，输出文件: " + outputPath);
        } else {
            System.out.println("正文合并失败");
        }
    }
} 
package cn.resume.util;

import cn.sunline.util.BasicInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;
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
 * 表格合并工具类 - 用于将多个Word文档中的表格合并到一个文档中
 */
@Slf4j
public class TableMerger {
    private static final String BASIC_EXPORT_PATH = BasicInfo.getBasicExportPath("");
    /**
     * 合并指定目录下所有Word文档中的表格
     *
     * @param inputDirectory 输入目录路径
     * @param outputPath 输出文件路径
     * @return 是否成功合并
     */
    public static boolean mergeTablesFromDirectory(String inputDirectory, String outputPath) {
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

        // 合并文档中的表格
        return mergeTablesFromFiles(fileList, outputPath);
    }

    /**
     * 从多个文件中合并表格
     *
     * @param files 要处理的文件列表
     * @param outputPath 输出文件路径
     * @return 是否成功合并
     */
    public static boolean mergeTablesFromFiles(List<File> files, String outputPath) {
        try (XWPFDocument mergedDoc = new XWPFDocument()) {
            boolean isFirstTable = true;

            for (File file : files) {
                try {
                    log.info("正在处理文件: {}", file.getName());
                    mergeTablesFromFile(mergedDoc, file, isFirstTable);
                    isFirstTable = false;
                } catch (Exception e) {
                    log.error("处理文件失败: " + file.getName(), e);
                }
            }

            // 保存合并后的文档
            try (FileOutputStream out = new FileOutputStream(outputPath)) {
                mergedDoc.write(out);
            }
            log.info("成功合并表格到: {}", outputPath);
            return true;
        } catch (IOException e) {
            log.error("合并表格失败", e);
            return false;
        }
    }

    /**
     * 从单个文件中合并表格
     */
    private static void mergeTablesFromFile(XWPFDocument mergedDoc, File file, boolean isFirstTable) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument doc = new XWPFDocument(fis)) {
            
            // 获取文档中的所有表格
            List<XWPFTable> tables = doc.getTables();
            if (tables.isEmpty()) {
                log.warn("文件中没有找到表格: {}", file.getName());
                return;
            }

            // 如果不是第一个表格，添加空白段落
            if (!isFirstTable) {
                XWPFParagraph paragraph = mergedDoc.createParagraph();
                paragraph.setSpacingAfter(500); // 设置段落后的间距
            }

            // 复制每个表格
            for (XWPFTable table : tables) {
                // 创建新表格并复制原始表格的XML结构
                XWPFTable newTable = mergedDoc.createTable();
                CTTbl ctTbl = table.getCTTbl();
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

    public static void main(String[] args) {
        // 测试用例
        String inputDir = "D:\\吉林银行\\risk_20250331\\resume_export";
        String outputPath = BASIC_EXPORT_PATH+"merged_documents_" + System.currentTimeMillis() + ".docx";
        
        boolean success = mergeTablesFromDirectory(inputDir, outputPath);
        if (success) {
            System.out.println("表格合并完成，输出文件: " + outputPath);
        } else {
            System.out.println("表格合并失败");
        }
    }
} 
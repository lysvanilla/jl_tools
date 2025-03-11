package cn.sunline.excel;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.sunline.util.BasicInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * ExcelMerger 类用于将指定目录下的多个 Excel 文件合并为一个 Excel 文件。
 * 它会遍历指定目录下的所有 .xlsx 文件，将每个文件中的工作表复制到一个新的工作簿中，
 * 并处理同名工作表的情况，最后将合并后的工作簿保存为一个新的 Excel 文件。
 */
@Slf4j
public class ExcelMerger {
    // 定义合并后文件的基础导出路径
    private static final String basicExportPath = BasicInfo.getBasicExportPath("");
    // 用于缓存已经创建的样式
    private static Map<CellStyle, CellStyle> styleCache = new HashMap<>();

    /**
     * 程序的入口方法，用于测试 Excel 文件合并功能。
     *
     * @param args 命令行参数，此处未使用
     */
    public static void main(String[] args) {
        // 定义输入的文件夹路径
        String inputDirectory = "D:\\svn\\jilin\\04.映射设计\\0401.基础模型层";
        try {
            // 记录开始合并 Excel 文件的日志
            log.info("开始合并 Excel 文件，输入文件夹路径：[{}]", inputDirectory);
            // 调用合并方法进行文件合并
            mergeExcelFiles(inputDirectory);
            // 记录合并完成的日志
            log.info("Excel 文件合并完成！");
        } catch (IOException e) {
            // 记录合并过程中出现 IO 异常的日志，并输出异常信息
            log.error("合并 Excel 文件时发生错误", e);
        }
    }

    /**
     * 根据参数映射中的文件夹路径进行 Excel 文件合并。
     *
     * @param args_map 包含文件夹路径的参数映射，键为 "file_path"
     */
    public void mergeExcelFiles(HashMap<String, String> args_map) {
        // 从参数映射中获取文件夹路径
        String file_path = args_map.get("file_name");
        if (file_path == null) {
            // 若路径为空，记录错误日志
            log.error("参数映射中未包含 'file_name'，无法进行 Excel 文件合并");
            return;
        }
        try {
            // 调用另一个合并方法进行文件合并
            mergeExcelFiles(file_path);
        } catch (IOException e) {
            // 记录合并过程中出现 IO 异常的日志，并抛出运行时异常
            log.error("合并 Excel 文件时发生错误，输入文件夹路径：[{}]", file_path, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据指定的输入文件夹路径进行 Excel 文件合并。
     *
     * @param inputDirectory 输入的文件夹路径
     * @throws IOException 若在文件读取或写入过程中出现 IO 异常
     */
    public static void mergeExcelFiles(String inputDirectory) throws IOException {
        // 检查输入文件夹是否存在
        if (!FileUtil.exist(inputDirectory)) {
            // 若不存在，记录错误日志
            log.error("文件夹不存在，请检查路径：[{}]", inputDirectory);
            return;
        }
        // 检查输入路径是否为文件夹
        if (!FileUtil.isDirectory(inputDirectory)) {
            // 若不是文件夹，记录错误日志
            log.error("需要输入文件夹，请检查文件夹是否正确：[{}]", inputDirectory);
            return;
        }
        // 检查输入文件夹是否为空
        if (BasicInfo.isDirectoryEmpty(inputDirectory)) {
            // 若为空，记录错误日志
            log.error("输入的文件夹是一个空目录：[{}]", inputDirectory);
            return;
        }
        // 调整最小压缩率限制，防止因压缩率问题导致文件读取失败
        ZipSecureFile.setMinInflateRatio(0);

        // 创建一个新的工作簿用于合并文件
        Workbook mergedWorkbook = new XSSFWorkbook();
        // 用于存储已存在的工作表名称，避免重名
        Set<String> sheetNames = new HashSet<>();

        // 获取输入文件夹的文件对象
        File inputDir = new File(inputDirectory);
        // 获取文件夹下的所有文件
        File[] files = inputDir.listFiles();
        if (files != null) {
            // 遍历文件夹下的所有文件
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".xlsx") && !file.getName().startsWith("~")) {
                    try (FileInputStream fis = new FileInputStream(file);
                         Workbook workbook = new XSSFWorkbook(fis)) {
                        // 获取文件名（不包含扩展名）
                        String fileName = FileUtil.mainName(file);
                        // 记录开始处理当前文件的日志
                        log.info("开始处理文件：[{}]", fileName);
                        // 遍历当前文件的所有工作表
                        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                            Sheet sourceSheet = workbook.getSheetAt(i);
                            String sheetName = sourceSheet.getSheetName();
                            if (sheetNames.contains(sheetName)) {
                                // 若存在同名工作表，记录警告日志
                                log.warn("[{}]发现同名工作表 [{}]，将添加后缀以区分", fileName, sheetName);
                                // 为同名工作表添加后缀
                                sheetName = sheetName + DateUtil.format(DateUtil.date(), "MMdd_HHmmss");
                            }
                            // 将新的工作表名称添加到集合中
                            sheetNames.add(sheetName);
                            // 在合并工作簿中创建新的工作表
                            Sheet newSheet = mergedWorkbook.createSheet(sheetName);
                            // 记录开始复制工作表的日志
                            log.info("开始复制工作表 [{}] 到合并工作簿", sheetName);
                            // 调用复制方法复制工作表内容
                            copySheet(sourceSheet, newSheet, mergedWorkbook);
                            // 记录复制完成的日志
                            log.info("工作表 [{}] 复制完成", sheetName);
                        }
                        // 记录当前文件处理完成的日志
                        log.info("文件 [{}] 处理完成", fileName);
                    } catch (IOException e) {
                        // 记录处理文件时出现 IO 异常的日志，并输出异常信息
                        log.error("处理文件 [{}] 时发生错误", file.getName(), e);
                    }
                }
            }
        }

        // 构建合并后文件的输出路径
        String outputFilePath = basicExportPath + "模型EXCEL合并-" + BasicInfo.CURRENT_DATE + ".xlsx";
        try (FileOutputStream fos = new FileOutputStream(outputFilePath)) {
            // 将合并后的工作簿写入输出文件
            mergedWorkbook.write(fos);
            // 记录合并后文件保存成功的日志
            log.info("合并后的文件已保存到：[{}]", outputFilePath);
        }
        try {
            // 关闭合并后的工作簿
            mergedWorkbook.close();
        } catch (IOException e) {
            // 记录关闭工作簿时出现 IO 异常的日志，并输出异常信息
            log.error("关闭合并后的工作簿时发生错误", e);
        }
    }

    /**
     * 将源工作表的内容复制到目标工作表中，同时考虑列宽和行高。
     *
     * @param sourceSheet     源工作表
     * @param destinationSheet 目标工作表
     * @param newWorkbook      目标工作表所在的新工作簿
     */
    private static void copySheet(Sheet sourceSheet, Sheet destinationSheet, Workbook newWorkbook) {
        // 获取源工作表的最后一行行号
        int lastRowNum = sourceSheet.getLastRowNum();
        // 记录开始复制工作表的日志
        log.info("开始复制工作表，源工作表最后一行号：[{}]", lastRowNum);

        // 复制列宽
        int lastColumn = 0;
        for (int i = 0; i <= lastRowNum; i++) {
            Row row = sourceSheet.getRow(i);
            if (row != null) {
                int lastCellNum = row.getLastCellNum();
                if (lastCellNum > lastColumn) {
                    lastColumn = lastCellNum;
                }
            }
        }
        for (int i = 0; i < lastColumn; i++) {
            destinationSheet.setColumnWidth(i, sourceSheet.getColumnWidth(i));
        }



        // 遍历源工作表的每一行
        for (int i = 0; i <= lastRowNum; i++) {
            Row sourceRow = sourceSheet.getRow(i);
            if (sourceRow != null) {
                // 在目标工作表中创建对应的行
                Row destinationRow = destinationSheet.createRow(i);
                // 设置目标行的高度与源行相同，同时考虑行高是否被自定义设置
                if (sourceRow.getHeight() != -1) {
                    destinationRow.setHeight(sourceRow.getHeight());
                }
                // 获取源行的最后一个单元格的列号
                int lastCellNum = sourceRow.getLastCellNum();
                // 遍历源行的每一个单元格
                for (int j = 0; j < lastCellNum; j++) {
                    Cell sourceCell = sourceRow.getCell(j);
                    if (sourceCell != null) {
                        // 在目标行中创建对应的单元格
                        Cell destinationCell = destinationRow.createCell(j);

                        // 获取源单元格的样式
                        CellStyle sourceCellStyle = sourceCell.getCellStyle();

                        // 生成样式的关键属性字符串
                        //String styleKey = getStyleKey(sourceCellStyle);

                        // 检查缓存中是否已经存在相同的样式
                        CellStyle newCellStyle = styleCache.get(sourceCellStyle);
                        if (newCellStyle == null) {
                            // 如果不存在，则创建新的样式并添加到缓存中
                            newCellStyle = newWorkbook.createCellStyle();
                            newCellStyle.cloneStyleFrom(sourceCellStyle);
                            styleCache.put(sourceCellStyle, newCellStyle);
                        }

                        // 设置目标单元格的样式为新样式
                        destinationCell.setCellStyle(newCellStyle);

                        // 根据源单元格的类型，将值复制到目标单元格
                        switch (sourceCell.getCellType()) {
                            case STRING:
                                destinationCell.setCellValue(sourceCell.getStringCellValue());
                                break;
                            case NUMERIC:
                                destinationCell.setCellValue(sourceCell.getNumericCellValue());
                                break;
                            case BOOLEAN:
                                destinationCell.setCellValue(sourceCell.getBooleanCellValue());
                                break;
                            case FORMULA:
                                try {
                                    // 尝试设置公式
                                    destinationCell.setCellFormula(sourceCell.getCellFormula());
                                } catch (FormulaParseException e) {
                                    // 处理公式解析异常，这里简单记录日志并忽略公式
                                    log.error("复制公式时发生错误，公式: [{}]", sourceCell.getCellFormula(), e);
                                    // 可以选择将公式结果作为值复制
                                    //destinationCell.setCellValue(sourceCell.getStringCellValue());
                                }
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        }
        // 记录工作表复制完成的日志
        log.info("工作表复制完成");
    }

    /**
     * 生成样式的关键属性字符串
     * @param style 单元格样式
     * @return 样式的关键属性字符串
     */
    private static String getStyleKey(CellStyle style) {
        StringBuilder key = new StringBuilder();
        key.append(style.getAlignment().name());
        key.append(style.getVerticalAlignment().name());
        key.append(style.getBorderTop().name());
        key.append(style.getBorderRight().name());
        key.append(style.getBorderBottom().name());
        key.append(style.getBorderLeft().name());
        key.append(style.getFillForegroundColor());
        key.append(style.getFillPattern().name());
        key.append(style.getFontIndexAsInt());
        return key.toString();
    }
}

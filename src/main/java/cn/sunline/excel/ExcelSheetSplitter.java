package cn.sunline.excel;

import cn.hutool.core.io.FileUtil;
import cn.sunline.util.BasicInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 * ExcelSheetSplitter 类用于将一个 Excel 文件中的各个工作表拆分成单独的 Excel 文件。
 * 它会读取输入的 Excel 文件，将每个工作表复制到一个新的工作簿中，并保存为独立的 Excel 文件。
 */
@Slf4j
public class ExcelSheetSplitter {
    // 定义拆分后文件的基础导出路径，使用 BasicInfo 类获取路径，并指定文件夹名为 "模型拆分"
    private static final String basicExportPath = BasicInfo.getBasicExportPath("模型拆分");

    /**
     * 程序的入口方法，用于测试 Excel 工作表拆分功能。
     *
     * @param args 命令行参数，此处未使用
     */
    public static void main(String[] args) {
        // 定义输入的 Excel 文件路径
        String inputFilePath = "C:\\Users\\lysva\\Desktop\\吉林银行风险集市项目_基础模型层映射(合并)v1.1.xlsx";
        inputFilePath = "D:\\svn\\jilin\\98.个人资料\\王萍\\吉林银行风险集市项目_风险计量层数据映射v1.0.xlsx";
        try {
            // 记录开始进行 Excel 工作表拆分的日志，包含输入文件路径
            log.info("开始进行 Excel 工作表拆分，输入文件路径：[{}]", inputFilePath);
            // 调用 splitExcelSheets 方法进行拆分操作
            splitExcelSheets(inputFilePath);
            // 记录拆分完成的日志
            log.info("Excel 工作表拆分完成！");
            // 输出提示信息到控制台
            System.out.println("拆分完成！");
        } catch (IOException e) {
            // 记录拆分过程中出现 IO 异常的日志，包含输入文件路径和异常信息
            log.error("拆分 Excel 工作表时出现 IO 异常，输入文件路径：[{}]", inputFilePath, e);
            // 打印异常堆栈信息
            e.printStackTrace();
        }
    }

    /**
     * 根据参数映射中的文件路径进行 Excel 工作表拆分。
     *
     * @param args_map 包含文件路径的参数映射，键为 "file_name"
     */
    public void splitExcelSheets(HashMap<String, String> args_map) {
        // 从参数映射中获取文件路径
        String file_name = args_map.get("file_name");
        // 检查文件路径是否为空
        if (file_name == null) {
            // 若为空，记录错误日志
            log.error("参数映射中未包含 'file_name'，无法进行 Excel 工作表拆分");
            return;
        }
        try {
            // 调用另一个 splitExcelSheets 方法进行拆分操作
            splitExcelSheets(file_name);
        } catch (IOException e) {
            // 记录拆分过程中出现 IO 异常的日志，包含输入文件路径和异常信息
            log.error("拆分 Excel 工作表时出现 IO 异常，输入文件路径：[{}]", file_name, e);
            // 抛出运行时异常
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据指定的输入文件路径进行 Excel 工作表拆分。
     *
     * @param inputFilePath 输入的 Excel 文件路径
     * @throws IOException 若在文件读取或写入过程中出现 IO 异常
     */
    public static void splitExcelSheets(String inputFilePath) throws IOException {
        // 检查输入文件是否存在
        if (!FileUtil.exist(inputFilePath)) {
            // 若不存在，记录错误日志
            log.error("文件不存在，请检查路径：[{}]", inputFilePath);
            return;
        }
        // 使用 try-with-resources 语句打开输入文件流和工作簿，确保资源自动关闭
        try (FileInputStream fis = new FileInputStream(inputFilePath);
             Workbook workbook = new XSSFWorkbook(fis)) {
            // 获取输入文件中的工作表数量
            int sheetCount = workbook.getNumberOfSheets();
            // 记录开始处理输入文件的日志，包含文件路径和工作表数量
            log.info("开始处理输入文件 [{}]，该文件包含 [{}] 个工作表", inputFilePath, sheetCount);

            // 遍历输入文件中的每个工作表
            for (int i = 0; i < sheetCount; i++) {
                // 获取当前工作表
                Sheet sheet = workbook.getSheetAt(i);
                // 获取当前工作表的名称
                String sheetName = sheet.getSheetName();

                // 创建一个新的工作簿
                Workbook newWorkbook = new XSSFWorkbook();
                // 在新工作簿中创建一个与当前工作表同名的工作表
                Sheet newSheet = newWorkbook.createSheet(sheetName);

                // 记录开始复制当前工作表到新工作簿的日志
                log.info("开始复制工作表 [{}] 到新的工作簿", sheetName);
                // 调用 copySheet 方法复制工作表内容
                copySheet(sheet, newSheet, newWorkbook);
                // 记录工作表复制完成的日志
                log.info("工作表 [{}] 复制完成", sheetName);

                // 构建输出文件的路径，使用基础导出路径和工作表名称
                String outputFilePath = basicExportPath + sheetName + ".xlsx";
                // 使用 try-with-resources 语句打开输出文件流，确保资源自动关闭
                try (FileOutputStream fos = new FileOutputStream(outputFilePath)) {
                    // 将新工作簿的内容写入输出文件
                    newWorkbook.write(fos);
                    // 记录当前工作表拆分成功的日志，包含处理进度、工作表名称和输出文件路径
                    log.info("[{}/{}]，Sheet [{}] 拆分成功，输出文件路径：[{}]", i + 1, sheetCount, sheetName, outputFilePath);
                } catch (IOException e) {
                    // 记录保存工作表到文件时出现 IO 异常的日志，包含工作表名称、输出文件路径和异常信息
                    log.error("保存工作表 [{}] 到文件 [{}] 时出现 IO 异常", sheetName, outputFilePath, e);
                }
                try {
                    // 关闭新工作簿
                    newWorkbook.close();
                } catch (IOException e) {
                    // 记录关闭新工作簿时出现 IO 异常的日志，包含工作表名称和异常信息
                    log.error("关闭新工作簿时出现 IO 异常，工作表名称：[{}]", sheetName, e);
                }
            }
        } catch (IOException e) {
            // 记录读取输入文件时出现 IO 异常的日志，包含输入文件路径和异常信息
            log.error("读取输入文件 [{}] 时出现 IO 异常", inputFilePath, e);
            // 抛出异常
            throw e;
        }
    }

    /**
     * 将源工作表的内容复制到目标工作表中。
     *
     * @param sourceSheet     源工作表
     * @param destinationSheet 目标工作表
     * @param newWorkbook      目标工作表所在的新工作簿
     */
    private static void copySheet(Sheet sourceSheet, Sheet destinationSheet, Workbook newWorkbook) {
        // 获取源工作表的最后一行行号
        int lastRowNum = sourceSheet.getLastRowNum();
        // 记录开始复制工作表的日志，包含源工作表的最后一行行号
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
            // 获取源工作表的当前行
            Row sourceRow = sourceSheet.getRow(i);
            // 检查当前行是否存在
            if (sourceRow != null) {
                // 在目标工作表中创建与源工作表当前行对应的行
                Row destinationRow = destinationSheet.createRow(i);

                // 设置目标行的高度与源行相同，同时考虑行高是否被自定义设置
                if (sourceRow.getHeight() != -1) {
                    destinationRow.setHeight(sourceRow.getHeight());
                }

                // 获取源行的最后一个单元格的列号
                int lastCellNum = sourceRow.getLastCellNum();
                // 遍历源行的每一个单元格
                for (int j = 0; j < lastCellNum; j++) {
                    // 获取源行的当前单元格
                    Cell sourceCell = sourceRow.getCell(j);
                    // 检查当前单元格是否存在
                    if (sourceCell != null) {
                        // 在目标行中创建与源单元格对应的单元格
                        Cell destinationCell = destinationRow.createCell(j);
                        // 创建一个新的单元格样式
                        CellStyle newCellStyle = newWorkbook.createCellStyle();
                        // 复制源单元格的样式到新样式
                        newCellStyle.cloneStyleFrom(sourceCell.getCellStyle());
                        // 设置目标单元格的样式为新样式
                        destinationCell.setCellStyle(newCellStyle);

                        // 根据源单元格的类型，将值复制到目标单元格
                        switch (sourceCell.getCellType()) {
                            case STRING:
                                // 若源单元格为字符串类型，将字符串值复制到目标单元格
                                destinationCell.setCellValue(sourceCell.getStringCellValue());
                                break;
                            case NUMERIC:
                                // 若源单元格为数值类型，将数值值复制到目标单元格
                                destinationCell.setCellValue(sourceCell.getNumericCellValue());
                                break;
                            case BOOLEAN:
                                // 若源单元格为布尔类型，将布尔值复制到目标单元格
                                destinationCell.setCellValue(sourceCell.getBooleanCellValue());
                                break;
                            case FORMULA:
                                // 若源单元格为公式类型，将公式复制到目标单元格
                                destinationCell.setCellFormula(sourceCell.getCellFormula());
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
}
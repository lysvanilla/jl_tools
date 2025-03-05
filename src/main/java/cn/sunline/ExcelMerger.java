package cn.sunline;

import cn.hutool.core.io.FileUtil;
import cn.sunline.util.BasicInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static cn.sunline.util.BasicInfo.isDirectoryEmpty;

@Slf4j
public class ExcelMerger {
    private static final String basicExportPath = BasicInfo.getBasicExportPath("");

    public static void main(String[] args) {
        String inputDirectory = "D:\\吉林银行\\risk_20250305\\模型拆分空白";
        try {
            // 调整最小压缩率限制
            ZipSecureFile.setMinInflateRatio(0.001);
            mergeExcelFiles(inputDirectory);
            log.info("Excel 文件合并完成！");
        } catch (IOException e) {
            log.error("合并 Excel 文件时发生错误", e);
        }
    }

    public void mergeExcelFiles(HashMap<String,String> args_map){
        String file_name=args_map.get("file_path");
        try {
            mergeExcelFiles(file_name);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void mergeExcelFiles(String inputDirectory) throws IOException {
        if (!FileUtil.exist(inputDirectory)){
            log.error("文件夹不存在，请检查路径：[{}]", inputDirectory);
            System.exit(1);
        }else if (!FileUtil.isDirectory(inputDirectory)){
            log.error("需要输入文件夹，请检查文件夹是否正确：[{}]", inputDirectory);
            System.exit(1);
        }else if (isDirectoryEmpty(inputDirectory)){
            log.error("输入的文件夹是一个空目录：[{}]", inputDirectory);
            System.exit(1);
        }


        Workbook mergedWorkbook = new XSSFWorkbook();
        Set<String> sheetNames = new HashSet<>();

        Path inputDir = Paths.get(inputDirectory);
        Files.list(inputDir)
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".xlsx"))
                .forEach(path -> {
                    try (FileInputStream fis = new FileInputStream(path.toFile());
                         Workbook workbook = new XSSFWorkbook(fis)) {
                        String fileName = FileUtil.mainName(path.toFile());
                        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                            Sheet sourceSheet = workbook.getSheetAt(i);
                            String sheetName = sourceSheet.getSheetName();
                            if (sheetNames.contains(sheetName)) {
                                log.warn("[{}]发现同名工作表 [{}]，跳过该工作表",fileName, sheetName);
                                //continue;
                                sheetName = sheetName+BasicInfo.dist_suffix;
                            }
                            sheetNames.add(sheetName);
                            Sheet newSheet = mergedWorkbook.createSheet(sheetName);
                            copySheet(sourceSheet, newSheet, mergedWorkbook);
                        }
                    } catch (IOException e) {
                        log.error("处理文件 {} 时发生错误", path, e);
                    }
                });
        String outputFilePath = basicExportPath+"模型EXCEL合并-"+BasicInfo.currentDate+".xlsx";
        try (FileOutputStream fos = new FileOutputStream(outputFilePath)) {
            mergedWorkbook.write(fos);
        }
        mergedWorkbook.close();
    }

    private static void copySheet(Sheet sourceSheet, Sheet destinationSheet, Workbook newWorkbook) {
        for (int i = 0; i <= sourceSheet.getLastRowNum(); i++) {
            Row sourceRow = sourceSheet.getRow(i);
            if (sourceRow != null) {
                Row destinationRow = destinationSheet.createRow(i);
                destinationRow.setHeight(sourceRow.getHeight());
                for (int j = 0; j < sourceRow.getLastCellNum(); j++) {
                    Cell sourceCell = sourceRow.getCell(j);
                    if (sourceCell != null) {
                        Cell destinationCell = destinationRow.createCell(j);
                        // 创建新的样式并复制原样式的属性
                        CellStyle newCellStyle = newWorkbook.createCellStyle();
                        newCellStyle.cloneStyleFrom(sourceCell.getCellStyle());
                        destinationCell.setCellStyle(newCellStyle);

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
                                destinationCell.setCellFormula(sourceCell.getCellFormula());
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        }
    }
}

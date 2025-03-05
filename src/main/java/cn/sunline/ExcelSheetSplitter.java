package cn.sunline;

import cn.hutool.core.io.FileUtil;
import cn.sunline.util.BasicInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

@Slf4j
public class ExcelSheetSplitter {
    private static final String basicExportPath = BasicInfo.getBasicExportPath("模型拆分");
    public static void main(String[] args) {
        String inputFilePath = "D:\\svn\\jilin\\02.需求分析\\模板_吉林银行_风险数据集市逻辑设计文档-v0.1.xlsx";
        try {
            splitExcelSheets(inputFilePath);
            System.out.println("拆分完成！");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void splitExcelSheets(HashMap<String,String> args_map){
        String file_name=args_map.get("file_name");
        try {
            splitExcelSheets(file_name);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void splitExcelSheets(String inputFilePath) throws IOException {
        if (!FileUtil.exist(inputFilePath)){
            log.error("文件不存在，请检查路径：[{}]", inputFilePath);
            System.exit(1);
        }
        try (FileInputStream fis = new FileInputStream(inputFilePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                String sheetName = sheet.getSheetName();

                Workbook newWorkbook = new XSSFWorkbook();
                Sheet newSheet = newWorkbook.createSheet(sheetName);

                copySheet(sheet, newSheet, newWorkbook);

                String outputFilePath = basicExportPath + sheetName + ".xlsx";
                try (FileOutputStream fos = new FileOutputStream(outputFilePath)) {
                    newWorkbook.write(fos);
                    log.info("Sheet [{}] 拆分成功 [{}]", sheetName, outputFilePath);
                }
                newWorkbook.close();
            }
        }
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

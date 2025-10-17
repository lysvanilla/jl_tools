package cn.pws;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExcelSheetCopier {

    /**
     * 复制指定Excel文件中的多个sheet到新的Excel文件
     *
     * @param sourceFilePath 源Excel文件路径
     * @param targetFilePath 目标Excel文件路径
     * @param sheetNames 要复制的sheet名称列表，如果为null或空则复制所有sheet
     * @throws IOException 当文件操作发生错误时抛出
     */
    public static void copySheets(String sourceFilePath, String targetFilePath, List<String> sheetNames) throws IOException {
        // 读取源Excel文件
        try (Workbook sourceWorkbook = WorkbookFactory.create(new FileInputStream(sourceFilePath));
             Workbook targetWorkbook = new XSSFWorkbook()) {

            // 遍历源Excel中的所有sheet
            for (int i = 0; i < sourceWorkbook.getNumberOfSheets(); i++) {
                Sheet sourceSheet = sourceWorkbook.getSheetAt(i);
                String sheetName = sourceSheet.getSheetName();

                // 判断是否需要复制当前sheet
                if (sheetNames == null || sheetNames.isEmpty() || sheetNames.contains(sheetName)) {
                    copySheet(sourceWorkbook, targetWorkbook, sourceSheet);
                }
            }

            // 将目标Excel写入文件
            try (FileOutputStream outputStream = new FileOutputStream(targetFilePath)) {
                targetWorkbook.write(outputStream);
            }
        }
    }

    /**
     * 复制单个sheet到目标工作簿
     */
    private static void copySheet(Workbook sourceWorkbook, Workbook targetWorkbook, Sheet sourceSheet) {
        // 在目标工作簿中创建新的sheet
        String sheetName = sourceSheet.getSheetName();
        Sheet targetSheet = targetWorkbook.createSheet(sheetName);

        // 复制sheet的设置
        targetSheet.setDefaultRowHeight(sourceSheet.getDefaultRowHeight());
        targetSheet.setDefaultRowHeightInPoints(sourceSheet.getDefaultRowHeightInPoints());
        targetSheet.setDisplayGridlines(sourceSheet.isDisplayGridlines());

        // 复制列宽
        int numberOfColumns = sourceSheet.getRow(sourceSheet.getFirstRowNum()).getLastCellNum();
        for (int i = 0; i < numberOfColumns; i++) {
            targetSheet.setColumnWidth(i, sourceSheet.getColumnWidth(i));
        }

        // 复制行和单元格
        int rowIndex = 0;
        for (Row sourceRow : sourceSheet) {
            Row targetRow = targetSheet.createRow(rowIndex++);
            copyRow(sourceWorkbook, targetWorkbook, sourceRow, targetRow);
        }
    }

    /**
     * 复制行及其包含的单元格
     */
    private static void copyRow(Workbook sourceWorkbook, Workbook targetWorkbook, Row sourceRow, Row targetRow) {
        targetRow.setHeight(sourceRow.getHeight());

        // 复制单元格
        for (Cell sourceCell : sourceRow) {
            Cell targetCell = targetRow.createCell(sourceCell.getColumnIndex());
            copyCell(sourceWorkbook, targetWorkbook, sourceCell, targetCell);
        }
    }

    /**
     * 复制单元格的值、样式和数据类型
     */
    private static void copyCell(Workbook sourceWorkbook, Workbook targetWorkbook, Cell sourceCell, Cell targetCell) {
        // 复制单元格样式
        CellStyle targetCellStyle = targetWorkbook.createCellStyle();
        targetCellStyle.cloneStyleFrom(sourceCell.getCellStyle());
        targetCell.setCellStyle(targetCellStyle);

        // 复制单元格的值和数据类型
        switch (sourceCell.getCellType()) {
            case STRING:
                targetCell.setCellValue(sourceCell.getStringCellValue());
                break;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(sourceCell)) {
                    targetCell.setCellValue(sourceCell.getDateCellValue());
                } else {
                    targetCell.setCellValue(sourceCell.getNumericCellValue());
                }
                break;
            case BOOLEAN:
                targetCell.setCellValue(sourceCell.getBooleanCellValue());
                break;
            case FORMULA:
                targetCell.setCellFormula(sourceCell.getCellFormula());
                break;
            case BLANK:
                targetCell.setBlank();
                break;
            default:
                targetCell.setCellValue(sourceCell.getStringCellValue());
        }
    }

    // 示例用法
    public static void main(String[] args) {
        try {
            String sourceFilePath = "source.xlsx";  // 源Excel文件路径
            String targetFilePath = "target.xlsx";  // 目标Excel文件路径

            // 要复制的sheet名称列表
            List<String> sheetsToCopy = new ArrayList<>();
            sheetsToCopy.add("Sheet1");
            sheetsToCopy.add("Sheet3");

            // 复制指定的sheet
            copySheets(sourceFilePath, targetFilePath, sheetsToCopy);

            // 如果要复制所有sheet，可以使用：
            // copySheets(sourceFilePath, targetFilePath, null);

            System.out.println("Excel sheet复制成功！");
        } catch (IOException e) {
            System.err.println("复制过程中发生错误：" + e.getMessage());
            e.printStackTrace();
        }
    }
}

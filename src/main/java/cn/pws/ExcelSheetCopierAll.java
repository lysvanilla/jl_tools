package cn.pws;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ExcelSheetCopierAll {

    public static void main(String[] args) {
        // 源Excel文件路径
        String sourceFilePath =   "D:\\projects\\jl_tools\\template\\报价评估表模板--运营中心商务部发布-数据业务总部-20250601（0618）.xlsx";
        // 目标Excel文件路径
        String destFilePath = "D:\\projects\\jl_tools\\template\\destination.xlsx";

        try {
            // 复制工作表
            copySheets(sourceFilePath, destFilePath);
            System.out.println("工作表复制成功！");
        } catch (IOException e) {
            System.err.println("复制过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 复制源Excel文件中的所有工作表到新的Excel文件
     * @param sourceFilePath 源Excel文件路径
     * @param destFilePath 目标Excel文件路径
     * @throws IOException 处理文件时可能抛出的异常
     */
    public static void copySheets(String sourceFilePath, String destFilePath) throws IOException {
        // 读取源Excel文件
        try (Workbook sourceWorkbook = WorkbookFactory.create(new FileInputStream(sourceFilePath));
             // 创建新的Excel工作簿
             Workbook destWorkbook = new XSSFWorkbook()) {

            // 逐个复制每个工作表
            for (int i = 0; i < sourceWorkbook.getNumberOfSheets(); i++) {
                Sheet sourceSheet = sourceWorkbook.getSheetAt(i);
                String sheetName = sourceWorkbook.getSheetName(i);

                // 在目标工作簿中创建新工作表
                Sheet destSheet = destWorkbook.createSheet(sheetName);

                // 复制工作表内容
                copySheetContent(sourceSheet, destSheet, destWorkbook);
            }

            // 将目标工作簿写入文件
            try (FileOutputStream fileOut = new FileOutputStream(destFilePath)) {
                destWorkbook.write(fileOut);
            }
        }
    }

    /**
     * 复制单个工作表的内容
     * @param sourceSheet 源工作表
     * @param destSheet 目标工作表
     * @param destWorkbook 目标工作簿
     */
    private static void copySheetContent(Sheet sourceSheet, Sheet destSheet, Workbook destWorkbook) {
        // 复制合并区域
        copyMergedRegions(sourceSheet, destSheet);

        // 复制行和单元格
        for (Row sourceRow : sourceSheet) {
            Row destRow = destSheet.createRow(sourceRow.getRowNum());

            for (Cell sourceCell : sourceRow) {
                Cell destCell = destRow.createCell(sourceCell.getColumnIndex());

                // 复制单元格值
                copyCellValue(sourceCell, destCell);

                // 复制单元格样式
                copyCellStyle(sourceCell, destCell, destWorkbook);
            }
        }

        // 复制列宽
        copyColumnWidths(sourceSheet, destSheet);
    }

    /**
     * 复制合并区域
     */
    private static void copyMergedRegions(Sheet sourceSheet, Sheet destSheet) {
        for (int i = 0; i < sourceSheet.getNumMergedRegions(); i++) {
            destSheet.addMergedRegion(sourceSheet.getMergedRegion(i));
        }
    }

    /**
     * 复制单元格值
     */
    private static void copyCellValue(Cell sourceCell, Cell destCell) {
        switch (sourceCell.getCellType()) {
            case STRING:
                destCell.setCellValue(sourceCell.getStringCellValue());
                break;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(sourceCell)) {
                    destCell.setCellValue(sourceCell.getDateCellValue());
                } else {
                    destCell.setCellValue(sourceCell.getNumericCellValue());
                }
                break;
            case BOOLEAN:
                destCell.setCellValue(sourceCell.getBooleanCellValue());
                break;
            case FORMULA:
                destCell.setCellFormula(sourceCell.getCellFormula());
                break;
            case BLANK:
                destCell.setCellType(CellType.BLANK);
                break;
            default:
                destCell.setCellValue(sourceCell.getStringCellValue());
        }
    }

    /**
     * 复制单元格样式
     */
    private static void copyCellStyle(Cell sourceCell, Cell destCell, Workbook destWorkbook) {
        CellStyle sourceStyle = sourceCell.getCellStyle();
        CellStyle destStyle = destWorkbook.createCellStyle();

        // 复制样式属性
        destStyle.cloneStyleFrom(sourceStyle);
        destCell.setCellStyle(destStyle);
    }

    /**
     * 复制列宽
     */
    private static void copyColumnWidths(Sheet sourceSheet, Sheet destSheet) {
        for (int i = 0; i <= sourceSheet.getLastRowNum(); i++) {
            Row row = sourceSheet.getRow(i);
            if (row != null) {
                for (int j = 0; j < row.getLastCellNum(); j++) {
                    int width = sourceSheet.getColumnWidth(j);
                    destSheet.setColumnWidth(j, width);
                }
            }
        }
    }
}

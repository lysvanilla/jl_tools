package cn.pws;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ExcelSheetReader {
    // 定义日期格式化器，用于将日期转换为yyyymmdd格式
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyyMMdd");

    public static void main(String[] args) {
        // Excel文件路径，请替换为实际文件路径
        String filePath = "D:\\projects\\jl_tools\\template\\报价评估表模板--运营中心商务部发布-数据业务总部-20250601（0618）.xlsx";
        // 要读取的sheet页名称
        String sheetName = "8.数据库-人力成本及补助（20250601）";

        //getSheetInfo(filePath,"8.数据库-人力成本及补助（20250601）");
        getSheetInfo(filePath,"7 .参数");
    }

    private static void getSheetInfo(String filePath, String sheetName) {
        try (FileInputStream fis = new FileInputStream(new File(filePath));
             Workbook workbook = new XSSFWorkbook(fis)) {

            // 获取指定名称的sheet页
            Sheet sheet = workbook.getSheet(sheetName);

            if (sheet == null) {
                System.out.println("未找到名称为 '" + sheetName + "' 的工作表");
                return;
            }

            // 遍历sheet页中的所有行
            for (Row row : sheet) {
                // 遍历行中的所有单元格
                for (Cell cell : row) {
                    // 根据单元格类型获取值
                    String cellValue = getCellValue(cell);
                    System.out.print(cellValue + "\t");
                }
                System.out.println(); // 一行结束后换行
            }

            System.out.println("成功读取sheet页: " + sheetName);

        } catch (IOException e) {
            System.err.println("读取Excel文件时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * 根据单元格类型获取单元格的值
     */
    private static String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    // 处理日期类型，格式化为yyyymmdd
                    Date date = cell.getDateCellValue();
                    return DATE_FORMATTER.format(date);
                } else {
                    // 处理数字，避免科学计数法
                    // 检查是否为整数
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == (long) numericValue) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                // 处理公式，获取公式结果
                return getCellValue(cell.getCachedFormulaResultType(), cell);
            default:
                return "";
        }
    }

    /**
     * 根据单元格值类型获取值
     */
    private static String getCellValue(CellType cellType, Cell cell) {
        switch (cellType) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    // 处理日期类型，格式化为yyyymmdd
                    Date date = cell.getDateCellValue();
                    return DATE_FORMATTER.format(date);
                } else {
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == (long) numericValue) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }
}

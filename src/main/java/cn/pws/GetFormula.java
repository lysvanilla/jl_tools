package cn.pws;

import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class GetFormula {
    public static void main(String[] args) {
        String cellRef = "G3";

        // Excel文件路径和工作表名称（保持不变）
        String filePath = "D:\\projects\\jl_tools\\template\\报价评估表模板--运营中心商务部发布-数据业务总部-20250601（0618）.xlsx";
        String sheetName = "2.交付主体人力成本";

        getCellInfo("G3",filePath,sheetName);
        getCellInfo("H3",filePath,sheetName);
        getCellInfo("I3",filePath,sheetName);
        getCellInfo("J3",filePath,sheetName);
        getCellInfo("K3",filePath,sheetName);
        getCellInfo("L3",filePath,sheetName);
        getCellInfo("F6",filePath,sheetName);
        getCellInfo("G6",filePath,sheetName);
        getCellInfo("H6",filePath,sheetName);
        getCellInfo("I6",filePath,sheetName);
        getCellInfo("J6",filePath,sheetName);
        getCellInfo("K6",filePath,sheetName);
        getCellInfo("L6",filePath,sheetName);

    }

    public static void getCellInfo(String cellRef,String filePath,String sheetName) {
        // 解析单元格坐标为行索引和列索引
        CellIndex index = parseCellReference(cellRef);
        if (index == null) {
            System.out.println("输入格式错误，请使用如G3的格式");
            return;
        }
        int rowIdx = index.row;
        int colIdx = index.col;


        try {
            FileInputStream fis = new FileInputStream(filePath);
            Workbook workbook = WorkbookFactory.create(fis);
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                System.out.println("找不到名为 '" + sheetName + "' 的工作表");
                return;
            }

            // 读取目标单元格的公式
            Row row = sheet.getRow(rowIdx);
            if (row == null) {
                System.out.println(cellRef + "单元格所在行不存在");
                return;
            }
            Cell cell = row.getCell(colIdx);
            if (cell == null) {
                System.out.println(cellRef + "单元格为空");
            } else {
                if (cell.getCellType() == CellType.FORMULA) {
                    String formula = cell.getCellFormula();
                    System.out.println(cellRef + "单元格的公式为: " + formula);
                } else {
                    System.out.println(cellRef + "单元格不是公式，其值为: " + getCellValue(cell));
                }
            }

            // 关闭资源
            fis.close();
            workbook.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 辅助类：存储单元格的行索引和列索引
    private static class CellIndex {
        int row;
        int col;
        CellIndex(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }

    // 解析单元格坐标（如G3 -> 行2，列6）
    private static CellIndex parseCellReference(String cellRef) {
        if (cellRef == null || cellRef.isEmpty()) {
            return null;
        }

        // 分离列字母和行数字
        int splitIndex = 0;
        while (splitIndex < cellRef.length() && Character.isLetter(cellRef.charAt(splitIndex))) {
            splitIndex++;
        }
        if (splitIndex == 0 || splitIndex == cellRef.length()) {
            return null; // 没有字母或没有数字部分
        }

        // 解析列字母（如G -> 6）
        String colStr = cellRef.substring(0, splitIndex).toUpperCase();
        int col = 0;
        for (int i = 0; i < colStr.length(); i++) {
            col = col * 26 + (colStr.charAt(i) - 'A' + 1);
        }
        col--; // 转换为0-based索引

        // 解析行数字（如3 -> 2）
        try {
            int row = Integer.parseInt(cellRef.substring(splitIndex)) - 1; // 转换为0-based索引
            return new CellIndex(row, col);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return getCellValue(cell.getSheet().getWorkbook().getCreationHelper()
                        .createFormulaEvaluator().evaluateInCell(cell));
            default:
                return "";
        }
    }
}

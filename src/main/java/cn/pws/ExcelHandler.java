package cn.pws;

import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ExcelHandler {
    public static void main(String[] args) {
        // Excel文件路径，请替换为实际文件路径
        String filePath = "D:\\projects\\jl_tools\\template\\报价评估表模板--运营中心商务部发布-数据业务总部-20250601（0618）.xlsx";
        // 目标工作表名称
        String sheetName = "2.交付主体人力成本";

        try {
            // 读取Excel文件
            FileInputStream fis = new FileInputStream(filePath);
            Workbook workbook = WorkbookFactory.create(fis);

            // 获取目标工作表
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                System.out.println("找不到名为 '" + sheetName + "' 的工作表");
                return;
            }

            // 向C6单元格写入数据 (行索引从0开始，所以第6行是索引5)
            Row rowC6 = sheet.getRow(5); // 第6行
            if (rowC6 == null) {
                rowC6 = sheet.createRow(5);
            }
            Cell cellC6 = rowC6.getCell(2); // C列是索引2
            if (cellC6 == null) {
                cellC6 = rowC6.createCell(2);
            }
            cellC6.setCellValue("一级B（助理工程师1级）"); // 设置C6的值，这里使用示例值

            // 向D6单元格写入数据
            Cell cellD6 = rowC6.getCell(3); // D列是索引3
            if (cellD6 == null) {
                cellD6 = rowC6.createCell(3);
            }
            cellD6.setCellValue("长沙市"); // 设置D6的值，这里使用示例值

            // 保存修改
            fis.close(); // 关闭输入流
            FileOutputStream fos = new FileOutputStream(filePath);
            workbook.write(fos);

            // 重新读取工作簿以获取更新后的值（特别是公式计算结果）
            workbook = WorkbookFactory.create(new FileInputStream(filePath));
            sheet = workbook.getSheet(sheetName);

            // 读取B1单元格的值 (第1行索引0，B列索引1)
            Row rowB1 = sheet.getRow(2);
            if (rowB1 == null) {
                System.out.println("N3单元格不存在");
                return;
            }

            Cell cellB1 = rowB1.getCell(13);
            if (cellB1 == null) {
                System.out.println("N3单元格为空");
            } else {
                String b1Value = getCellValue(cellB1);
                System.out.println("N3单元格的值: " + b1Value);
            }

            // 关闭资源
            fos.close();
            workbook.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 辅助方法：获取单元格的实际值，处理不同数据类型
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
                // 获取公式本身
                String b1Formula = cell.getCellFormula();
                System.out.println("B1单元格的公式: " + b1Formula);
                // 如果是公式，获取计算结果
                return getCellValue(cell.getSheet().getWorkbook().getCreationHelper()
                        .createFormulaEvaluator().evaluateInCell(cell));
            default:
                return "";
        }
    }
}

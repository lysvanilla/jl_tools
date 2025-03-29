package com.example.resume.policy;

import com.deepoove.poi.data.RowRenderData;
import com.deepoove.poi.data.Rows;
import com.deepoove.poi.data.Texts;
import com.deepoove.poi.policy.DynamicTableRenderPolicy;
import com.deepoove.poi.template.ElementTemplate;
import com.deepoove.poi.XWPFTemplate;
import com.example.resume.entity.ProjectExperience;
import com.example.resume.entity.Resume;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTVMerge;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STMerge;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 项目经验表格渲染策略
 * 动态处理项目经验表格，按照模板行填充
 */
public class ProjectExperienceTablePolicy extends DynamicTableRenderPolicy {

    @Override
    public void render(XWPFTable table, Object data) throws Exception {
        System.out.println("开始渲染项目经验表格...");
        if (data == null) {
            System.out.println("项目经验渲染策略接收到的数据为空");
            return;
        }
        
        Resume resume = null;
        // 尝试直接转换
        if (data instanceof Resume) {
            resume = (Resume) data;
            System.out.println("直接获取Resume对象");
        } 
        // 优先尝试从模板数据映射中获取resume对象
        else if (data instanceof Map) {
            Map<String, Object> dataMap = (Map<String, Object>) data;
            if (dataMap.containsKey("resume")) {
                Object resumeObj = dataMap.get("resume");
                if (resumeObj instanceof Resume) {
                    resume = (Resume) resumeObj;
                    System.out.println("从数据映射中成功获取Resume对象");
                }
            }
        } 
        
        if (resume == null) {
            System.out.println("无法获取Resume对象，渲染终止");
            return;
        }
        
        List<ProjectExperience> projectExperiences = resume.getProjectExperiences();
        if (projectExperiences == null || projectExperiences.isEmpty()) {
            System.out.println("项目经验列表为空");
            return;
        }
        
        System.out.println("获取到项目经验数据，共有 " + projectExperiences.size() + " 条项目经验");
        System.out.println("表格行数: " + table.getRows().size());
        
        // 假设项目经验表格的第一行是模板行
        if (table.getRows().size() < 6) {
            System.out.println("表格行数不足6行，无法渲染");
            return;
        }
        
        // 使用第6行(索引5)作为项目经验模板行
        XWPFTableRow templateRow = table.getRow(5);
        System.out.println("使用表格第6行(索引5)作为项目经验模板行");
        
        // 输出模板行信息，便于调试
        if (templateRow.getTableCells().size() > 0) {
            for (int i = 0; i < templateRow.getTableCells().size(); i++) {
                XWPFTableCell cell = templateRow.getCell(i);
                System.out.println("模板行第" + (i + 1) + "列内容: " + cell.getText());
            }
        }
        
        // 清空模板行中可能存在的{{projectExperiences}}标记
        for (XWPFTableCell cell : templateRow.getTableCells()) {
            for (XWPFParagraph paragraph : cell.getParagraphs()) {
                String text = paragraph.getText();
                if (text != null && text.contains("{{projectExperiences}}")) {
                    System.out.println("找到并清除标记文本: {{projectExperiences}}");
                    for (XWPFRun run : paragraph.getRuns()) {
                        String runText = run.getText(0);
                        if (runText != null && runText.contains("{{projectExperiences}}")) {
                            run.setText(runText.replace("{{projectExperiences}}", ""), 0);
                        }
                    }
                }
            }
        }
        
        // 确保模板行有足够的单元格(至少6个)
        while (templateRow.getTableCells().size() < 6) {
            System.out.println("为模板行添加单元格，当前单元格数: " + templateRow.getTableCells().size());
            templateRow.addNewTableCell();
        }
        
        // 计算需要显示的项目经验数量（最多10条）
        int displayCount = Math.min(projectExperiences.size(), 10);
        
        // Step 1: 先复制模板行，创建足够数量的行
        for (int i = 1; i < displayCount; i++) {
            // 如果行已存在，不需要创建新行
            if (i + 5 < table.getRows().size()) {
                System.out.println("使用已有行 #" + (i + 5));
                XWPFTableRow existingRow = table.getRow(i + 5);
                // 确保行有足够的单元格
                while (existingRow.getTableCells().size() < 6) {
                    System.out.println("为已有行 #" + (i + 5) + " 添加单元格，当前单元格数: " + existingRow.getTableCells().size());
                    existingRow.addNewTableCell();
                }
            } else {
                // 创建新行，复制模板行的属性
                XWPFTableRow newRow = table.createRow();
                newRow.getCtRow().setTrPr(templateRow.getCtRow().getTrPr());
                System.out.println("创建新行 #" + (i + 5) + "，复制模板行");
                
                // 确保新行有6个单元格
                while (newRow.getTableCells().size() < 6) {
                    XWPFTableCell newCell = newRow.addNewTableCell();
                    // 复制单元格属性
                    if (newRow.getTableCells().size() <= templateRow.getTableCells().size()) {
                        int cellIndex = newRow.getTableCells().size() - 1;
                        XWPFTableCell templateCell = templateRow.getCell(cellIndex);
                        if (templateCell.getCTTc().isSetTcPr()) {
                            newCell.getCTTc().setTcPr(templateCell.getCTTc().getTcPr());
                        }
                    }
                }
            }
        }
        
        // Step 2: 依次填充数据到行中
        for (int i = 0; i < displayCount; i++) {
            ProjectExperience exp = projectExperiences.get(i);
            int rowIndex = i + 5; // 从第6行(索引5)开始填充
            XWPFTableRow row = table.getRow(rowIndex);
            
            // 项目经验名称和时间
            String projectName = getValueOrDefault(exp.getProjectName(), "");
            String startDate = getValueOrDefault(exp.getStartDate(), "");
            String endDate = getValueOrDefault(exp.getEndDate(), "");
            String projectDesc = projectName + " (" + startDate + " - " + endDate + ")";
            
            // 项目角色
            String role = getValueOrDefault(exp.getRole(), "");
            
            System.out.println("填充第" + (rowIndex + 1) + "行: " + projectDesc + " - " + role);
            
            // 1. 先合并单元格
            mergeCellsHorizontally(table, rowIndex, 0, 4);
            
            // 2. 在合并后的单元格中写入项目名称和时间
            row.getCell(0).setText(projectDesc);
            
            // 3. 在最后一列写入角色
            row.getCell(5).setText(role);
        }
        
        System.out.println("项目经验表格渲染完成，当前表格共有 " + table.getRows().size() + " 行");
    }
    
    /**
     * 获取值或默认值
     */
    private static String getValueOrDefault(String value, String defaultValue) {
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }

    /**
     * 水平方向合并单元格
     * 
     * @param table 表格
     * @param row 行索引
     * @param fromCell 起始单元格索引
     * @param toCell 结束单元格索引
     */
    private void mergeCellsHorizontally(XWPFTable table, int row, int fromCell, int toCell) {
        try {
            System.out.println("合并单元格: 行" + row + "，从第" + fromCell + "列到第" + toCell + "列");
            XWPFTableRow tableRow = table.getRow(row);
            
            // 检查单元格数量，确保足够
            int cellCount = tableRow.getTableCells().size();
            if (cellCount <= toCell) {
                System.out.println("警告: 行" + row + "的单元格数量(" + cellCount + ")不足，需要先添加单元格");
                // 添加缺少的单元格
                while (tableRow.getTableCells().size() <= toCell) {
                    tableRow.addNewTableCell();
                }
            }
            
            // 检查并打印每个单元格内容
            for (int i = 0; i < tableRow.getTableCells().size(); i++) {
                System.out.println("行" + row + "的第" + i + "个单元格内容: " + tableRow.getCell(i).getText());
            }
            
            // 优化合并操作 - 使用直接设置CTTcPr的方式
            for (int cellIndex = fromCell; cellIndex <= toCell; cellIndex++) {
                XWPFTableCell cell = tableRow.getCell(cellIndex);
                CTTcPr tcPr = cell.getCTTc().getTcPr();
                if (tcPr == null) {
                    tcPr = cell.getCTTc().addNewTcPr();
                }
                
                if (cellIndex == fromCell) {
                    // 为第一个单元格设置RESTART
                    if (tcPr.getHMerge() == null) {
                        tcPr.addNewHMerge().setVal(STMerge.RESTART);
                    } else {
                        tcPr.getHMerge().setVal(STMerge.RESTART);
                    }
                    System.out.println("设置第" + cellIndex + "列为合并起始点");
                } else {
                    // 为后续单元格设置CONTINUE
                    if (tcPr.getHMerge() == null) {
                        tcPr.addNewHMerge().setVal(STMerge.CONTINUE);
                    } else {
                        tcPr.getHMerge().setVal(STMerge.CONTINUE);
                    }
                    
                    // 清空被合并单元格的内容，避免显示问题
                    for (XWPFParagraph p : cell.getParagraphs()) {
                        for (XWPFRun run : p.getRuns()) {
                            run.setText("", 0);
                        }
                    }
                    
                    System.out.println("设置第" + cellIndex + "列为合并继续点");
                }
            }
            
            System.out.println("单元格合并完成");
        } catch (Exception e) {
            System.out.println("合并单元格时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 
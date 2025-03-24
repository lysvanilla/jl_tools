package cn.sunline.mapping;

import cn.hutool.core.date.DateUtil;
import cn.idev.excel.ExcelWriter;
import cn.idev.excel.FastExcel;
import cn.idev.excel.write.metadata.WriteSheet;
import cn.sunline.util.BasicInfo;
import cn.sunline.vo.StandardizedMappingRelation;
import cn.sunline.vo.TableFieldInfo;
import cn.sunline.vo.TableStructure;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Slf4j
public class GenTableStructureExcel {
    // 定义 Excel 模板文件的路径，使用 BasicInfo 类中的 tpl_path 拼接而成
    private static final String TPL_PATH = BasicInfo.TPL_PATH + "excel" + File.separator + "字段信息模板.xlsx";
    // 定义基础导出路径，使用 BasicInfo 类的方法获取
    private static final String BASIC_EXPORT_PATH = BasicInfo.getBasicExportPath("");

    public static void writeTableStructureExcel(List<TableStructure> tableStructureList){
        String outputPath = BASIC_EXPORT_PATH + "标准化变更后物理模型" + DateUtil.format(DateUtil.date(), "YYYYMMdd_HHmmss") + ".xlsx";
        writeTableStructureExcel(tableStructureList,outputPath);
    }
    public static void writeTableStructureExcel(List<TableStructure> tableStructureList,String outputPath) {
        log.debug("开始检查 Excel 模板文件是否存在。");
        // 使用 Collections.sort 方法排序
        Collections.sort(tableStructureList, Comparator.comparing(TableStructure::getTableNameEn));

        log.debug("开始收集所有表结构信息中的表字段信息。");
        List<TableFieldInfo> tableFieldInfoListAll = new ArrayList<>();
        // 遍历表结构信息列表，收集表字段信息
        for (TableStructure tableStructure : tableStructureList) {
            List<TableFieldInfo> tableFieldInfoList = tableStructure.getFields();
            if (tableFieldInfoList != null) {
                tableFieldInfoListAll.addAll(tableFieldInfoList);
            }
        }
        log.info("共收集到 {} 条表字段信息，开始生成输出 Excel 文件路径。", tableFieldInfoListAll.size());
        log.debug("即将生成的 Excel 文件路径为: {}", outputPath);
        // 将表字段信息写入 Excel 文件
        writeTableStructureExcel(tableStructureList,tableFieldInfoListAll, TPL_PATH, outputPath);
    }

    public static void writeTableStructureExcel(List<TableStructure> tableStructureList,List<TableFieldInfo> tableFieldInfoList, String templatePath, String outputPath) {
        log.debug("开始检查 Excel 模板文件是否存在。");
        // 创建模板文件和输出文件的 File 对象
        File templateFile = new File(templatePath);
        File outputFile = new File(outputPath);
        // 检查模板文件是否存在
        if (!templateFile.exists()) {
            log.error("Excel 模板文件不存在，路径：{}，无法继续写入操作。", templatePath);
            return;
        }
        log.debug("Excel 模板文件存在，开始创建 ExcelWriter 对象。");
        ExcelWriter excelWriter = null;
        try {
            // 创建 ExcelWriter 对象，使用模板文件进行写入操作
            excelWriter = FastExcel.write(outputPath).withTemplate(templatePath).build();
            // 创建写入工作表对象，指定工作表名称为 "字段级信息"
            WriteSheet fieldsheet = FastExcel.writerSheet("字段级信息").build();
            WriteSheet tableSheet = FastExcel.writerSheet("表级信息").build();
            log.debug("开始向 Excel 文件写入 {} 条指标信息，输出路径：{}。", tableFieldInfoList.size(), outputPath);
            // 将表字段信息填充到 Excel 文件中
            excelWriter.fill(tableFieldInfoList, fieldsheet);
            excelWriter.fill(tableStructureList, tableSheet);
            log.info("成功向 Excel 文件写入 {} 条指标信息，输出路径：{}。", tableFieldInfoList.size(), outputPath);
        } catch (Exception e) {
            log.error("写入 Excel 文件时出现异常，输出路径：{}，异常信息：{}", outputPath, e.getMessage());
        } finally {
            if (excelWriter != null) {
                try {
                    // 关闭 ExcelWriter 对象
                    excelWriter.close();
                    log.debug("ExcelWriter 对象已成功关闭。");
                } catch (Exception e) {
                    log.error("关闭 ExcelWriter 时出现异常，输出路径：{}，异常信息：{}", outputPath, e.getMessage());
                }
            }
        }
        log.debug("转换成功，生成的 Excel 文件路径为：[{}]。", outputPath);
    }
}

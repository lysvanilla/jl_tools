package cn.sunline.mapping;

import cn.hutool.core.date.DateUtil;
import cn.idev.excel.ExcelWriter;
import cn.idev.excel.FastExcel;
import cn.idev.excel.write.metadata.WriteSheet;
import cn.sunline.util.BasicInfo;
import cn.sunline.vo.StandardizedMappingRelation;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.List;

@Slf4j
public class GenStandardizedInfoExcel {
    // 定义 Excel 模板文件的路径，使用 BasicInfo 类中的 tpl_path 拼接而成
    private static final String TPL_PATH = BasicInfo.TPL_PATH + "excel" + File.separator + "标准化变更.xlsx";
    // 定义基础导出路径，使用 BasicInfo 类的方法获取
    private static final String BASIC_EXPORT_PATH = BasicInfo.getBasicExportPath("");

    public static void writeStandardizedInfoExcel(List<StandardizedMappingRelation> standardizedMappingRelations) {
        String outputPath = BASIC_EXPORT_PATH + "标准化变更" + DateUtil.format(DateUtil.date(), "YYYYMMdd_HHmmss") + ".xlsx";
        log.debug("开始检查 Excel 模板文件是否存在。");
        // 创建模板文件和输出文件的 File 对象
        File templateFile = new File(TPL_PATH);
        File outputFile = new File(outputPath);
        // 检查模板文件是否存在
        if (!templateFile.exists()) {
            log.error("Excel 模板文件不存在，路径：{}，无法继续写入操作。", TPL_PATH);
            return;
        }
        log.debug("Excel 模板文件存在，开始创建 ExcelWriter 对象。");
        ExcelWriter excelWriter = null;
        try {
            // 创建 ExcelWriter 对象，使用模板文件进行写入操作
            excelWriter = FastExcel.write(outputPath).withTemplate(TPL_PATH).build();
            // 创建写入工作表对象，指定工作表名称为 "字段级信息"
            WriteSheet task_sheet = FastExcel.writerSheet("标准化变更").build();
            log.debug("开始向 Excel 文件写入 {} 条信息，输出路径：{}。", standardizedMappingRelations.size(), outputPath);
            // 将表字段信息填充到 Excel 文件中
            excelWriter.fill(standardizedMappingRelations, task_sheet);
            log.info("成功向 Excel 文件写入 {} 条信息，输出路径：{}。", standardizedMappingRelations.size(), outputPath);
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

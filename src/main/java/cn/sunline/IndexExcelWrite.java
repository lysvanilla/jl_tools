package cn.sunline;

import cn.hutool.core.io.FileUtil;
import cn.idev.excel.ExcelWriter;
import cn.idev.excel.FastExcel;
import cn.idev.excel.write.metadata.WriteSheet;
import cn.sunline.util.BasicInfo;
import cn.sunline.vo.IndexInfo;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * IndexExcelWrite 类用于将指定 Excel 文件中的指标信息进行过滤，并将过滤后的数据写入到 Excel 模板文件中。
 * 整个过程包括读取源文件、过滤数据、写入模板文件等步骤，同时会记录详细的日志信息。
 */
@Slf4j
public class IndexExcelWrite {
    // 定义模板文件路径，使用 BasicInfo 类中的 tpl_path 拼接模板文件所在目录和文件名
    private static final String TPL_PATH = BasicInfo.TPL_PATH + "excel" + File.separator + "指标清单模版.xlsx";
    // 定义基础导出路径，使用 BasicInfo 类的方法获取
    private static final String BASIC_EXPORT_PATH = BasicInfo.getBasicExportPath("");

    /**
     * 程序的入口方法，用于测试指标数据写入 Excel 的功能。
     *
     * @param args 命令行参数，此处未使用
     */
    public static void main(String[] args) {
        // 定义源文件路径
        String sourceFilePath = "D:\\svn\\jilin\\02.需求分析\\0202.智能风控系统\\智能风控系统指标信息_20250304.xlsx";
        try {
            // 记录开始执行指标数据写入 Excel 操作的日志，包含源文件路径
            log.info("开始执行指标数据写入 Excel 操作，源文件路径：{}", sourceFilePath);
            // 调用写入方法进行数据处理和写入
            writeIndexExcel(sourceFilePath);
            // 记录数据写入成功的日志
            log.info("数据写入成功！");
            // 输出提示信息到控制台
            System.out.println("数据写入成功！");
        } catch (Exception e) {
            // 记录执行过程中出现异常的日志，包含源文件路径和异常信息
            log.error("执行指标数据写入 Excel 操作时出现异常，源文件路径：{}", sourceFilePath, e);
        }
    }

    /**
     * 从参数映射中获取文件路径并调用写入方法。
     *
     * @param args_map 包含文件路径的参数映射，键为 "file_name"
     */
    public static void writeIndexExcel(HashMap<String, String> args_map) {
        // 从参数映射中获取文件路径
        String filePath = args_map.get("file_name");
        // 检查文件路径是否为空
        if (filePath == null) {
            // 若为空，记录错误日志
            log.error("args_map 中缺少 file_name 参数，无法继续执行");
            return;
        }
        // 调用另一个写入方法进行数据处理和写入
        writeIndexExcel(filePath);
    }

    /**
     * 根据指定的文件路径读取数据，过滤后写入 Excel 模板。
     *
     * @param filePath 源文件路径
     */
    public static void writeIndexExcel(String filePath) {
        // 检查源文件是否存在
        if (!FileUtil.exist(filePath)) {
            // 若不存在，记录错误日志
            log.error("file_name 参数对应的文件不存在, [{}]", filePath);
            return;
        }
        try {
            // 记录开始读取源 Excel 文件的日志，包含源文件路径
            log.info("开始读取源 Excel 文件：{}", filePath);
            // 调用 IndexExcelReader 类的 readExcel 方法读取源 Excel 文件中的指标信息
            List<IndexInfo> indexInfoList = IndexExcelReader.readExcel(filePath);
            // 若读取结果为空，将其初始化为空列表
            if (indexInfoList == null) {
                indexInfoList = new ArrayList<>();
            }
            // 记录成功读取到的指标信息数量
            log.info("成功读取源 Excel 文件，共读取到 {} 条指标信息", indexInfoList.size());

            // 记录开始过滤指标信息的日志
            log.info("开始过滤指标信息");
            // 过滤 applicationScenario 仅为“启用”和“未启用”的数据，并设置数据时效性为“批量”
            List<IndexInfo> filteredList = new ArrayList<>();
            for (IndexInfo info : indexInfoList) {
                if (("启用".equals(info.getIfEnabled()) || "未启用".equals(info.getIfEnabled()))
                        && "指标".equals(info.getDesignClassification())) {
                    info.setDataTimeliness("批量");
                    filteredList.add(info);
                }
            }
            // 记录过滤完成后筛选出的符合条件的指标信息数量
            log.info("过滤完成，共筛选出 {} 条符合条件的指标信息", filteredList.size());

            // 获取源文件的主文件名和扩展名
            String fileName = FileUtil.mainName(filePath);
            String extFileName = FileUtil.extName(filePath);

            // 构建输出文件的路径，使用基础导出路径、主文件名、日期和扩展名
            String outputFilePath = BASIC_EXPORT_PATH + fileName + "-自动转换" + BasicInfo.CURRENT_DATE + "." + extFileName;
            // 记录将过滤后的数据写入到 Excel 模板文件的日志，包含输出路径
            log.info("将过滤后的数据写入到 Excel 模板文件，输出路径：{}", outputFilePath);
            // 调用另一个写入方法将过滤后的数据写入 Excel 模板文件
            writeIndexExcel(filteredList, TPL_PATH, outputFilePath);
        } catch (Exception e) {
            // 记录处理文件过程中出现异常的日志，包含源文件路径和异常信息
            log.error("处理文件 {} 时出现异常", filePath, e);
        }
    }

    /**
     * 将指标信息列表写入指定的 Excel 模板文件。
     *
     * @param indexInfoList 指标信息列表
     * @param templatePath  模板文件路径
     * @param outputPath    输出文件路径
     */
    public static void writeIndexExcel(List<IndexInfo> indexInfoList, String templatePath, String outputPath) {
        // 创建模板文件和输出文件的 File 对象
        File templateFile = new File(templatePath);
        File outputFile = new File(outputPath);

        // 检查模板文件是否存在
        if (!templateFile.exists()) {
            // 若不存在，记录错误日志
            log.error("Excel 模板文件不存在，路径：{}", templatePath);
            return;
        }

        ExcelWriter excelWriter = null;
        try {
            // 创建 ExcelWriter 对象，使用模板文件进行写入操作
            excelWriter = FastExcel.write(outputPath).withTemplate(templatePath).build();
            // 创建写入工作表对象，指定工作表名称为 "指标数据"
            WriteSheet task_sheet = FastExcel.writerSheet("指标数据").build();
            // 记录开始向 Excel 文件写入数据的日志，包含指标信息数量和输出路径
            log.info("开始向 Excel 文件写入 {} 条指标信息，输出路径：{}", indexInfoList.size(), outputPath);
            // 将指标信息列表填充到指定工作表中
            excelWriter.fill(indexInfoList, task_sheet);
            // 记录成功向 Excel 文件写入数据的日志，包含输出路径
            log.info("成功向 Excel 文件写入数据，输出路径：{}", outputPath);
        } catch (Exception e) {
            // 记录写入 Excel 文件时出现异常的日志，包含输出路径和异常信息
            log.error("写入 Excel 文件时出现异常，输出路径：{}", outputPath, e);
        } finally {
            if (excelWriter != null) {
                try {
                    // 关闭 ExcelWriter 对象
                    excelWriter.close();
                } catch (Exception e) {
                    // 记录关闭 ExcelWriter 对象时出现异常的日志，包含输出路径和异常信息
                    log.error("关闭 ExcelWriter 时出现异常，输出路径：{}", outputPath, e);
                }
            }
        }
        // 记录转换成功的日志，包含输出路径
        log.info("转换成功：[{}]", outputPath);
    }
}
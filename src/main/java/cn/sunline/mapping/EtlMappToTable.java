package cn.sunline.mapping;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.idev.excel.ExcelWriter;
import cn.idev.excel.FastExcel;
import cn.idev.excel.write.metadata.WriteSheet;
import cn.sunline.table.ExcelTableStructureReader;
import cn.sunline.util.BasicInfo;
import cn.sunline.vo.TableFieldInfo;
import cn.sunline.vo.TableStructure;
import cn.sunline.vo.etl.EtlGroup;
import cn.sunline.vo.etl.EtlGroupColMapp;
import cn.sunline.vo.etl.EtlMapp;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cn.sunline.mapping.EtlMappingExcelRead.readEtlMappExcel;
import static cn.sunline.mapping.GenTableStructureExcel.writeTableStructureExcel;

/**
 * EtlMappToTable 类用于将 ETL 映射文件转换为表结构信息，并将结果写入 Excel 文件。
 * 该类提供了一系列方法，用于处理文件路径、读取 ETL 映射信息、转换为表结构和字段信息，
 * 最终将结果写入 Excel 文件。
 */
@Slf4j
public class EtlMappToTable {
    // 定义 Excel 模板文件的路径，使用 BasicInfo 类中的 tpl_path 拼接而成
    private static final String TPL_PATH = BasicInfo.TPL_PATH + "excel" + File.separator + "字段信息模板.xlsx";
    // 定义基础导出路径，使用 BasicInfo 类的方法获取
    private static final String BASIC_EXPORT_PATH = BasicInfo.getBasicExportPath("");

    /**
     * 程序入口方法，用于启动 ETL 映射转换任务。
     * @param args 命令行参数，此处未使用。
     */
    public static void main(String[] args) {
        log.debug("程序启动，开始执行 ETL 映射转换任务。");
        // 创建包含文件路径的参数映射
        Map<String, String> argsMap = new HashMap<>();
        argsMap.put("file_name", "D:\\svn\\jilin\\04.映射设计\\0401.基础模型层\\");
        //argsMap.put("model_file_name","D:\\svn\\jilin\\03.模型设计\\0303.基础模型层\\风险数据集市物理模型-基础层_v0.2.xlsx");
        argsMap.put("model_file_name",BasicInfo.baseModelPath);
        // 调用主处理方法
        etlMappToTableMain(argsMap);
        log.debug("程序执行完毕，ETL 映射转换任务完成。");
    }

    /**
     * 根据传入的参数映射启动 ETL 映射转换任务。
     * @param argsMap 包含文件路径的参数映射，键为 "file_name"。
     */
    public static void etlMappToTableMain(Map<String, String> argsMap) {
        log.debug("开始处理参数映射，准备获取文件路径。");
        // 从参数映射中获取文件路径
        String filePath = argsMap.get("file_name");
        String modelFilePath = argsMap.get("model_file_name");
        // 检查文件路径是否为空
        if (StringUtils.isBlank(filePath)) {
            log.error("argsMap 中缺少 file_name 参数，无法继续执行。");
            return;
        }
        if (StringUtils.isBlank(modelFilePath)) {
            log.error("argsMap中缺少model_file_name参数");
        }
        log.debug("成功从参数映射中获取文件路径: {}", filePath);
        // 调用处理单个文件路径的方法
        etlMappToTableMain(filePath,modelFilePath);
    }

    /**
     * 处理单个文件路径，将其转换为表结构信息并写入 Excel 文件。
     * @param filePath 要处理的文件或目录的路径。
     */
    public static void etlMappToTableMain(String filePath,String modelFilePath) {
        log.info("开始读取物理模型信息: {}", modelFilePath);
        LinkedHashMap<String, TableStructure> tableMap = ExcelTableStructureReader.readExcel(modelFilePath);

        log.info("开始处理文件路径: {}", filePath);
        List<TableStructure> tableStructureListAll = new ArrayList<>();
        // 检查文件路径是否为目录
        if (FileUtil.isDirectory(filePath)) {
            log.debug("文件路径为目录，开始遍历目录下的文件。");
            // 遍历目录下的所有文件
            for (File file : FileUtil.ls(filePath)) {
                String fileName = file.getName();
                // 检查是否需要处理当前文件
                if (fileName.endsWith(".xlsx") && !fileName.startsWith("~") && !fileName.endsWith("0_封面.xlsx")
                        && !fileName.endsWith("2_目录.xlsx") && !fileName.endsWith("1_变更记录.xlsx")
                    ) {
                    // 处理单个文件，获取表结构信息
                    tableStructureListAll.addAll(etlMappToTable(file.getAbsolutePath(), tableMap));
                }else{
                    log.debug("跳过文件: {}, 原因：文件名以 ~ 开头或不是 .xlsx 文件。", file.getAbsolutePath());
                    continue;
                }
            }
        } else {
            log.debug("文件路径为单个文件，开始处理该文件。");
            // 处理单个文件，获取表结构信息
            tableStructureListAll.addAll(etlMappToTable(filePath, tableMap));
        }
        log.info("共收集到 {} 个表结构信息，开始将其转换为表字段信息并写入 Excel 文件。", tableStructureListAll.size());
        // 将表结构信息转换为表字段信息并写入 Excel 文件
        etlMappToTable(tableStructureListAll);
        log.debug("文件路径处理完成，ETL 映射转换任务结束。");
    }

    /**
     * 从指定文件中读取 ETL 映射信息，并转换为表结构信息。
     * @param filePath 包含 ETL 映射信息的文件路径。
     * @return 转换后的表结构信息列表。
     */
    public static List<TableStructure> etlMappToTable(String filePath,LinkedHashMap<String, TableStructure> tableMap) {
        log.debug("开始从文件 {} 中读取 ETL 映射信息。", filePath);
        // 从文件中读取 ETL 映射信息
        List<EtlMapp> etlMappList = readEtlMappExcel(filePath);
        log.info("从文件 {} 中读取到 {} 条 ETL 映射信息。", filePath, etlMappList.size());
        List<TableStructure> tableStructureList = new ArrayList<>();
        // 遍历 ETL 映射信息，创建表结构信息
        for (EtlMapp etlMapp : etlMappList) {
            String tableEnglishName = etlMapp.getTableEnglishName();
            String tableChineseName = etlMapp.getTableChineseName();
            String analyst = etlMapp.getAnalyst();
            String primaryKeyField = etlMapp.getPrimaryKeyField();
            // 将主键字段字符串按逗号分割为列表
            List<String> primaryKeyFieldList = splitStringByComma(primaryKeyField);
            List<EtlGroup> etlGroupList = etlMapp.getEtlGroupList();
            TableStructure tableStructure = new TableStructure();

            TableStructure tableStructureModel = tableMap.get(tableEnglishName);
            LinkedHashMap<String, TableFieldInfo> fieldMap = new LinkedHashMap<>();
            if (tableStructureModel == null){
                log.error("模型文件中不存在表结构信息: {}", tableEnglishName);
            }else{
                BeanUtil.copyProperties(tableStructureModel,tableStructure);
                tableStructure.clearFieldsAndFieldMap();
                fieldMap = tableStructureModel.getFieldMap();
            }
            String systemModule = tableEnglishName.substring(0, 1);
            String subject = extractBetweenUnderscores(tableEnglishName);
            tableStructure.setTableNameEn(tableEnglishName);
            tableStructure.setTableNameCn(tableChineseName);
            tableStructure.setDesigner(analyst);
            tableStructure.setSystemModule(systemModule);
            tableStructure.setSubject(subject);
            // 如果 ETL 组列表不为空
            if (!etlGroupList.isEmpty()) {
                int etlGroupSize = etlGroupList.size();
                EtlGroup etlGroup = etlGroupList.get(0);
                if (etlGroupSize>1){
                    for (EtlGroup etlGroup1 :etlGroupList){
                        String templateType = etlGroup1.getTemplateType();
                        if(templateType.equals("N1")){
                            etlGroup = etlGroup1;
                            break;
                        }
                    }
                }

                String distributionKey = etlGroup.getDistributionKey();
                // 将分布键字段字符串按逗号分割为列表
                List<String> distributionKeyList = splitStringByComma(distributionKey);
                List<EtlGroupColMapp> etlGroupColMappList = etlGroup.getEtlGroupColMappList();
                int fieldOrder = 1;
                // 遍历 ETL 组列映射信息，创建表字段信息
                for (EtlGroupColMapp etlGroupColMapp : etlGroupColMappList) {
                    String targetTableEnglishName = etlGroupColMapp.getTargetTableEnglishName();
                    String targetTableChineseName = etlGroupColMapp.getTargetTableChineseName();
                    String targetFieldEnglishName = etlGroupColMapp.getTargetFieldEnglishName();
                    String targetFieldChineseName = etlGroupColMapp.getTargetFieldChineseName();
                    String targetFieldType = etlGroupColMapp.getTargetFieldType();

                    TableFieldInfo tableFieldInfo = new TableFieldInfo();

                    TableFieldInfo tableFieldInfoModel = fieldMap.get(targetFieldEnglishName);
                    if (tableFieldInfoModel != null){
                        BeanUtil.copyProperties(tableFieldInfoModel,tableFieldInfo);
                    }
                    tableFieldInfo.setSystemModule(systemModule);
                    tableFieldInfo.setSubject(subject);
                    tableFieldInfo.setTableNameEn(tableEnglishName);
                    tableFieldInfo.setTableNameCn(tableChineseName);
                    tableFieldInfo.setFieldNameEn(targetFieldEnglishName);
                    // 修正：之前重复设置了 setFieldNameEn，这里应该设置中文名称
                    tableFieldInfo.setFieldNameCn(targetFieldChineseName);
                    tableFieldInfo.setFieldType(targetFieldType);
                    tableStructure.addField(tableFieldInfo);
                    tableFieldInfo.setTableCreationType("切片");
                    // 如果字段英文名是 PART_DT，则设置为分区键
                    if ("PART_DT".equals(targetFieldEnglishName)) {
                        tableFieldInfo.setPartKey("Y");
                    }
                    // 如果字段中文名在主键字段列表中，则设置为主键
                    if (primaryKeyFieldList.contains(targetFieldChineseName)) {
                        tableFieldInfo.setPrimaryKey("Y");
                    }
                    // 如果字段英文名在分布键字段列表中，则设置为分布键
                    if (distributionKeyList.contains(targetFieldEnglishName)) {
                        tableFieldInfo.setBucketKey("Y");
                    }
                    tableFieldInfo.setFieldOrder(fieldOrder);
                    fieldOrder++;
                }
            }
            tableStructureList.add(tableStructure);
        }
        log.info("将文件 {} 中的 ETL 映射信息转换为 {} 个表结构信息。", filePath, tableStructureList.size());
        return tableStructureList;
    }

    /**
     * 将表结构信息转换为表字段信息，并写入 Excel 文件。
     * @param tableStructureList 要处理的表结构信息列表。
     */
    public static void etlMappToTable(List<TableStructure> tableStructureList) {
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
        // 生成输出 Excel 文件的路径
        String outputFilePath = BASIC_EXPORT_PATH + "映射文档转物理模型" + DateUtil.format(DateUtil.date(), "YYYYMMdd_HHmmss") + ".xlsx";
        log.debug("即将生成的 Excel 文件路径为: {}", outputFilePath);
        // 将表字段信息写入 Excel 文件
        writeTableStructureExcel(tableStructureList,tableFieldInfoListAll, TPL_PATH, outputFilePath);
    }

    /**
     * 将表字段信息写入 Excel 文件。
     * @param tableFieldInfoList 要写入的表字段信息列表。
     * @param templatePath Excel 模板文件的路径。
     * @param outputPath 输出 Excel 文件的路径。
     */
    /*public static void writeTableExcel(List<TableFieldInfo> tableFieldInfoList, String templatePath, String outputPath) {
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
            WriteSheet task_sheet = FastExcel.writerSheet("字段级信息").build();
            log.debug("开始向 Excel 文件写入 {} 条信息，输出路径：{}。", tableFieldInfoList.size(), outputPath);
            // 将表字段信息填充到 Excel 文件中
            excelWriter.fill(tableFieldInfoList, task_sheet);
            log.info("成功向 Excel 文件写入 {} 条信息，输出路径：{}。", tableFieldInfoList.size(), outputPath);
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

    public static void writeTableExcel(List<TableStructure> tableStructureList,List<TableFieldInfo> tableFieldInfoList, String templatePath, String outputPath) {
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
    }*/

    /**
     * 将字符串按逗号分割为列表，同时处理中文逗号。
     * @param input 要分割的字符串。
     * @return 分割后的字符串列表，如果输入为 null 则返回 null。
     */
    public static List<String> splitStringByComma(String input) {
        if (input == null) {
            return null;
        }
        // 将中文逗号替换为英文逗号
        input = input.replace("、", ",");
        // 按英文逗号分割字符串并返回列表
        return Arrays.asList(input.split(","));
    }

    /**
     * 提取字符串中第一个 _ 和第二个 _ 中间的字母
     * @param input 输入的字符串
     * @return 第一个 _ 和第二个 _ 中间的字母，如果未找到则返回 null
     */
    public static String extractBetweenUnderscores(String input) {
        if (input == null) {
            return null;
        }
        // 定义正则表达式模式
        String regex = "^[^_]*_([A-Za-z]+)_.*$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        if (matcher.matches()) {
            return matcher.group(1);
        }
        return null;
    }
}

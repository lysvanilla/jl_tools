package cn.sunline.mapping;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.sunline.util.BasicInfo;
import cn.sunline.vo.etl.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.*;

import static cn.sunline.mapping.EtlMappingExcelRead.readEtlMappExcel;
import static cn.sunline.util.BasicInfo.TEMPLATE_SETTING;

/**
 * SqlTemplateFiller 类用于根据 Excel 文件中的表结构信息生成 DML SQL 语句。
 * 它会读取 Excel 文件，获取表结构信息，然后根据模板填充数据，最终生成 DML SQL 文件。
 */
@Slf4j
public class DmlTemplateFiller {
    // 定义导出文件的基础路径，通过 BasicInfo 类的方法获取
    public static final String BASE_EXPORT_PATH = BasicInfo.getBasicExportPath("autocode"+ File.separator+"dml");
    private static final String TPL_PATH = BasicInfo.TPL_PATH + "sql/dml/inceptor/";

    // 模板文件内容
    private static final String MAIN_TPL = readTemplate(TPL_PATH + "dml_template.sql");
    private static final String MAPP_N1_TPL = readTemplate(TPL_PATH + "dml_template_mapping_N1.sql");
    private static final String MAPP_N2_TPL = readTemplate(TPL_PATH + "dml_template_mapping_N2.sql");
    private static final String MAPP_Y1_TPL = readTemplate(TPL_PATH + "dml_template_mapping_Y1.sql");
    private static final String MAPP_Y2_TPL = readTemplate(TPL_PATH + "dml_template_mapping_Y2.sql");
    private static final String MAPP_Y3_TPL = readTemplate(TPL_PATH + "dml_template_mapping_Y3.sql");
    private static final String MAPP_DQC_TPL = readTemplate(TPL_PATH + "dml_template_mapping_DQC.sql");
    private static final String MAPP_D1_TPL = readTemplate(TPL_PATH + "dml_template_mapping_D1.sql");

    // 读取模板文件内容
    private static String readTemplate(String filePath) {
        try {
            return new FileReader(filePath).readString();
        } catch (Exception e) {
            log.error("读取模板文件失败: {}", filePath, e);
            return "";
        }
    }

    /**
     * 程序入口方法，用于测试生成 DML SQL 语句的功能。
     *
     * @param args 命令行参数，此处未使用
     */
    public static void main(String[] args) {
        // 定义 Excel 文件的路径
        // 调用 genDmlSql 方法生成 DML SQL 语句
        //genDmlSql("D:\\svn\\jilin\\04.映射设计\\0402.计量模型层\\宝奇订单指标表.xlsx");
        Map<String, String> argsMap = new HashMap<>();
        //argsMap.put("file_name","D:\\svn\\jilin\\04.映射设计\\0402.计量模型层\\");
        //argsMap.put("file_name","D:\\svn\\jilin\\04.映射设计\\0402.计量模型层\\宝奇订单指标表.xlsx");
        argsMap.put("file_name","D:\\svn\\jilin\\04.映射设计\\0401.基础模型层\\总账科目余额.xlsx");
        new DmlTemplateFiller().genDmlSqlMain(argsMap);
    }

    /**
     * 重载的 genDmlSql 方法，接受一个包含参数的 HashMap。
     * 从 HashMap 中获取文件路径，并调用另一个 genDmlSql 方法生成 DML SQL 语句。
     *
     * @param argsMap 包含参数的 HashMap，其中应包含 "file_name" 键，对应 Excel 文件的路径
     */
    public void genDmlSqlMain(Map<String, String> argsMap) {
        // 从 HashMap 中获取文件路径
        String filePath = argsMap.get("file_name");
        // 检查文件路径是否为空
        if (StringUtils.isBlank(filePath)) {
            // 若为空，记录错误日志
            log.error("argsMap中缺少file_name参数");
            return;
        }
        // 调用另一个 genDmlSql 方法生成 DML SQL 语句
        genDmlSqlMain(filePath);
    }

    /**
     * 生成 DML SQL 语句的核心方法。
     * 该方法会读取指定路径的 Excel 文件，获取表结构信息，然后为每个表生成 DML SQL 语句并保存到文件中。
     *
     * @param filePath Excel 文件的路径
     */
    public static void genDmlSqlMain(String filePath) {
        if (FileUtil.isDirectory(filePath)){
            for (File file : FileUtil.ls(filePath)) {
                String fileName = file.getName();
                if (fileName.startsWith("~") && !fileName.equals(".xlsx")){
                    continue;
                }
                genDmlSql(file.getAbsolutePath());
            }
        }else{
            genDmlSql(filePath);
        }
    }

    /**
     * 生成 DML SQL 语句的核心方法。
     * 该方法会读取指定路径的 Excel 文件，获取表结构信息，然后为每个表生成 DML SQL 语句并保存到文件中。
     *
     * @param filePath Excel 文件的路径
     */
    public static void genDmlSql(String filePath) {
        // 检查文件是否存在
        if (!FileUtil.exist(filePath)) {
            // 若文件不存在，记录错误日志
            log.error("file_name参数对应的文件不存在: [{}]", filePath);
            return;
        }

        // 调用 ExcelTableStructureReader 类的 readExcel 方法读取 Excel 文件，获取表结构信息
        List<EtlMapp> etlMappList = readEtlMappExcel(filePath);
        // 检查是否成功获取表结构信息
        if (etlMappList == null) {
            // 若未获取到，记录错误日志
            log.error("读取Excel文件失败，无法获取mapping信息");
            return;
        }

        for (EtlMapp etlMapp : etlMappList) {
            String tableEnglishName = StringUtils.lowerCase(etlMapp.getTableEnglishName());
            String tableChineseName = etlMapp.getTableChineseName();
            try {
                // 调用 fillTemplate 方法填充模板，生成 DML SQL 语句
                String filledSql = fillTemplate(etlMapp);
                // 定义导出文件的路径
                String outputPath = BASE_EXPORT_PATH + tableEnglishName + ".sql";
                // 将生成的 DML SQL 语句写入文件
                FileUtil.writeString(filledSql, outputPath, "UTF-8");
                // 记录成功日志
                log.info("dml建表语句生成成功 [{}]-[{}]，输出文件路径: [{}]", tableEnglishName, tableChineseName, outputPath);
            } catch (Exception e) {
                // 若生成过程中出现异常，记录错误日志
                log.error("生成表 [{}]-[{}] 的DML语句时出错", tableEnglishName, tableChineseName, e);
            }
        }
    }

    /**
     * 根据映射信息填充模板，生成 DML SQL 语句。
     *
     * @param etlMapp 映射信息对象
     * @return 填充后的 DML SQL 语句
     */
    public static String fillTemplate(EtlMapp etlMapp) {
        // 替换主模板中的占位符
        String mainSql = replaceMainTemplate(etlMapp);

        // 构建分组 SQL 列表
        List<String> groupList = buildGroupList(etlMapp);

        // 替换主模板中的分组 SQL 占位符
        mainSql = mainSql.replace("${mapping}", String.join("\n", groupList));

        // 返回填充后的 DML SQL 语句
        return mainSql;
    }

    // 获取表的 schema
    private static String getTableSchema(String attributionLevel) {
        String tableSchema = TEMPLATE_SETTING.get(attributionLevel);
        if (StringUtils.isBlank(tableSchema)) {
            log.error("归属层次 [{}] 未在配置对应的schema", attributionLevel);
            tableSchema = "未配置";
        }
        return tableSchema;
    }

    // 构建更新日志列表
    private static List<String> buildUpdateLogList(List<EtlUpdateRecord> etlUpdateRecordList) {
        List<String> updateLogList = new ArrayList<>();
        updateLogList.add("");
        for (EtlUpdateRecord etlUpdateRecord : etlUpdateRecordList) {
            String date = etlUpdateRecord.getDate();
            String updater = etlUpdateRecord.getUpdater();
            String updateDescription = etlUpdateRecord.getDescription();
            updateLogList.add("1.0.0                "+date + "                     " + updater + StrUtil.repeat(" ", 21-updater.length()) + updateDescription);
        }
        return updateLogList;
    }

    // 替换主模板中的占位符
    private static String replaceMainTemplate(EtlMapp etlMapp) {
        // 获取表信息
        String tableEnglishName = StringUtils.lowerCase(etlMapp.getTableEnglishName());
        String tableChineseName = etlMapp.getTableChineseName();
        String creationDate = etlMapp.getCreationDate();
        String primaryKeyField = etlMapp.getPrimaryKeyField();
        String attributionLevel = etlMapp.getAttributionLevel();  // 归属层次
        String attributionTheme = etlMapp.getAttributionTheme();  // 归属主题
        String mainApplication = etlMapp.getMainApplication();
        String analyst = etlMapp.getAnalyst();
        String timeGranularity = etlMapp.getTimeGranularity();
        String retentionPeriod = etlMapp.getRetentionPeriod();
        String description = etlMapp.getDescription();
        String tableSchema = getTableSchema(attributionLevel);

        // 构建更新日志列表
        List<String> updateLogList = buildUpdateLogList(etlMapp.getEtlUpdateRecordList());
        List<String> descriptionList = Arrays.asList(description.split("\n"));
        String mainSql = MAIN_TPL;
        mainSql = replacePlaceholder(mainSql, "${belong_level}", attributionLevel);
        mainSql = replacePlaceholder(mainSql, "${target_table_schema}", tableSchema);
        mainSql = replacePlaceholder(mainSql, "${target_table_cn_name}", tableChineseName);
        mainSql = replacePlaceholder(mainSql, "${mapping_analyst}", analyst);
        mainSql = replacePlaceholder(mainSql, "${create_time}", creationDate);
        mainSql = replacePlaceholder(mainSql, "${target_table_en_name}", tableEnglishName);
        mainSql = replacePlaceholder(mainSql, "${primary_key}", primaryKeyField);
        mainSql = replacePlaceholder(mainSql, "${belong_subject}", attributionTheme);
        mainSql = replacePlaceholder(mainSql, "${main_application}", mainApplication);
        mainSql = replacePlaceholder(mainSql, "${time_granule}", timeGranularity);
        mainSql = replacePlaceholder(mainSql, "${retention_period}", retentionPeriod);
        mainSql = replacePlaceholder(mainSql, "${table_comment}", String.join("\n                  ", descriptionList));
        mainSql = replacePlaceholder(mainSql, "${update_log}", String.join("\n   ", updateLogList));
        return mainSql;
    }

    // 构建分组 SQL 列表
    private static List<String> buildGroupList(EtlMapp etlMapp) {
        List<String> groupList = new ArrayList<>();
        String attributionLevel = etlMapp.getAttributionLevel();  //归属层次
        String tableSchema = getTableSchema(attributionLevel);
        List<EtlGroup> etlGroupList = etlMapp.getEtlGroupList();
        for (EtlGroup etlGroup : etlGroupList) {
            String templateType = etlGroup.getTemplateType();

            // 获取分组模板
            String groupMappingSql = getGroupMappingTemplate(templateType);

            // 替换分组模板中的占位符
            groupMappingSql = replaceGroupTemplate(groupMappingSql, tableSchema, etlGroup);

            // 构建目标列名列表
            List<String> targetColumnNamesList = buildTargetColumnNamesList(etlGroup.getEtlGroupColMappList());
            // 构建映射详情列表
            List<String> mappingDetailList = buildMappingDetailList(etlGroup.getEtlGroupColMappList());
            // 构建目标列名带数据类型列表
            List<String> targetColumnNamesWithDataTypeList = buildTargetColumnNamesWithDataTypeList(etlGroup.getEtlGroupColMappList());
            // 构建表关联信息列表
            List<String> tableRelationList = buildTableRelationList(etlGroup.getEtlGroupJoinInfoList());

            // 替换分组模板中的占位符
            groupMappingSql = groupMappingSql.replace("${target_column_names}", appendLineStr(String.join("\n,", targetColumnNamesList)));
            groupMappingSql = groupMappingSql.replace("${mapping_detail}", appendLineStr(String.join("\n,", mappingDetailList)));
            groupMappingSql = groupMappingSql.replace("${table_relation}", String.join("\n", tableRelationList));
            groupMappingSql = groupMappingSql.replace("${target_column_names_with_data_type}", appendLineStr(String.join("\n,", targetColumnNamesWithDataTypeList)));

            groupList.add(groupMappingSql);
        }
        return groupList;
    }

    // 根据模板类型获取分组映射模板
    private static String getGroupMappingTemplate(String templateType) {
        switch (templateType) {
            case "Y1":
                return MAPP_Y1_TPL;
            case "Y2":
                return MAPP_Y2_TPL;
            case "Y3":
                return MAPP_Y3_TPL;
            case "N1":
                return MAPP_N1_TPL;
            case "N2":
                return MAPP_N2_TPL;
            case "D1":
                return MAPP_D1_TPL;
            default:
                log.error("未找到匹配的分组模板类型: [{}]", templateType);
                return "";
        }
    }

    // 替换分组模板中的占位符
    private static String replaceGroupTemplate(String groupMappingSql, String tableSchema, EtlGroup etlGroup) {
        String targetTableEnglishName = StringUtils.lowerCase(etlGroup.getTargetTableEnglishName());
        String targetTableChineseName = etlGroup.getTargetTableChineseName();
        String groupRemarks = etlGroup.getGroupRemarks();
        String distributionKey = etlGroup.getDistributionKey();
        String filterCondition = etlGroup.getFilterCondition();
        String groupingCondition = etlGroup.getGroupingCondition();
        String sortingCondition = etlGroup.getSortingCondition();

        groupMappingSql = replacePlaceholder(groupMappingSql, "${target_table_schema}", tableSchema);
        groupMappingSql = replacePlaceholder(groupMappingSql, "${target_table_cn_name}", targetTableChineseName);
        groupMappingSql = replacePlaceholder(groupMappingSql, "${target_table_comment}", groupRemarks);
        groupMappingSql = replacePlaceholder(groupMappingSql, "${target_table_name}", targetTableEnglishName);

        if (StringUtils.isNotEmpty(filterCondition)) {
            filterCondition = "where " + filterCondition;
        }
        if (StringUtils.isNotEmpty(groupingCondition)) {
            groupingCondition = "group by " + groupingCondition;
        }
        if (StringUtils.isNotEmpty(sortingCondition)) {
            sortingCondition = "order by " + sortingCondition;
        }

        groupMappingSql = replacePlaceholder(groupMappingSql, "${where_condition}", filterCondition);
        groupMappingSql = replacePlaceholder(groupMappingSql, "${groupby_condition}", groupingCondition);
        groupMappingSql = replacePlaceholder(groupMappingSql, "${orderby_condition}", sortingCondition);
        groupMappingSql = replacePlaceholder(groupMappingSql, "@{db_key}", distributionKey);
        return groupMappingSql;
    }

    // 构建目标列名列表
    private static List<String> buildTargetColumnNamesList(List<EtlGroupColMapp> etlGroupColMappList) {
        List<String> targetColumnNamesList = new ArrayList<>();
        for (EtlGroupColMapp etlGroupColMapp : etlGroupColMappList) {
            String targetFieldEnglishName = etlGroupColMapp.getTargetFieldEnglishName();
            String targetFieldChineseName = etlGroupColMapp.getTargetFieldChineseName();
            targetColumnNamesList.add(targetFieldEnglishName + "    -- " + targetFieldChineseName);
        }
        return targetColumnNamesList;
    }

    // 构建映射详情列表
    private static List<String> buildMappingDetailList(List<EtlGroupColMapp> etlGroupColMappList) {
        List<String> mappingDetailList = new ArrayList<>();
        for (EtlGroupColMapp etlGroupColMapp : etlGroupColMappList) {
            String targetFieldEnglishName = etlGroupColMapp.getTargetFieldEnglishName();
            String targetFieldChineseName = etlGroupColMapp.getTargetFieldChineseName();
            String mappingRule = etlGroupColMapp.getMappingRule();
            String sourceFieldChineseName = etlGroupColMapp.getSourceFieldChineseName();
            String sourceFieldEnglishName = etlGroupColMapp.getSourceFieldEnglishName();
            String groupMappingColRemark = etlGroupColMapp.getRemarks();

            if (StringUtils.isEmpty(sourceFieldChineseName)) {
                sourceFieldChineseName = targetFieldChineseName;
            }
            if (StringUtils.isBlank(mappingRule)) {
                mappingRule = sourceFieldEnglishName;
            }
            String mappingDetailCol = mappingRule + " as " + targetFieldEnglishName + "    -- " + sourceFieldChineseName + "  " + groupMappingColRemark;
            if (mappingRule.contains(" as ")) {
                mappingDetailCol = mappingRule + "    -- " + sourceFieldChineseName + "  " + groupMappingColRemark;
            }
            mappingDetailList.add(mappingDetailCol);
        }
        return mappingDetailList;
    }

    // 构建目标列名带数据类型列表
    private static List<String> buildTargetColumnNamesWithDataTypeList(List<EtlGroupColMapp> etlGroupColMappList) {
        List<String> targetColumnNamesWithDataTypeList = new ArrayList<>();
        for (EtlGroupColMapp etlGroupColMapp : etlGroupColMappList) {
            String targetFieldEnglishName = etlGroupColMapp.getTargetFieldEnglishName();
            String targetFieldChineseName = etlGroupColMapp.getTargetFieldChineseName();
            String targetFieldType = etlGroupColMapp.getTargetFieldType();
            targetColumnNamesWithDataTypeList.add(targetFieldEnglishName + "  " + targetFieldType + "    -- " + targetFieldChineseName);
        }
        return targetColumnNamesWithDataTypeList;
    }

    // 构建表关联信息列表
    private static List<String> buildTableRelationList(List<EtlGroupJoinInfo> etlGroupJoinInfoList) {
        List<String> tableRelationList = new ArrayList<>();
        for (EtlGroupJoinInfo etlGroupJoinInfo : etlGroupJoinInfoList) {
            String sourceTableSchema = etlGroupJoinInfo.getSourceTableSchema();
            String sourceTableEnglishName = etlGroupJoinInfo.getSourceTableEnglishName();
            String sourceTableChineseName = etlGroupJoinInfo.getSourceTableChineseName();
            String joinType = etlGroupJoinInfo.getJoinType();
            String sourceTableAlias = etlGroupJoinInfo.getSourceTableAlias();
            String comment = etlGroupJoinInfo.getComment();
            String joinCondition = etlGroupJoinInfo.getJoinCondition();

            /*if (StringUtils.isBlank(sourceTableEnglishName)) {
                continue;
            }*/

            String joinTableFullName = sourceTableEnglishName;
            if (StringUtils.isNotEmpty(sourceTableSchema)) {
                joinTableFullName = sourceTableSchema + "." + sourceTableEnglishName;
            }
            joinTableFullName = appendLineStr(joinTableFullName).trim();

            String joinInfo;
            if (StringUtils.isEmpty(joinType)) {
                joinInfo = "FROM " + joinTableFullName + " " + sourceTableAlias + "    --" + sourceTableChineseName + "  " + comment;
            } else {
                joinInfo = joinType + " " + joinTableFullName + " " + sourceTableAlias + "    --" + sourceTableChineseName + "  " + comment;
                joinInfo = joinInfo + "\n" + appendLineStr(" ON " + joinCondition);
            }
            tableRelationList.add(joinInfo);
        }
        return tableRelationList;
    }

    // 替换占位符的通用方法
    private static String replacePlaceholder(String template, String placeholder, String value) {
        return template.replaceAll(ReUtil.escape(placeholder), ReUtil.escape(value));
    }

    /**
     * 根据 SQL 类型和算法类型获取模板文件名。
     *
     * @param sqlType       SQL 类型，如 "ddl"
     * @param algorithmType 算法类型
     * @return 模板文件名
     */
    public static String getTplName(String sqlType, String algorithmType) {
        // 拼接模板文件名的键
        String tplFileNameKey = String.join("_", sqlType, StringUtils.lowerCase(algorithmType), "tpl");
        // 从模板设置中获取模板文件名
        String tplFileName = TEMPLATE_SETTING.get(tplFileNameKey);
        // 检查模板文件名是否为空
        if (StringUtils.isEmpty(tplFileName)) {
            // 若为空，记录错误日志
            log.error("sql模板查询失败，未找到匹配的模板: [{}]", tplFileNameKey);
        }
        // 返回模板文件名
        return tplFileName;
    }

    public static String appendLineStr(String src_str, String append_str) {
        String[] src_str_arr = src_str.split("\n");
        return String.join("\n", ArrayUtil.edit(src_str_arr, t -> append_str + t));
    }

    public static String appendLineStr(String src_str) {
        String append_str = "    ";
        return appendLineStr(src_str, append_str);
    }
}
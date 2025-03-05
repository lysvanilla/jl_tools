package cn.sunline;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.util.ReUtil;
import cn.sunline.util.BasicInfo;
import cn.sunline.vo.TableFieldInfo;
import cn.sunline.vo.TableStructure;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import java.util.*;

import static cn.sunline.util.BasicInfo.TEMPLATE_SETTING;
import static cn.sunline.util.GetTemplateInfo.getCircleLine;

/**
 * SqlTemplateFiller 类用于根据 Excel 文件中的表结构信息生成 DDL SQL 语句。
 * 它会读取 Excel 文件，获取表结构信息，然后根据模板填充数据，最终生成 DDL SQL 文件。
 */
@Slf4j
public class SqlTemplateFiller {
    // 定义导出文件的基础路径，通过 BasicInfo 类的方法获取
    public static final String base_export_path = BasicInfo.getBasicExportPath("autocode");

    /**
     * 程序入口方法，用于测试生成 DDL SQL 语句的功能。
     *
     * @param args 命令行参数，此处未使用
     */
    public static void main(String[] args) {
        // 定义 Excel 文件的路径
        String filePath = "D:\\svn\\jilin\\03.模型设计\\风险数据集市物理模型-模板.xlsx";
        // 调用 genDdlSql 方法生成 DDL SQL 语句
        genDdlSql(filePath);
    }

    /**
     * 重载的 genDdlSql 方法，接受一个包含参数的 HashMap。
     * 从 HashMap 中获取文件路径，并调用另一个 genDdlSql 方法生成 DDL SQL 语句。
     *
     * @param args_map 包含参数的 HashMap，其中应包含 "file_name" 键，对应 Excel 文件的路径
     */
    public void genDdlSql(HashMap<String, String> args_map) {
        // 从 HashMap 中获取文件路径
        String filePath = args_map.get("file_name");
        // 检查文件路径是否为空
        if (filePath == null) {
            // 若为空，记录错误日志
            log.error("args_map中缺少file_name参数");
            return;
        }
        // 调用另一个 genDdlSql 方法生成 DDL SQL 语句
        genDdlSql(filePath);
    }

    /**
     * 生成 DDL SQL 语句的核心方法。
     * 该方法会读取指定路径的 Excel 文件，获取表结构信息，然后为每个表生成 DDL SQL 语句并保存到文件中。
     *
     * @param filePath Excel 文件的路径
     */
    public static void genDdlSql(String filePath) {
        // 检查文件是否存在
        if (!FileUtil.exist(filePath)) {
            // 若文件不存在，记录错误日志
            log.error("file_name参数对应的文件不存在,[{}]", filePath);
            return;
        }

        // 调用 ExcelTableStructureReader 类的 readExcel 方法读取 Excel 文件，获取表结构信息
        LinkedHashMap<String, TableStructure> tableMap = ExcelTableStructureReader.readExcel(filePath);
        // 检查是否成功获取表结构信息
        if (tableMap == null) {
            // 若未获取到，记录错误日志
            log.error("读取Excel文件失败，无法获取表结构信息");
            return;
        }

        // 遍历表结构信息
        for (Map.Entry<String, TableStructure> entry : tableMap.entrySet()) {
            // 获取表结构对象
            TableStructure tableStructure = entry.getValue();
            // 将系统模块名转换为小写
            String systemModule = StringUtils.lowerCase(tableStructure.getSystemModule());
            // 将表英文名转换为小写
            String tableNameEn = StringUtils.lowerCase(tableStructure.getTableNameEn());
            // 将表中文名转换为小写
            String tableNameCn = StringUtils.lowerCase(tableStructure.getTableNameCn());

            try {
                // 调用 fillTemplate 方法填充模板，生成 DDL SQL 语句
                String filledSql = fillTemplate(tableStructure);
                // 定义导出文件的路径
                String outputPath = base_export_path + "create_table_" + tableNameEn + "_.sql";
                // 将生成的 DDL SQL 语句写入文件
                FileUtil.writeString(filledSql, outputPath, "UTF-8");
                // 记录成功日志
                log.info("ddl建表语句生成功[{}]-[{}]，输出文件路径: [{}]", tableNameEn, tableNameCn, outputPath);
            } catch (Exception e) {
                // 若生成过程中出现异常，记录错误日志
                log.error("生成表[{}]-[{}]的DDL语句时出错", tableNameEn, tableNameCn, e);
            }
        }
    }

    /**
     * 根据表结构信息填充模板，生成 DDL SQL 语句。
     *
     * @param tableStructure 表结构信息对象
     * @return 填充后的 DDL SQL 语句
     */
    public static String fillTemplate(TableStructure tableStructure) {
        // 获取模板文件名
        String tplFileName = getTplName("ddl", tableStructure.getAlgorithmType());
        // 检查模板文件名是否为空
        if (StringUtils.isEmpty(tplFileName)) {
            // 若为空，记录错误日志
            log.error("未找到合适的SQL模板，无法生成DDL语句");
            return "";
        }
        // 拼接模板文件的完整路径
        String tplFilePath = BasicInfo.TPL_PATH + tplFileName;
        // 读取模板文件内容
        String tplInfo = new FileReader(tplFilePath).readString();
        // 获取模板文件中的循环行信息
        List<String> circleLineList = getCircleLine(tplFilePath);
        // 获取表英文名
        String tableNameEn = tableStructure.getTableNameEn();
        // 获取表中文名
        String tableNameCn = tableStructure.getTableNameCn();
        // 获取表的字段信息
        LinkedHashMap<String, TableFieldInfo> fieldMap = tableStructure.getFieldMap();

        // 替换模板中的表英文名和表中文名
        String exportSql = tplInfo.replace("${table_name_en}", tableNameEn)
                .replace("${table_name_cn}", tableNameCn);

        // 存储分桶键的列表
        List<String> bucketKeys = new ArrayList<>();
        // 遍历表的字段信息，找出分桶键
        for (TableFieldInfo field : tableStructure.getFields()) {
            if ("Y".equals(field.getBucketKey())) {
                bucketKeys.add(field.getFieldNameEn());
            }
        }
        // 将分桶键列表拼接成字符串
        String bucketKeyStr = String.join(",", bucketKeys);
        // 检查分桶键字符串是否为空
        if (StringUtils.isEmpty(bucketKeyStr)) {
            // 若为空，记录错误日志
            log.error("分桶键不允许为空,[{}]-[{}]", tableNameEn, tableNameCn);
            //return "";
        }
        // 替换模板中的分桶键
        exportSql = exportSql.replace("${bucketKey}", bucketKeyStr);

        // 遍历模板中的循环行信息
        for (String circleLineTpl : circleLineList) {
            // 检查循环行模板是否包含特定关键字
            if (circleLineTpl.contains("column_name_en}") || circleLineTpl.contains("column_type}")
                    || circleLineTpl.contains("column_name_cn}") || circleLineTpl.contains("column_default}")) {
                // 存储替换后的循环行的列表
                List<String> circleLineReplaceList = new ArrayList<>();
                // 遍历表的字段信息
                for (TableFieldInfo tableFieldInfo : fieldMap.values()) {
                    // 将字段英文名转换为小写
                    String fieldNameEn = StringUtils.lowerCase(tableFieldInfo.getFieldNameEn());
                    // 将字段中文名转换为小写
                    String fieldNameCn = StringUtils.lowerCase(tableFieldInfo.getFieldNameCn());
                    // 将字段类型转换为小写
                    String fieldType = StringUtils.lowerCase(tableFieldInfo.getFieldType());
                    // 将字段是否非空标识转换为小写
                    String notNull = StringUtils.lowerCase(tableFieldInfo.getNotNull());
                    // 根据字段是否非空标识生成相应的 SQL 语句
                    String ifNull = "Y".equals(notNull) ? "not null" : "default null";

                    // 复制循环行模板
                    String circleLine = circleLineTpl;
                    // 替换循环行模板中的字段英文名
                    circleLine = circleLine.replaceAll("\\@\\{column_name_en}", ReUtil.escape(StringUtils.defaultString(fieldNameEn, "")))
                            // 替换循环行模板中的字段中文名
                            .replaceAll("\\@\\{column_name_cn}", ReUtil.escape(StringUtils.defaultString(fieldNameCn, "")))
                            // 替换循环行模板中的字段类型
                            .replaceAll("\\@\\{column_type}", ReUtil.escape(StringUtils.defaultString(fieldType, "")))
                            // 替换循环行模板中的字段是否非空信息
                            .replaceAll("\\@\\{if_null}", ReUtil.escape(StringUtils.defaultString(ifNull, "")));

                    // 将替换后的循环行添加到列表中
                    circleLineReplaceList.add(circleLine);
                }
                // 将替换后的循环行列表拼接成字符串
                String circleLineInfo = String.join("\n", circleLineReplaceList);
                // 替换模板中的循环行
                exportSql = exportSql.replaceAll(ReUtil.escape(circleLineTpl), ReUtil.escape(circleLineInfo));
            }
        }

        // 返回填充后的 DDL SQL 语句
        return exportSql;
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
            log.error("sql模板查询失败，未找到匹配的模板:[{}]", tplFileNameKey);
        }
        // 返回模板文件名
        return tplFileName;
    }
}

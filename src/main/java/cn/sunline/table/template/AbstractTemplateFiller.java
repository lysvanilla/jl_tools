package cn.sunline.table.template;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.util.ReUtil;
import cn.sunline.table.ExcelTableStructureReader;
import cn.sunline.util.BasicInfo;
import cn.sunline.util.GetTemplateInfo;
import cn.sunline.vo.TableFieldInfo;
import cn.sunline.vo.TableStructure;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 模板填充器抽象基类，提供通用的模板处理功能
 */
@Slf4j
public abstract class AbstractTemplateFiller implements TemplateFiller {

    // 模板缓存，避免重复读取相同的模板文件
    protected static final ConcurrentHashMap<String, String> TEMPLATE_CACHE = new ConcurrentHashMap<>();

    // 模板缓存，避免重复获取循环行
    protected static final ConcurrentHashMap<String, List<String>> CIRCLE_LINE_CACHE = new ConcurrentHashMap<>();

    /**
     * 获取模板路径
     * @param tableStructure 表结构信息
     * @return 模板文件路径
     */
    protected abstract String getTemplatePath(TableStructure tableStructure);

    /**
     * 处理特定字段
     * @param tableStructure 表结构信息
     * @param sql SQL构建器
     */
    protected abstract void processSpecificFields(TableStructure tableStructure, StringBuilder sql);

    /**
     * 获取输出文件路径
     * @param tableStructure 表结构信息
     * @return 输出文件路径
     */
    @Override
    public abstract String getOutputPath(TableStructure tableStructure);

    /**
     * 填充模板的主方法
     * @param tableStructure 表结构信息
     * @return 填充后的SQL语句
     */
    @Override
    public String fillTemplate(TableStructure tableStructure) {
        try {
            // 验证表结构对象
            if (tableStructure == null) {
                log.error("表结构对象为空，无法生成SQL语句");
                return "";
            }

            // 验证表名
            String tableNameEn = tableStructure.getTableNameEn();
            if (StringUtils.isBlank(tableNameEn)) {
                log.error("表英文名为空，无法生成SQL语句");
                return "";
            }

            // 使用MDC记录上下文信息
            MDC.put("table", tableNameEn);
            MDC.put("operation", "fillTemplate");

            // 获取模板路径
            String templatePath = getTemplatePath(tableStructure);
            if (StringUtils.isBlank(templatePath)) {
                log.error("未找到合适的SQL模板，无法生成SQL语句");
                return "";
            }

            // 读取模板内容
            String templateContent = readTemplate(templatePath);
            if (StringUtils.isBlank(templateContent)) {
                log.error("模板文件内容为空，无法生成SQL语句");
                return "";
            }

            // 创建SQL构建器
            StringBuilder sql = new StringBuilder(templateContent);

            try {
                // 替换通用占位符
                replaceCommonPlaceholders(tableStructure, sql);

                // 处理特定字段
                processSpecificFields(tableStructure, sql);

                // 处理循环行
                processCircleLines(tableStructure, templatePath, sql);
            } catch (Exception e) {
                log.error("处理模板时发生错误: {}", e.getMessage(), e);
                // 返回原始模板内容，而不是空字符串，以便于调试
                return "-- 错误: 处理模板时发生异常\n-- " + e.getMessage() + "\n\n" + templateContent;
            }

            return sql.toString();
        } catch (Exception e) {
            log.error("填充模板时发生错误: {}", e.getMessage(), e);
            return "-- 错误: 填充模板时发生异常\n-- " + e.getMessage();
        } finally {
            // 清理MDC上下文
            MDC.remove("table");
            MDC.remove("operation");
        }
    }

    /**
     * 读取模板文件内容，使用缓存避免重复读取
     * @param templatePath 模板文件路径
     * @return 模板文件内容
     */
    protected String readTemplate(String templatePath) {
        return TEMPLATE_CACHE.computeIfAbsent(templatePath, path -> {
            try {
                log.debug("读取模板文件: {}", path);
                return new FileReader(path).readString();
            } catch (Exception e) {
                log.error("读取模板文件 [{}] 失败: {}", path, e.getMessage(), e);
                return "";
            }
        });
    }

    /**
     * 获取模板文件中的循环行，使用缓存避免重复获取
     * @param templatePath 模板文件路径
     * @return 循环行列表
     */
    protected List<String> getCircleLines(String templatePath) {
        return CIRCLE_LINE_CACHE.computeIfAbsent(templatePath, path -> {
            try {
                log.debug("获取模板文件循环行: {}", path);
                return GetTemplateInfo.getCircleLine(path);
            } catch (Exception e) {
                log.error("获取模板文件 [{}] 的循环行失败: {}", path, e.getMessage(), e);
                return new ArrayList<>();
            }
        });
    }

    /**
     * 替换通用占位符
     * @param tableStructure 表结构信息
     * @param sql SQL构建器
     */
    protected void replaceCommonPlaceholders(TableStructure tableStructure, StringBuilder sql) {
        // 获取表信息
        String tableNameEn = tableStructure.getTableNameEn();
        String tableNameEnLower = StringUtils.lowerCase(tableNameEn);
        String tableNameCn = tableStructure.getTableNameCn();
        String systemModule = tableStructure.getSystemModule();
        String designer = StringUtils.defaultString(tableStructure.getDesigner(), "");
        String onlineTime = StringUtils.defaultString(tableStructure.getOnlineTime(), "");
        String sourceTableNameEn = tableStructure.getSourceTableNameEn();
        String sourceTableNameEnLower = StringUtils.defaultString(StringUtils.lowerCase(sourceTableNameEn), "");

        // 获取表所属的schema
        String tableSchema = BasicInfo.TEMPLATE_SETTING.get(systemModule);
        if (StringUtils.isBlank(tableSchema)) {
            log.warn("[{}-{}]的归属层次[{}]未在配置对应的schema", tableNameEn, tableNameCn, systemModule);
            tableSchema = "未配置";
        }

        // 替换通用占位符
        String content = sql.toString();
        content = content.replace("${table_name_en}", tableNameEn)
                .replace("${table_name_en_lower}", tableNameEnLower)
                .replace("${table_name_cn}", tableNameCn)
                .replace("${table_schema}", tableSchema)
                .replace("${mapping_analyst}", designer)
                .replace("${create_time}", onlineTime)
                .replace("${src_table_name_en_lower}", sourceTableNameEnLower);

        // 更新SQL构建器
        sql.setLength(0);
        sql.append(content);
    }

    /**
     * 处理循环行
     * @param tableStructure 表结构信息
     * @param templatePath 模板文件路径
     * @param sql SQL构建器
     */
    protected void processCircleLines(TableStructure tableStructure, String templatePath, StringBuilder sql) {
        // 获取循环行
        List<String> circleLineList = getCircleLines(templatePath);

        // 遍历循环行
        for (String circleLineTpl : circleLineList) {
            // 检查循环行是否包含字段相关占位符
            if (circleLineTpl.contains("column_name_en}") || circleLineTpl.contains("column_type}")
                    || circleLineTpl.contains("column_name_cn}") || circleLineTpl.contains("column_default}")) {

                // 处理字段循环行
                processFieldCircleLine(tableStructure, circleLineTpl, sql);
            }
        }
    }

    /**
     * 处理字段循环行
     * @param tableStructure 表结构信息
     * @param circleLineTpl 循环行模板
     * @param sql SQL构建器
     */
    protected void processFieldCircleLine(TableStructure tableStructure, String circleLineTpl, StringBuilder sql) {
        // 存储替换后的循环行
        List<String> circleLineReplaceList = new ArrayList<>();

        // 遍历字段
        for (TableFieldInfo field : tableStructure.getFieldMap().values()) {
            // 获取字段信息
            String fieldNameEn = StringUtils.lowerCase(field.getFieldNameEn());
            String fieldNameCn = StringUtils.lowerCase(field.getFieldNameCn());
            String fieldType = StringUtils.lowerCase(field.getFieldType());
            String notNull = StringUtils.lowerCase(field.getNotNull());
            String ifNull = "Y".equals(notNull) ? "not null" : "default null";
            String sourceFieldNameEn = StringUtils.defaultString(StringUtils.lowerCase(field.getSourceFieldNameEn()), fieldNameEn);

            // 复制循环行模板
            String circleLine = circleLineTpl;

            // 替换字段占位符
            circleLine = circleLine.replaceAll("\\@\\{column_name_en}", ReUtil.escape(StringUtils.defaultString(fieldNameEn, "")))
                    .replaceAll("\\@\\{column_name_cn}", ReUtil.escape(StringUtils.defaultString(fieldNameCn, "")))
                    .replaceAll("\\@\\{src_column_name_en}", ReUtil.escape(StringUtils.defaultString(sourceFieldNameEn, "")))
                    .replaceAll("\\@\\{column_type}", ReUtil.escape(StringUtils.defaultString(fieldType, "")))
                    .replaceAll("\\@\\{if_null}", ReUtil.escape(StringUtils.defaultString(ifNull, "")));

            // 添加到替换列表
            circleLineReplaceList.add(circleLine);
        }

        // 将替换后的循环行拼接成字符串
        String circleLineInfo = String.join("\n", circleLineReplaceList);
        circleLineInfo = GetTemplateInfo.removeFirstOccurence(circleLineInfo, ',');

        // 替换模板中的循环行
        String content = sql.toString();
        content = content.replaceAll(ReUtil.escape(circleLineTpl), ReUtil.escape(circleLineInfo));

        // 更新SQL构建器
        sql.setLength(0);
        sql.append(content);
    }

    /**
     * 获取主键字符串
     * @param tableStructure 表结构信息
     * @return 主键字符串
     */
    protected String getPrimaryKeyString(TableStructure tableStructure) {
        List<String> primaryKeys = new ArrayList<>();
        for (TableFieldInfo field : tableStructure.getFields()) {
            if ("Y".equals(field.getPrimaryKey())) {
                primaryKeys.add(field.getFieldNameEn());
            }
        }
        return String.join(",", primaryKeys);
    }

    /**
     * 获取分桶键字符串
     * @param tableStructure 表结构信息
     * @return 分桶键字符串
     */
    protected String getBucketKeyString(TableStructure tableStructure) {
        List<String> bucketKeys = new ArrayList<>();
        for (TableFieldInfo field : tableStructure.getFields()) {
            if ("Y".equals(field.getBucketKey())) {
                bucketKeys.add(field.getFieldNameEn());
            }
        }

        String bucketKeyStr = String.join(",", bucketKeys);
        if (StringUtils.isEmpty(bucketKeyStr)) {
            log.warn("表 [{}] 的分桶键为空", tableStructure.getTableNameEn());
        }

        return bucketKeyStr;
    }

    /**
     * 根据SQL类型和算法类型获取模板文件名
     * @param sqlType SQL类型
     * @param algorithmType 算法类型
     * @return 模板文件名
     */
    protected String getTplName(String sqlType, String algorithmType) {
        // 拼接模板文件名的键
        String tplFileNameKey = String.join("_", sqlType, StringUtils.lowerCase(algorithmType), "tpl");
        // 从模板设置中获取模板文件名
        String tplFileName = BasicInfo.TEMPLATE_SETTING.get(tplFileNameKey);
        // 检查模板文件名是否为空
        if (StringUtils.isEmpty(tplFileName)) {
            // 若为空，记录错误日志
            log.error("sql模板查询失败，未找到匹配的模板:[{}]", tplFileNameKey);
        }
        // 返回模板文件名
        return tplFileName;
    }

    /**
     * 确保目录存在，如果不存在则创建
     * @param filePath 文件路径
     */
    protected void ensureDirectoryExists(String filePath) {
        File file = new File(filePath);
        File directory = file.getParentFile();
        if (!directory.exists()) {
            if (directory.mkdirs()) {
                log.info("创建目录成功: [{}]", directory.getAbsolutePath());
            } else {
                log.warn("创建目录失败: [{}]", directory.getAbsolutePath());
            }
        }
    }

    /**
     * 处理单个表结构
     * @param tableStructure 表结构信息
     * @throws TemplateFillerException 模板填充异常
     */
    @Override
    public void processTable(TableStructure tableStructure) throws TemplateFillerException {
        String tableNameEn = tableStructure.getTableNameEn();
        String tableNameCn = tableStructure.getTableNameCn();

        try {
            // 使用MDC记录上下文信息
            MDC.put("table", tableNameEn);

            log.info("开始处理表 [{}]-[{}]", tableNameEn, tableNameCn);

            // 填充模板
            String filledSql = fillTemplate(tableStructure);
            if (StringUtils.isBlank(filledSql)) {
                throw new TemplateFillerException("生成表 " + tableNameEn + " 的SQL失败");
            }

            // 获取输出路径
            String outputPath = getOutputPath(tableStructure);

            // 确保输出目录存在
            ensureDirectoryExists(outputPath);

            // 写入文件
            FileUtil.writeString(filledSql, outputPath, StandardCharsets.UTF_8);

            log.info("表 [{}]-[{}] 的SQL生成成功，输出文件: [{}]", tableNameEn, tableNameCn, outputPath);
        } catch (Exception e) {
            log.error("处理表 [{}]-[{}] 时发生错误: {}", tableNameEn, tableNameCn, e.getMessage(), e);
            throw new TemplateFillerException("处理表 " + tableNameEn + " 时发生错误: " + e.getMessage(), e);
        } finally {
            // 清理MDC上下文
            MDC.remove("table");
        }
    }

    /**
     * 生成SQL
     * @param filePath 文件路径
     * @throws TemplateFillerException 模板填充异常
     */
    @Override
    public void generate(String filePath) throws TemplateFillerException {
        // 检查文件是否存在
        if (!FileUtil.exist(filePath)) {
            throw TemplateFillerException.fileNotFound(filePath);
        }

        try {
            // 使用MDC记录上下文信息
            MDC.put("operation", "generate");
            MDC.put("file", filePath);

            log.info("开始从文件 [{}] 读取表结构信息", filePath);

            // 读取表结构信息
            LinkedHashMap<String, TableStructure> tableMap = ExcelTableStructureReader.readExcel(filePath);
            if (tableMap.isEmpty()) {
                throw new TemplateFillerException("读取Excel文件失败，无法获取表结构信息");
            }

            log.info("成功读取 [{}] 个表的结构信息", tableMap.size());

            // 创建线程池
            int processors = Runtime.getRuntime().availableProcessors();
            ExecutorService executor = Executors.newFixedThreadPool(processors);

            // 并行处理表结构
            for (TableStructure tableStructure : tableMap.values()) {
                executor.submit(() -> {
                    try {
                        processTable(tableStructure);
                    } catch (Exception e) {
                        log.error("处理表 [{}] 时发生错误: {}", tableStructure.getTableNameEn(), e.getMessage(), e);
                    }
                });
            }

            // 关闭线程池并等待所有任务完成
            executor.shutdown();
            if (!executor.awaitTermination(30, TimeUnit.MINUTES)) {
                log.warn("等待任务完成超时");
                executor.shutdownNow();
            }

            log.info("所有表的SQL生成完成");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TemplateFillerException("生成SQL过程被中断", e);
        } catch (Exception e) {
            if (e instanceof TemplateFillerException) {
                throw (TemplateFillerException) e;
            }
            throw new TemplateFillerException("生成SQL过程中发生错误: " + e.getMessage(), e);
        } finally {
            // 清理MDC上下文
            MDC.remove("operation");
            MDC.remove("file");
        }
    }

    /**
     * 生成SQL
     * @param args 参数映射
     * @throws TemplateFillerException 模板填充异常
     */
    @Override
    public void generate(HashMap<String, String> args) throws TemplateFillerException {
        try {
            // 使用MDC记录上下文信息
            MDC.put("operation", "generate(HashMap)");

            // 记录参数信息
            log.info("开始生成SQL，参数: {}", args);

            // 从参数中获取文件路径
            String filePath = args.get("file_name");

            // 检查文件路径是否为空
            if (StringUtils.isBlank(filePath)) {
                throw new TemplateFillerException("缺少必要参数: file_name");
            }

            log.info("使用文件路径: {}", filePath);

            // 调用另一个generate方法
            generate(filePath);

            log.info("SQL生成完成");
        } catch (Exception e) {
            log.error("生成SQL时发生错误: {}", e.getMessage(), e);
            if (e instanceof TemplateFillerException) {
                throw (TemplateFillerException) e;
            }
            throw new TemplateFillerException("生成SQL过程中发生错误: " + e.getMessage(), e);
        } finally {
            // 清理MDC上下文
            MDC.remove("operation");
        }
    }
}

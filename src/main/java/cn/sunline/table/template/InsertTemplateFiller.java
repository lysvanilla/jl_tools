package cn.sunline.table.template;

import cn.hutool.core.io.FileUtil;
import cn.sunline.util.BasicInfo;
import cn.sunline.vo.TableStructure;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import java.io.File;
import java.util.HashMap;

/**
 * Insert模板填充器，用于生成Insert SQL语句
 */
@Slf4j
public class InsertTemplateFiller extends AbstractTemplateFiller {

    // 定义导出文件的基础路径
    private static final String BASE_EXPORT_PATH = BasicInfo.getBasicExportPath("autocode" + File.separator + "insert");
    private static final String BASE_EXPORT_DML_PATH = BasicInfo.getBasicExportPath("autocode" + File.separator + "dml_a");

    // 定义Insert模板路径
    private static final String INSERT_TPL_PATH = BasicInfo.TPL_PATH + "sql/ddl/insert_sql.sql";

    static {
        // 检查模板文件是否存在，如果不存在则记录警告日志
        if (!FileUtil.exist(INSERT_TPL_PATH)) {
            log.warn("Insert模板文件不存在: {}, 将尝试在运行时查找其他可用模板", INSERT_TPL_PATH);
        } else {
            log.info("成功加载Insert模板文件: {}", INSERT_TPL_PATH);
        }
    }

    /**
     * 获取模板路径
     * @param tableStructure 表结构信息
     * @return 模板文件路径
     */
    @Override
    protected String getTemplatePath(TableStructure tableStructure) {
        // 首先尝试使用默认的Insert模板路径
        if (FileUtil.exist(INSERT_TPL_PATH)) {
            return INSERT_TPL_PATH;
        }

        // 如果默认模板不存在，尝试使用其他可能的路径
        String[] possiblePaths = {
            BasicInfo.TPL_PATH + "sql/insert_sql.sql",
            BasicInfo.TPL_PATH + "insert_sql.sql",
            BasicInfo.TPL_PATH + "sql/ddl/insert.sql"
        };

        for (String path : possiblePaths) {
            if (FileUtil.exist(path)) {
                log.info("使用替代Insert模板文件: {}", path);
                return path;
            }
        }

        // 如果所有可能的路径都不存在，记录错误并返回空字符串
        log.error("无法找到可用的Insert模板文件");
        return "";
    }

    /**
     * 处理特定字段
     * @param tableStructure 表结构信息
     * @param sql SQL构建器
     */
    @Override
    protected void processSpecificFields(TableStructure tableStructure, StringBuilder sql) {
        // Insert模板不需要特殊处理，通用替换已经足够
    }

    /**
     * 获取输出文件路径
     * @param tableStructure 表结构信息
     * @return 输出文件路径
     */
    @Override
    public String getOutputPath(TableStructure tableStructure) {
        String tableNameEn = StringUtils.lowerCase(tableStructure.getTableNameEn());
        return BASE_EXPORT_PATH + "insert_" + tableNameEn + ".sql";
    }

    /**
     * 获取DML输出文件路径
     * @param tableStructure 表结构信息
     * @return DML输出文件路径
     */
    public String getDmlOutputPath(TableStructure tableStructure) {
        String tableNameEn = StringUtils.lowerCase(tableStructure.getTableNameEn());
        return BASE_EXPORT_DML_PATH + tableNameEn + ".sql";
    }

    /**
     * 生成Insert SQL语句
     * @param tableStructure 表结构信息
     * @throws TemplateFillerException 模板填充异常
     */
    public void genInsertSql(TableStructure tableStructure) throws TemplateFillerException {
        String tableNameEn = tableStructure.getTableNameEn();
        String tableNameCn = tableStructure.getTableNameCn();

        try {
            // 使用MDC记录上下文信息
            MDC.put("table", tableNameEn);
            MDC.put("operation", "genInsertSql");

            log.info("开始为表 [{}]-[{}] 生成Insert SQL", tableNameEn, tableNameCn);

            // 填充模板
            String insertSql = fillTemplate(tableStructure);
            if (StringUtils.isBlank(insertSql)) {
                throw new TemplateFillerException("生成表 " + tableNameEn + " 的Insert SQL失败");
            }

            // 获取输出路径
            String outputPath = getOutputPath(tableStructure);

            // 写入文件
            FileUtil.writeString(insertSql, outputPath, "UTF-8");

            log.info("表 [{}]-[{}] 的Insert SQL生成成功，输出文件: [{}]", tableNameEn, tableNameCn, outputPath);

            // 如果表名以'a'开头，还需要生成DML文件
            if (tableNameEn.startsWith("a")) {
                String dmlOutputPath = getDmlOutputPath(tableStructure);
                FileUtil.writeString(insertSql, dmlOutputPath, "UTF-8");
                log.info("表 [{}]-[{}] 的DML SQL生成成功，输出文件: [{}]", tableNameEn, tableNameCn, dmlOutputPath);
            }
        } catch (Exception e) {
            if (e instanceof TemplateFillerException) {
                throw (TemplateFillerException) e;
            }
            throw new TemplateFillerException("生成Insert SQL过程中发生错误: " + e.getMessage(), e);
        } finally {
            // 清理MDC上下文
            MDC.remove("table");
            MDC.remove("operation");
        }
    }
}

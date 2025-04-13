package cn.sunline.table.template;

import cn.hutool.core.io.FileUtil;
import cn.sunline.util.BasicInfo;
import cn.sunline.vo.TableStructure;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.HashMap;

/**
 * DDL模板填充器，用于生成DDL SQL语句
 */
@Slf4j
public class DdlTemplateFiller extends AbstractTemplateFiller {
    
    // 定义导出文件的基础路径
    private static final String BASE_EXPORT_PATH = BasicInfo.getBasicExportPath("autocode" + File.separator + "ddl");
    
    /**
     * 获取模板路径
     * @param tableStructure 表结构信息
     * @return 模板文件路径
     */
    @Override
    protected String getTemplatePath(TableStructure tableStructure) {
        // 获取模板文件名
        String tplFileName = getTplName("ddl", tableStructure.getAlgorithmType());
        if (StringUtils.isBlank(tplFileName)) {
            log.error("未找到合适的DDL模板，表: {}", tableStructure.getTableNameEn());
            return "";
        }
        
        // 拼接完整路径
        String fullPath = BasicInfo.TPL_PATH + tplFileName;
        if (!FileUtil.exist(fullPath)) {
            log.error("DDL模板文件不存在: {}", fullPath);
            return "";
        }
        
        return fullPath;
    }
    
    /**
     * 处理特定字段
     * @param tableStructure 表结构信息
     * @param sql SQL构建器
     */
    @Override
    protected void processSpecificFields(TableStructure tableStructure, StringBuilder sql) {
        // 获取主键和分桶键
        String primaryKeyStr = getPrimaryKeyString(tableStructure);
        String bucketKeyStr = getBucketKeyString(tableStructure);
        
        // 替换主键和分桶键占位符
        String content = sql.toString();
        content = content.replace("${primaryKey}", primaryKeyStr)
                .replace("${bucketKey}", bucketKeyStr);
        
        // 更新SQL构建器
        sql.setLength(0);
        sql.append(content);
    }
    
    /**
     * 获取输出文件路径
     * @param tableStructure 表结构信息
     * @return 输出文件路径
     */
    @Override
    public String getOutputPath(TableStructure tableStructure) {
        String tableNameEn = StringUtils.lowerCase(tableStructure.getTableNameEn());
        return BASE_EXPORT_PATH + "create_table_" + tableNameEn + ".sql";
    }
    
    /**
     * 生成DDL SQL语句
     * @param filePath Excel文件路径
     * @throws TemplateFillerException 模板填充异常
     */
    public void genDdlSql(String filePath) throws TemplateFillerException {
        generate(filePath);
    }
    
    /**
     * 重载的genDdlSql方法，接受一个包含参数的HashMap
     * @param argsMap 参数映射
     * @throws TemplateFillerException 模板填充异常
     */
    public void genDdlSql(HashMap<String, String> argsMap) throws TemplateFillerException {
        generate(argsMap);
    }
    
    /**
     * 程序入口方法，用于测试
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        try {
            // 使用工厂创建DdlTemplateFiller实例
            DdlTemplateFiller filler = TemplateFillerFactory.createDdlFiller();
            
            // 生成DDL SQL
            if (args.length > 0) {
                filler.genDdlSql(args[0]);
            } else {
                // 测试文件路径
                String filePath = "D:\\BaiduSyncdisk\\工作目录\\商机\\202503湖南银行指标管理平台\\业务表表结构.xlsx";
                filler.genDdlSql(filePath);
            }
        } catch (Exception e) {
            log.error("测试过程中发生错误: {}", e.getMessage(), e);
        }
    }
}

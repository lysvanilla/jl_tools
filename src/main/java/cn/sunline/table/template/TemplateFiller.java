package cn.sunline.table.template;

import cn.sunline.vo.TableStructure;

import java.util.HashMap;

/**
 * 模板填充器接口，定义模板填充器的基本行为
 */
public interface TemplateFiller {
    
    /**
     * 填充模板
     * @param tableStructure 表结构信息
     * @return 填充后的内容
     */
    String fillTemplate(TableStructure tableStructure);
    
    /**
     * 获取输出路径
     * @param tableStructure 表结构信息
     * @return 输出文件路径
     */
    String getOutputPath(TableStructure tableStructure);
    
    /**
     * 处理单个表结构
     * @param tableStructure 表结构信息
     * @throws TemplateFillerException 模板填充异常
     */
    void processTable(TableStructure tableStructure) throws TemplateFillerException;
    
    /**
     * 生成SQL
     * @param filePath 文件路径
     * @throws TemplateFillerException 模板填充异常
     */
    void generate(String filePath) throws TemplateFillerException;
    
    /**
     * 生成SQL
     * @param args 参数映射
     * @throws TemplateFillerException 模板填充异常
     */
    void generate(HashMap<String, String> args) throws TemplateFillerException;
}

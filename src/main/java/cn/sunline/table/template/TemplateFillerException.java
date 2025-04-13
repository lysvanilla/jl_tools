package cn.sunline.table.template;

/**
 * 模板填充异常类，用于处理模板填充过程中的异常
 */
public class TemplateFillerException extends Exception {
    
    /**
     * 创建一个新的模板填充异常
     * @param message 异常信息
     */
    public TemplateFillerException(String message) {
        super(message);
    }
    
    /**
     * 创建一个新的模板填充异常
     * @param message 异常信息
     * @param cause 原始异常
     */
    public TemplateFillerException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * 创建一个文件不存在异常
     * @param filePath 文件路径
     * @return 模板填充异常
     */
    public static TemplateFillerException fileNotFound(String filePath) {
        return new TemplateFillerException("文件不存在: " + filePath);
    }
    
    /**
     * 创建一个模板不存在异常
     * @param templatePath 模板路径
     * @return 模板填充异常
     */
    public static TemplateFillerException templateNotFound(String templatePath) {
        return new TemplateFillerException("未找到模板: " + templatePath);
    }
    
    /**
     * 创建一个模板读取异常
     * @param templatePath 模板路径
     * @param cause 原始异常
     * @return 模板填充异常
     */
    public static TemplateFillerException templateReadError(String templatePath, Throwable cause) {
        return new TemplateFillerException("读取模板失败: " + templatePath, cause);
    }
    
    /**
     * 创建一个表结构处理异常
     * @param tableName 表名
     * @param cause 原始异常
     * @return 模板填充异常
     */
    public static TemplateFillerException tableProcessingError(String tableName, Throwable cause) {
        return new TemplateFillerException("处理表 " + tableName + " 时发生错误", cause);
    }
}

package cn.sunline.constant;

/**
 * 应用常量类
 */
public final class AppConstants {
    private AppConstants() {
        // 私有构造函数防止实例化
    }
    
    // 配置相关常量
    public static final String CONFIG_APP_NAME = "app.name";
    public static final String CONFIG_APP_VERSION = "app.version";
    public static final String CONFIG_LOG_LEVEL = "log.level";
    public static final String CONFIG_LOG_PATH = "log.path";
    public static final String CONFIG_FILE_TEMPLATE_PATH = "file.template.path";
    public static final String CONFIG_FILE_CONFIG_PATH = "file.config.path";

    // 参数名常量
    public static final String PARAM_FUNCTION = "f";
    public static final String PARAM_FILE_NAME = "file_name";
    public static final String PARAM_MODEL_FILE_NAME = "model_file_name";
    
    // 文件相关常量
    public static final String FILE_ENCODING = "UTF-8";
    public static final String FILE_SEPARATOR = System.getProperty("file.separator");
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    
    // 错误消息常量
    public static final String ERROR_FUNCTION_EMPTY = "功能名称不能为空";
    public static final String ERROR_FILE_NAME_EMPTY = "文件名不能为空";
    public static final String ERROR_FUNCTION_NOT_SUPPORTED = "不支持的功能: ";
    public static final String ERROR_EXECUTION_FAILED = "功能执行失败: ";
} 
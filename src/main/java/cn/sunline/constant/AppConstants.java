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
    
    // 功能代码常量
    /*public static final String FUNCTION_WLH = "wlh";
    public static final String FUNCTION_DDL = "ddl";
    public static final String FUNCTION_DML = "dml";
    public static final String FUNCTION_GEN_MAPP = "gen_mapp";
    public static final String FUNCTION_GEN_TABLE = "gen_table";
    public static final String FUNCTION_SUPP_MAPP = "supp_mapp";
    public static final String FUNCTION_UPDATE_MAPP = "update_mapp";
    public static final String FUNCTION_GET_RELA_TAB = "get_rela_tab";
    public static final String FUNCTION_ZB = "zb";
    public static final String FUNCTION_CF = "cf";
    public static final String FUNCTION_HB = "hb";*/
    
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
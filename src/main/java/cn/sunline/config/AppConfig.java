package cn.sunline.config;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 应用配置管理类
 */
@Slf4j
public class AppConfig {
    private static final Properties properties = new Properties();
    private static final String CONFIG_FILE = "application.properties";
    
    static {
        loadConfig();
    }
    
    private static void loadConfig() {
        try (InputStream input = AppConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                log.warn("未找到配置文件: {}", CONFIG_FILE);
                return;
            }
            properties.load(input);
            log.info("成功加载配置文件: {}", CONFIG_FILE);
        } catch (IOException e) {
            log.error("加载配置文件失败: {}", CONFIG_FILE, e);
        }
    }
    
    /**
     * 获取配置项
     *
     * @param key 配置键
     * @return 配置值
     */
    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    /**
     * 获取配置项，如果不存在则返回默认值
     *
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    /**
     * 获取整数配置项
     *
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    public static int getIntProperty(String key, int defaultValue) {
        String value = properties.getProperty(key);
        try {
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            log.warn("配置项{}的值{}不是有效的整数，使用默认值{}", key, value, defaultValue);
            return defaultValue;
        }
    }
    
    /**
     * 获取布尔配置项
     *
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    public static boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }
} 
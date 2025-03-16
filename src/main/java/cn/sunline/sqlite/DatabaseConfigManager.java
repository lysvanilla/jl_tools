package cn.sunline.sqlite;

import cn.hutool.db.GlobalDbConfig;
import cn.sunline.util.BasicInfo;

// 单例配置管理类
public class DatabaseConfigManager {
    private static DatabaseConfigManager instance;

    private DatabaseConfigManager() {
        // 设置全局数据库配置文件路径
        GlobalDbConfig.setDbSettingPath(BasicInfo.dbConfigFile);
    }

    public static DatabaseConfigManager getInstance() {
        if (instance == null) {
            synchronized (DatabaseConfigManager.class) {
                if (instance == null) {
                    instance = new DatabaseConfigManager();
                }
            }
        }
        return instance;
    }
}
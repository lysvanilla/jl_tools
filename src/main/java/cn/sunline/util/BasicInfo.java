package cn.sunline.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import cn.hutool.setting.Setting;
import cn.idev.excel.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * BasicInfo 类提供了一些基础信息和工具方法，用于获取文件路径、处理时间间隔、读取配置参数以及检查目录是否为空等操作。
 * 这些方法主要依赖于 Hutool 工具库，方便在项目中进行文件操作和时间处理。
 */
@Slf4j
public class BasicInfo {
    // 文件分隔符，根据不同操作系统自动适配
    private static final String SEPARATOR = File.separator;
    // 当前工作目录路径
    private static final String WORKSPACE_PATH = System.getProperty("user.dir");
    // 配置文件所在的基础路径
    public static final String BASE_PATH = WORKSPACE_PATH + SEPARATOR + "config" + SEPARATOR;
    // 模板文件所在的路径
    public static final String TPL_PATH = WORKSPACE_PATH + SEPARATOR + "template" + SEPARATOR;
    // 帮助文件的内容，从指定路径的文件中读取
    public static final String HELP_FILE = new FileReader(BASE_PATH + "help.txt").readString();
    // 全局配置文件所在的路径
    public static final String GLOBAL_CONFIG_PATH = WORKSPACE_PATH + "/config/";
    // 当前日期，格式为 YYYYMMdd
    public static final String CURRENT_DATE = DateUtil.format(DateUtil.date(), "YYYYMMdd");
    // 用于区分的后缀，格式为 MMdd_HHmmss
    public static final String DIST_SUFFIX = DateUtil.format(DateUtil.date(), "MMdd_HHmmss");
    // 模板配置文件的设置对象
    public static final Setting TEMPLATE_SETTING = new Setting(GLOBAL_CONFIG_PATH + "template_config.txt");

    /**
     * 程序入口方法，用于测试获取基础导出路径的功能。
     *
     * @param args 命令行参数，此处未使用
     */
    public static void main(String[] args) {
        // 打印获取到的基础导出路径
        System.out.println(getBasicExportPath(""));
    }

    /**
     * 获取基础导出路径，默认处理文件标识为 "risk"。
     *
     * @param subpath 子路径，可为空
     * @return 基础导出路径
     */
    public static String getBasicExportPath(String subpath) {
        return getBasicExportPath("risk", subpath);
    }

    /**
     * 根据处理文件标识和子路径获取基础导出路径。
     * 路径格式为：配置文件中的输出基础路径 + 处理文件标识 + 处理时间 + 子路径。
     *
     * @param deal_file_sign 处理文件标识
     * @param subpath        子路径，可为空
     * @return 基础导出路径
     */
    public static String getBasicExportPath(String deal_file_sign, String subpath) {
        // 获取当前日期的前 8 位作为处理时间
        String deal_time = DateUtil.format(DateUtil.date(), "YYYYMMdd_HHmmss").substring(0, 8);
        // 获取当前日期时间作为日志时间
        String log_time = DateUtil.format(DateUtil.date(), "YYYYMMdd_HHmmss");

        // 读取配置文件中的输出基础路径
        Setting setting = new Setting(WORKSPACE_PATH + "/config/config.txt");
        String out_base_path = setting.getStr("out_base_path") + SEPARATOR;

        // 拼接处理文件标识和处理时间到输出基础路径
        out_base_path = out_base_path + deal_file_sign + "_" + deal_time + SEPARATOR;
        String export_file_path = out_base_path;

        // 如果子路径不为空，拼接子路径到导出路径
        if (!StringUtils.isEmpty(subpath)) {
            export_file_path = out_base_path + subpath + SEPARATOR;
        }

        // 创建导出路径对应的目录
        FileUtil.mkdir(export_file_path);
        // 记录导出路径信息
        log.info("生成的导出路径为: {}", export_file_path);
        return export_file_path;
    }

    /**
     * 计算两个时间点之间的时间间隔，并以 "X天 X小时 X分钟 X秒" 的格式返回。
     *
     * @param begin_time 开始时间
     * @param end_time   结束时间
     * @return 时间间隔的字符串表示
     */
    public static String getTimeInterval(LocalDateTime begin_time, LocalDateTime end_time) {
        // 计算两个时间点之间的时长
        Duration duration = Duration.between(begin_time, end_time);
        // 提取天数
        long days = duration.toDays();
        // 提取剩余的小时数
        long hours = duration.minusDays(days).toHours();
        // 提取剩余的分钟数
        long minutes = duration.minusHours(hours).toMinutes();
        // 提取剩余的秒数
        long seconds = duration.minusHours(hours).minusMinutes(minutes).getSeconds();

        // 拼接时间间隔字符串
        String timeInterval = days + "天 " + hours + "小时 " + minutes + "分钟 " + seconds + "秒";
        // 记录时间间隔信息
        log.info("时间间隔为: {}", timeInterval);
        return timeInterval;
    }

    /**
     * 从配置文件中获取指定参数的值。
     *
     * @param para_name 参数名称
     * @return 参数的值，如果未找到则返回 null
     */
    public static String getBasicPara(String para_name) {
        // 读取配置文件
        Setting setting = new Setting(WORKSPACE_PATH + "/config/config.txt");
        // 获取指定参数的值
        String value = setting.getStr(para_name);
        // 记录获取的参数信息
        log.info("获取到的参数 {} 的值为: {}", para_name, value);
        return value;
    }

    /**
     * 检查指定目录是否为空。
     *
     * @param directoryPath 目录路径
     * @return 如果目录存在且为空返回 true，否则返回 false
     */
    public static boolean isDirectoryEmpty(String directoryPath) {
        // 创建目录的 Path 对象
        Path path = Paths.get(directoryPath);
        try {
            // 检查目录是否存在且为有效的目录
            if (Files.exists(path) && Files.isDirectory(path)) {
                // 记录开始检查目录的信息
                log.debug("开始检查目录 {} 是否为空", directoryPath);
                // 判断目录是否为空
                boolean isEmpty = !Files.list(path).findFirst().isPresent();
                if (isEmpty) {
                    // 记录目录为空的信息
                    log.info("目录 {} 为空", directoryPath);
                } else {
                    // 记录目录不为空的信息
                    log.info("目录 {} 不为空", directoryPath);
                }
                return isEmpty;
            } else {
                // 记录路径不存在或不是有效目录的错误信息
                log.error("路径 {} 不存在或不是一个有效的目录", directoryPath);
            }
        } catch (IOException e) {
            // 记录检查目录时发生 I/O 错误的信息
            log.error("检查目录 {} 时发生 I/O 错误", directoryPath, e);
        }
        return false;
    }
}
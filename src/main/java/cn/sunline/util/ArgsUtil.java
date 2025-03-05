package cn.sunline.util;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * ArgsUtil 类主要负责将命令行参数解析为键值对的形式，
 * 支持对参数列表和参数数组进行解析，最终将解析结果存储在 HashMap 中。
 */
@Slf4j
public class ArgsUtil {

    /**
     * 将参数列表解析为键值对的 HashMap。
     * 会对列表中的每个参数去除引号，查找等号进行分割，
     * 若参数中包含有效等号，则将等号前后部分分别作为键和值存入 HashMap。
     *
     * @param arg_list 包含命令行参数的列表
     * @return 包含解析后键值对的 HashMap，若列表为空则返回空的 HashMap
     */
    public static HashMap<String, String> parseArgs(List<String> arg_list) {
        // 初始化用于存储解析结果的 HashMap
        HashMap<String, String> args_map = new HashMap<>();
        // 检查参数列表是否为空
        if (arg_list == null || arg_list.isEmpty()) {
            // 若为空，记录日志并返回空的 HashMap
            log.info("传入的参数列表为空，返回空的 HashMap");
            return args_map;
        }
        // 使用流式方式遍历参数列表
        arg_list.forEach(arg -> {
            try {
                // 去除参数中的引号
                String processedArg = arg.replace("\"", "").replace("'", "");
                // 查找等号的位置
                int idx = processedArg.indexOf("=");
                if (idx > 0) {
                    // 提取等号前的部分作为键
                    String key = processedArg.substring(0, idx);
                    // 提取等号后的部分作为值
                    String value = processedArg.substring(idx + 1);
                    // 将键值对存入 HashMap
                    args_map.put(key, value);
                    // 记录调试日志，显示添加的键值对
                    log.debug("添加键值对: {} -> {}", key, value);
                } else {
                    // 若参数中不包含有效等号，记录警告日志
                    log.warn("参数 {} 不包含有效的键值对分隔符 '='，跳过该参数", arg);
                }
            } catch (Exception e) {
                // 若解析过程中出现异常，记录错误日志
                log.error("解析参数 {} 时出现异常", arg, e);
            }
        });
        return args_map;
    }

    /**
     * 将参数数组解析为键值对的 HashMap。
     * 先把参数数组转换为列表，再调用 parseArgs(List<String> arg_list) 方法进行解析。
     *
     * @param arg_arr 包含命令行参数的数组
     * @return 包含解析后键值对的 HashMap，若数组为空则返回空的 HashMap
     */
    public static HashMap<String, String> parseArgs(String[] arg_arr) {
        // 检查参数数组是否为空
        if (arg_arr == null || arg_arr.length == 0) {
            // 若为空，记录日志并返回空的 HashMap
            log.info("传入的参数数组为空，返回空的 HashMap");
            return new HashMap<>();
        }
        // 将参数数组转换为列表
        List<String> arg_list = Arrays.asList(arg_arr);
        // 调用另一个 parseArgs 方法进行解析
        return parseArgs(arg_list);
    }
}
package cn.sunline;

import cn.idev.excel.FastExcel;
import cn.sunline.vo.LexiconInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * LexiconInfoReader 类用于从 Excel 文件中读取词库信息，并将其转换为中文名称（包括同义词）到英文缩写的映射。
 * 该类提供了读取 Excel 文件、将词库信息列表转换为映射等功能，同时会记录详细的日志信息，方便调试和监控。
 */
@Slf4j
public class LexiconInfoReader {

    /**
     * 程序的入口方法，用于测试从 Excel 文件读取词库信息并转换为映射的功能。
     *
     * @param args 命令行参数，此处未使用
     */
    public static void main(String[] args) {
        // 定义要读取的 Excel 文件路径
        String filePath = "C:\\Users\\lysva\\Desktop\\物理化工具-公司1.xlsx";
        try {
            // 记录开始从文件读取词库信息并转换为映射的日志
            log.info("开始从文件 [{}] 读取词库信息并转换为映射", filePath);
            // 调用 convertListToMap 方法进行转换
            HashMap<String, String> resultMap = convertListToMap(filePath);
            // 记录成功完成转换并尝试获取特定中文名称映射结果的日志
            log.info("成功完成转换，尝试获取 '实现' 的映射结果");
            // 从映射中获取 '实现' 的英文缩写
            String mappingResult = resultMap.get("实现");
            // 记录 '实现' 的映射结果日志
            log.info("'实现' 的映射结果为: {}", mappingResult);
            // 输出 '实现' 的映射结果到控制台
            System.out.println(mappingResult);
            System.out.println("aa");
        } catch (Exception e) {
            // 捕获并记录处理文件过程中出现的异常信息
            log.error("在处理文件 [{}] 时出现异常", filePath, e);
        }
    }

    /**
     * 从指定路径的 Excel 文件中读取词库信息。
     *
     * @param filePath Excel 文件的路径
     * @return 词库信息列表，如果读取失败则返回 null
     */
    public static List<LexiconInfo> readExcel(String filePath) {
        // 检查文件路径是否为空或空白
        if (StringUtils.isBlank(filePath)) {
            // 若为空，记录错误日志并返回 null
            log.error("传入的文件路径为空，无法读取 Excel 文件");
            return null;
        }
        // 创建文件对象
        File file = new File(filePath);
        // 检查文件是否存在
        if (!file.exists()) {
            // 若不存在，记录错误日志并返回 null
            log.error("指定的文件 [{}] 不存在", filePath);
            return null;
        }
        try {
            // 记录开始从文件的 '词库' 工作表读取数据的日志
            log.info("开始从文件 [{}] 的 '词库' 工作表读取数据", filePath);
            // 使用 FastExcel 读取文件中的词库信息
            List<LexiconInfo> lexiconInfoList = FastExcel.read(file)
                    .sheet("词库")
                    .head(LexiconInfo.class)
                    .doReadSync();
            // 记录成功读取到的词库信息数量
            log.info("成功从文件 [{}] 读取到 {} 条词库信息", filePath, lexiconInfoList.size());
            return lexiconInfoList;
        } catch (Exception e) {
            // 捕获并记录读取文件时出现的异常信息
            log.error("读取文件 [{}] 时出现异常", filePath, e);
            return null;
        }
    }

    /**
     * 根据文件路径读取 Excel 文件，并将其中的词库信息转换为映射。
     *
     * @param filePath Excel 文件的路径
     * @return 词库信息的映射，如果读取或转换失败则返回空的 HashMap
     */
    public static HashMap<String, String> convertListToMap(String filePath) {
        // 调用 readExcel 方法读取词库信息列表
        List<LexiconInfo> lexiconInfoList = readExcel(filePath);
        // 检查读取结果是否为 null
        if (lexiconInfoList == null) {
            // 若为 null，记录错误日志并返回空的 HashMap
            log.error("未能从文件 [{}] 中读取到词库信息，无法进行转换", filePath);
            return new HashMap<>();
        }
        // 调用另一个 convertListToMap 方法将词库信息列表转换为映射
        return convertListToMap(lexiconInfoList);
    }

    /**
     * 将词库信息列表转换为映射。
     *
     * @param lexiconInfoList 词库信息列表
     * @return 词库信息的映射
     */
    public static HashMap<String, String> convertListToMap(List<LexiconInfo> lexiconInfoList) {
        // 检查词库信息列表是否为空或 null
        if (lexiconInfoList == null || lexiconInfoList.isEmpty()) {
            // 若为空，记录警告日志并返回空的 HashMap
            log.warn("传入的词库信息列表为空，返回空的映射");
            return new HashMap<>();
        }
        // 记录开始将词库信息列表转换为映射的日志
        log.info("开始将 {} 条词库信息转换为映射", lexiconInfoList.size());
        // 初始化用于存储映射结果的 HashMap
        HashMap<String, String> resultMap = new HashMap<>();
        // 遍历词库信息列表
        for (LexiconInfo info : lexiconInfoList) {
            // 获取英文缩写
            String englishAbbreviation = info.getEnglishAbbreviation();
            // 获取中文名称
            String chineseName = info.getChineseName();
            // 检查英文缩写是否为空或空白
            if (StringUtils.isBlank(englishAbbreviation)) {
                // 若为空，记录调试日志并跳过该条记录
                log.debug("词库信息中英文缩写为空，跳过该条记录: {}", chineseName);
                continue;
            }
            // 检查中文名称是否不为空或空白
            if (StringUtils.isNotBlank(chineseName)) {
                // 若不为空，将中文名称和英文缩写添加到映射中
                resultMap.put(chineseName, englishAbbreviation);
                // 记录添加映射的调试日志
                log.debug("添加映射: 中文名称 [{}] -> 英文缩写 [{}]", chineseName, englishAbbreviation);
            }
            // 获取同义词
            String synonyms = info.getSynonyms();
            // 检查同义词是否不为空或空白
            if (StringUtils.isNotBlank(synonyms)) {
                // 将同义词按 '|' 分割成列表
                List<String> synonymList = Arrays.asList(synonyms.split("\\|"));
                // 遍历同义词列表
                for (String synonym : synonymList) {
                    // 检查同义词是否不为空或空白
                    if (StringUtils.isNotBlank(synonym)) {
                        // 若不为空，将同义词和英文缩写添加到映射中
                        resultMap.put(synonym, englishAbbreviation);
                        // 记录添加同义词映射的调试日志
                        log.debug("添加同义词映射: 同义词 [{}] -> 英文缩写 [{}]", synonym, englishAbbreviation);
                    }
                }
            }
        }
        // 记录成功将词库信息转换为映射的日志，包含映射的数量
        log.info("成功将词库信息转换为包含 {} 个映射的结果", resultMap.size());
        return resultMap;
    }
}
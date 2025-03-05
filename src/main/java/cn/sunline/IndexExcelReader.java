package cn.sunline;

import cn.idev.excel.FastExcel;
import cn.sunline.vo.IndexInfo;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * IndexExcelReader 类用于从指定的 Excel 文件中读取指标信息，并对读取到的指标信息进行过滤。
 * 它包含了读取 Excel 文件和过滤指标信息列表的功能，同时会记录详细的日志信息。
 */
@Slf4j
public class IndexExcelReader {

    /**
     * 程序的入口方法，用于测试从 Excel 文件读取指标信息并进行过滤的功能。
     *
     * @param args 命令行参数，此处未使用
     */
    public static void main(String[] args) {
        // 定义要读取的 Excel 文件路径
        String filePath = "D:\\svn\\jilin\\02.需求分析\\0202.智能风控系统\\智能风控系统指标信息_20250304.xlsx";
        try {
            // 记录开始从文件读取指标信息的日志
            log.info("开始从文件 [{}] 读取指标信息", filePath);
            // 调用 readExcel 方法读取指标信息
            List<IndexInfo> indexInfoList = readExcel(filePath);
            if (indexInfoList != null) {
                // 若读取到指标信息，记录读取到的指标信息数量
                log.info("成功从文件 [{}] 读取到 [{}] 条指标信息", filePath, indexInfoList.size());
            }

            // 记录开始过滤指标信息的日志
            log.info("开始过滤指标信息，仅保留启用和未启用且设计分类为指标的数据");
            // 调用 filterIndexInfoList 方法过滤指标信息
            List<IndexInfo> filteredList = filterIndexInfoList(indexInfoList);
            if (filteredList != null) {
                // 若过滤后有符合条件的指标信息，记录筛选出的指标信息数量
                log.info("过滤完成，筛选出 [{}] 条符合条件的指标信息", filteredList.size());
            }
        } catch (Exception e) {
            // 捕获并记录处理文件过程中出现的异常信息
            log.error("在处理文件 [{}] 时出现异常", filePath, e);
        }
    }

    /**
     * 从指定路径的 Excel 文件中读取指标信息。
     *
     * @param filePath Excel 文件的路径
     * @return 指标信息列表，如果文件路径无效或读取过程中出现异常，返回空列表
     */
    public static List<IndexInfo> readExcel(String filePath) {
        // 检查文件路径是否为空
        if (filePath == null || filePath.isEmpty()) {
            // 若为空，记录错误日志并返回空列表
            log.error("传入的文件路径为空，无法读取 Excel 文件");
            return new ArrayList<>();
        }
        // 创建文件对象
        File file = new File(filePath);
        // 检查文件是否存在
        if (!file.exists()) {
            // 若不存在，记录错误日志并返回空列表
            log.error("指定的文件 [{}] 不存在", filePath);
            return new ArrayList<>();
        }
        try {
            // 记录开始从指定工作表读取数据的日志
            log.info("开始从文件 [{}] 的 '风控中台_指标清单' 工作表读取数据", filePath);
            // 使用 FastExcel 读取文件中的指标信息
            List<IndexInfo> indexInfoList = FastExcel.read(file)
                    .sheet("风控中台_指标清单")
                    .head(IndexInfo.class)
                    .doReadSync();
            // 记录成功读取到的指标信息数量
            log.info("成功从文件 [{}] 读取到 [{}] 条指标信息", filePath, indexInfoList.size());
            return indexInfoList;
        } catch (Exception e) {
            // 捕获并记录读取文件时出现的异常信息
            log.error("读取文件 [{}] 时出现异常，异常信息: {}", filePath, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 过滤指标信息列表，仅保留启用和未启用且设计分类为指标的数据。
     *
     * @param indexInfoList 原始指标信息列表
     * @return 过滤后的指标信息列表，如果原始列表为空，返回空列表
     */
    public static List<IndexInfo> filterIndexInfoList(List<IndexInfo> indexInfoList) {
        // 检查原始指标信息列表是否为空
        if (indexInfoList == null || indexInfoList.isEmpty()) {
            // 若为空，记录警告日志并返回空列表
            log.warn("传入的指标信息列表为空，无需过滤，直接返回空列表");
            return new ArrayList<>();
        }
        // 记录开始对指标信息进行过滤的日志
        log.info("开始对 [{}] 条指标信息进行过滤", indexInfoList.size());
        // 用于存储过滤后的指标信息
        List<IndexInfo> filteredList = new ArrayList<>();
        // 遍历原始指标信息列表
        for (IndexInfo info : indexInfoList) {
            // 检查指标是否启用状态为启用或未启用，且设计分类为指标
            if (("启用".equals(info.getIfEnabled()) || "未启用".equals(info.getIfEnabled()))
                    && "指标".equals(info.getDesignClassification())) {
                // 若符合条件，将该指标信息添加到过滤后的列表中
                filteredList.add(info);
            }
        }
        // 记录过滤完成后筛选出的指标信息数量
        log.info("过滤完成，从 [{}] 条指标信息中筛选出 [{}] 条符合条件的指标信息", indexInfoList.size(), filteredList.size());
        return filteredList;
    }
}
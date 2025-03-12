package cn.sunline.table;

import cn.idev.excel.FastExcel;
import cn.sunline.vo.TableFieldInfo;
import cn.sunline.vo.TableStructure;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * ExcelTableStructureReader 类用于从指定的 Excel 文件中读取表结构信息。
 * 它会读取 Excel 文件中 "表级信息" 和 "字段级信息" 工作表的数据，
 * 并将表信息和字段信息进行关联，最终返回一个包含表名和对应表结构的 LinkedHashMap。
 */
@Slf4j
public class ExcelTableStructureReader {

    /**
     * 程序的入口方法，用于测试从 Excel 文件读取表结构信息的功能。
     *
     * @param args 命令行参数，此处未使用
     */
    public static void main(String[] args) {
        // 定义要读取的 Excel 文件路径
        String filePath = "D:\\svn\\jilin\\03.模型设计\\风险数据集市物理模型-模板.xlsx";
        try {
            // 记录开始读取表结构信息的日志
            log.info("开始从文件 [{}] 读取表结构信息", filePath);
            // 调用 readExcel 方法读取表结构信息
            LinkedHashMap<String, TableStructure> tableMap = ExcelTableStructureReader.readExcel(filePath);
            if (tableMap != null && tableMap.containsKey("F_MKT_BOND_PRD_INFO")) {
                // 如果读取到的表结构信息中包含指定表名，打印该表的字段信息
                tableMap.get("F_MKT_BOND_PRD_INFO").getFields().forEach(System.out::println);
            } else {
                // 如果未找到指定表名的表结构信息，记录警告日志
                log.warn("未找到 F_MKT_BOND_PRD_INFO 表的结构信息");
            }
            // 记录表结构信息读取完成的日志
            log.info("表结构信息读取完成");
            System.out.println("1");
        } catch (Exception e) {
            // 捕获并记录读取文件过程中出现的异常信息
            log.error("在处理文件 [{}] 时出现异常", filePath, e);
        }
    }

    /**
     * 从指定的 Excel 文件中读取表结构信息。
     *
     * @param filePath Excel 文件的路径
     * @return 包含表名和对应表结构的 LinkedHashMap，如果读取失败则返回空的 LinkedHashMap
     */
    public static LinkedHashMap<String, TableStructure> readExcel(String filePath) {
        // 初始化用于存储表结构信息的 LinkedHashMap
        LinkedHashMap<String, TableStructure> tableMap = new LinkedHashMap<>();
        // 检查文件路径是否为空
        if (filePath == null || filePath.isEmpty()) {
            // 若为空，记录错误日志并返回空的 LinkedHashMap
            log.error("传入的文件路径为空，无法读取 Excel 文件");
            return tableMap;
        }
        // 创建文件对象
        File file = new File(filePath);
        // 检查文件是否存在且为有效的文件
        if (!file.exists() || !file.isFile()) {
            // 若不满足条件，记录错误日志并返回空的 LinkedHashMap
            log.error("指定的 Excel 文件不存在或不是一个有效的文件: {}", filePath);
            return tableMap;
        }

        try {
            // 记录开始从 "表级信息" 工作表读取表基本信息的日志
            log.debug("开始从文件 [{}] 的 '表级信息' 工作表读取表基本信息", filePath);
            // 读取表基本信息
            List<TableStructure> tableStructures = FastExcel.read(file)
                    .sheet("表级信息")
                    .head(TableStructure.class)
                    .doReadSync();
            // 若读取结果为空，将其初始化为空列表
            if (tableStructures == null) {
                tableStructures = new ArrayList<>();
            }
            // 记录成功读取到的表基本信息数量
            log.info("成功从 '表级信息' 工作表读取到 [{}] 条表基本信息", tableStructures.size());

            // 记录开始从 "字段级信息" 工作表读取字段信息的日志
            log.debug("开始从文件 [{}] 的 '字段级信息' 工作表读取字段信息", filePath);
            // 读取字段信息
            List<TableFieldInfo> tableFieldInfos = FastExcel.read(file)
                    .sheet("字段级信息")
                    .head(TableFieldInfo.class)
                    .doReadSync();
            // 若读取结果为空，将其初始化为空列表
            if (tableFieldInfos == null) {
                tableFieldInfos = new ArrayList<>();
            }
            // 记录成功读取到的字段信息数量
            log.info("成功从 '字段级信息' 工作表读取到 [{}] 条字段信息", tableFieldInfos.size());

            // 记录开始将表基本信息放入 Map 的日志
            log.debug("开始将表基本信息放入 Map");
            for (TableStructure table : tableStructures) {
                // 检查表的英文名称是否有效
                if (table.getTableNameEn() != null && !table.getTableNameEn().trim().isEmpty()) {
                    // 将表信息放入 Map
                    tableMap.put(table.getTableNameEn(), table);
                }
            }
            // 记录成功放入 Map 的表基本信息数量
            log.info("成功将 [{}] 条表基本信息放入 Map", tableMap.size());

            // 记录开始将字段信息添加到对应表结构中的日志
            log.debug("开始将字段信息添加到对应的表结构中");
            int unmatchedCount = 0;
            for (TableFieldInfo field : tableFieldInfos) {
                // 检查字段所属表的英文名称是否有效
                if (field.getTableNameEn() != null && !field.getTableNameEn().trim().isEmpty()) {
                    // 根据字段所属表名从 Map 中获取对应的表结构
                    TableStructure table = tableMap.get(field.getTableNameEn());
                    if (table != null) {
                        // 若找到对应的表结构，将字段信息添加到该表结构中
                        table.addField(field);
                    } else {
                        // 若未找到对应的表结构，记录警告日志并增加未匹配数量
                        log.warn("未找到对应的表结构: {}", field.getTableNameEn());
                        unmatchedCount++;
                    }
                }
            }
            // 记录字段信息添加完成的日志，包含未匹配到表结构的字段信息数量
            if (unmatchedCount>0){
                 log.warn("字段信息添加完成，共有 [{}] 条字段信息未找到对应的表结构", unmatchedCount);
            }

        } catch (Exception e) {
            // 捕获并记录读取文件过程中出现的异常信息
            log.error("读取文件 [{}] 时出现异常，异常信息: {}", filePath, e.getMessage(), e);
        }

        return tableMap;
    }
}
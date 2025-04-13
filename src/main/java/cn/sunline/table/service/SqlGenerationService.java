package cn.sunline.table.service;

import cn.hutool.core.io.FileUtil;
import cn.sunline.table.ExcelTableStructureReader;
import cn.sunline.table.template.DdlTemplateFiller;
import cn.sunline.table.template.InsertTemplateFiller;
import cn.sunline.table.template.TemplateFillerException;
import cn.sunline.table.template.TemplateFillerFactory;
import cn.sunline.vo.TableStructure;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * SQL生成服务类，提供生成DDL和Insert SQL的服务
 */
@Slf4j
public class SqlGenerationService {

    /**
     * 生成DDL和Insert SQL
     * @param filePath Excel文件路径
     * @throws TemplateFillerException 模板填充异常
     */
    public void generateSql(String filePath) throws TemplateFillerException {
        // 检查文件是否存在
        if (!FileUtil.exist(filePath)) {
            throw TemplateFillerException.fileNotFound(filePath);
        }

        try {
            // 使用MDC记录上下文信息
            MDC.put("operation", "generateSql");
            MDC.put("file", filePath);

            log.info("开始从文件 [{}] 读取表结构信息", filePath);

            // 读取表结构信息
            LinkedHashMap<String, TableStructure> tableMap = ExcelTableStructureReader.readExcel(filePath);
            if (tableMap.isEmpty()) {
                throw new TemplateFillerException("读取Excel文件失败，无法获取表结构信息");
            }

            log.info("成功读取 [{}] 个表的结构信息", tableMap.size());

            // 创建线程池
            int processors = Runtime.getRuntime().availableProcessors();
            ExecutorService executor = Executors.newFixedThreadPool(processors);

            // 创建模板填充器
            DdlTemplateFiller ddlFiller = TemplateFillerFactory.createDdlFiller();
            InsertTemplateFiller insertFiller = TemplateFillerFactory.createInsertFiller();

            // 并行处理表结构
            for (TableStructure tableStructure : tableMap.values()) {
                executor.submit(() -> {
                    try {
                        // 生成DDL SQL
                        processTable(tableStructure, ddlFiller, insertFiller);
                    } catch (Exception e) {
                        log.error("处理表 [{}] 时发生错误: {}", tableStructure.getTableNameEn(), e.getMessage(), e);
                    }
                });
            }

            // 关闭线程池并等待所有任务完成
            executor.shutdown();
            if (!executor.awaitTermination(30, TimeUnit.MINUTES)) {
                log.warn("等待任务完成超时");
                executor.shutdownNow();
            }

            log.info("所有表的SQL生成完成");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TemplateFillerException("生成SQL过程被中断", e);
        } catch (Exception e) {
            if (e instanceof TemplateFillerException) {
                throw (TemplateFillerException) e;
            }
            throw new TemplateFillerException("生成SQL过程中发生错误: " + e.getMessage(), e);
        } finally {
            // 清理MDC上下文
            MDC.remove("operation");
            MDC.remove("file");
        }
    }

    /**
     * 处理单个表结构
     * @param tableStructure 表结构信息
     * @param ddlFiller DDL模板填充器
     * @param insertFiller Insert模板填充器
     */
    private void processTable(TableStructure tableStructure, DdlTemplateFiller ddlFiller, InsertTemplateFiller insertFiller) {
        String tableNameEn = tableStructure.getTableNameEn();
        String tableNameCn = tableStructure.getTableNameCn();

        try {
            // 使用MDC记录上下文信息
            MDC.put("table", tableNameEn);

            log.info("开始处理表 [{}]-[{}]", tableNameEn, tableNameCn);

            // 填充DDL模板
            String ddlSql = ddlFiller.fillTemplate(tableStructure);
            if (StringUtils.isBlank(ddlSql)) {
                log.error("生成表 [{}]-[{}] 的DDL SQL失败", tableNameEn, tableNameCn);
                return;
            }

            // 获取DDL输出路径
            String ddlOutputPath = ddlFiller.getOutputPath(tableStructure);

            // 确保输出目录存在
            File ddlOutputFile = new File(ddlOutputPath);
            File ddlOutputDir = ddlOutputFile.getParentFile();
            if (!ddlOutputDir.exists()) {
                log.info("创建DDL输出目录: {}", ddlOutputDir.getAbsolutePath());
                if (!ddlOutputDir.mkdirs()) {
                    log.error("无法创建DDL输出目录: {}", ddlOutputDir.getAbsolutePath());
                    return;
                }
            }

            // 写入DDL文件
            FileUtil.writeString(ddlSql, ddlOutputPath, "UTF-8");

            log.info("表 [{}]-[{}] 的DDL SQL生成成功，输出文件: [{}]", tableNameEn, tableNameCn, ddlOutputPath);

            // 填充Insert模板
            String insertSql = insertFiller.fillTemplate(tableStructure);
            if (StringUtils.isBlank(insertSql)) {
                log.error("生成表 [{}]-[{}] 的Insert SQL失败", tableNameEn, tableNameCn);
                return;
            }

            // 获取Insert输出路径
            String insertOutputPath = insertFiller.getOutputPath(tableStructure);

            // 确保输出目录存在
            File insertOutputFile = new File(insertOutputPath);
            File insertOutputDir = insertOutputFile.getParentFile();
            if (!insertOutputDir.exists()) {
                log.info("创建Insert输出目录: {}", insertOutputDir.getAbsolutePath());
                if (!insertOutputDir.mkdirs()) {
                    log.error("无法创建Insert输出目录: {}", insertOutputDir.getAbsolutePath());
                    return;
                }
            }

            // 写入Insert文件
            FileUtil.writeString(insertSql, insertOutputPath, "UTF-8");

            log.info("表 [{}]-[{}] 的Insert SQL生成成功，输出文件: [{}]", tableNameEn, tableNameCn, insertOutputPath);

            // 如果表名以'a'开头，还需要生成DML文件
            if (tableNameEn.startsWith("a")) {
                String dmlOutputPath = insertFiller.getDmlOutputPath(tableStructure);

                // 确保输出目录存在
                File dmlOutputFile = new File(dmlOutputPath);
                File dmlOutputDir = dmlOutputFile.getParentFile();
                if (!dmlOutputDir.exists()) {
                    log.info("创建DML输出目录: {}", dmlOutputDir.getAbsolutePath());
                    if (!dmlOutputDir.mkdirs()) {
                        log.error("无法创建DML输出目录: {}", dmlOutputDir.getAbsolutePath());
                        return;
                    }
                }

                FileUtil.writeString(insertSql, dmlOutputPath, "UTF-8");
                log.info("表 [{}]-[{}] 的DML SQL生成成功，输出文件: [{}]", tableNameEn, tableNameCn, dmlOutputPath);
            }
        } catch (Exception e) {
            log.error("处理表 [{}]-[{}] 时发生错误: {}", tableNameEn, tableNameCn, e.getMessage(), e);
        } finally {
            // 清理MDC上下文
            MDC.remove("table");
        }
    }

    /**
     * 生成SQL，接受一个包含参数的HashMap
     * @param argsMap 参数映射
     * @throws TemplateFillerException 模板填充异常
     */
    public void generateSql(HashMap<String, String> argsMap) throws TemplateFillerException {
        try {
            // 使用MDC记录上下文信息
            MDC.put("operation", "generateSql(HashMap)");

            // 记录参数信息
            log.info("开始生成SQL，参数: {}", argsMap);

            // 从HashMap中获取文件路径
            String filePath = argsMap.get("file_name");

            // 检查文件路径是否为空
            if (StringUtils.isBlank(filePath)) {
                throw new TemplateFillerException("缺少必要参数: file_name");
            }

            log.info("使用文件路径: {}", filePath);

            // 调用另一个generateSql方法
            generateSql(filePath);

            log.info("SQL生成完成");
        } catch (Exception e) {
            log.error("生成SQL时发生错误: {}", e.getMessage(), e);
            if (e instanceof TemplateFillerException) {
                throw (TemplateFillerException) e;
            }
            throw new TemplateFillerException("生成SQL过程中发生错误: " + e.getMessage(), e);
        } finally {
            // 清理MDC上下文
            MDC.remove("operation");
        }
    }
}

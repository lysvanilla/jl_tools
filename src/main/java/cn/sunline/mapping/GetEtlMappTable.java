package cn.sunline.mapping;

import cn.hutool.core.io.FileUtil;
import cn.idev.excel.ExcelWriter;
import cn.idev.excel.FastExcel;
import cn.idev.excel.write.metadata.WriteSheet;
import cn.sunline.table.LexiconInfoReader;
import cn.sunline.util.BasicInfo;
import cn.sunline.vo.SplitWordsFailure;
import cn.sunline.vo.TableRelaInfo;
import cn.sunline.vo.TranslationResultFull;
import cn.sunline.vo.etl.EtlGroup;
import cn.sunline.vo.etl.EtlGroupJoinInfo;
import cn.sunline.vo.etl.EtlMapp;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.oracle.parser.OracleStatementParser;
import com.alibaba.druid.sql.dialect.oracle.visitor.OracleSchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static cn.sunline.mapping.EtlMappingExcelRead.readEtlMappExcel;

@Slf4j
public class GetEtlMappTable {

    private static final String tpl_path = BasicInfo.TPL_PATH+"excel"+File.separator+"模型加工表级依赖模板.xlsx";
    private static final String basicExportPath = BasicInfo.getBasicExportPath("");
    public static void main(String[] args) {
        Map<String, String> argsMap = new HashMap<>();
        argsMap.put("file_name","D:\\svn\\jilin\\04.映射设计\\0402.计量模型层\\");
        getEtlMappTableMain(argsMap);
    }

    public static void getEtlMappTableMain(Map<String, String> argsMap) {
        // 从 HashMap 中获取文件路径
        String filePath = argsMap.get("file_name");
        // 检查文件路径是否为空
        if (StringUtils.isBlank(filePath)) {
            // 若为空，记录错误日志
            log.error("argsMap中缺少file_name参数");
            return;
        }
        getEtlMappTableMain(filePath);
    }
    public static void getEtlMappTableMain(String filePath){
        List<TableRelaInfo> tableRelaInfos = new ArrayList<>();
        String outPutFileName = FileUtil.mainName(filePath);
        if (FileUtil.isDirectory(filePath)){
            outPutFileName = FileUtil.getName(filePath);
            for (File file : FileUtil.ls(filePath)) {
                String fileName = file.getName();
                if (fileName.endsWith(".xlsx") && !fileName.startsWith("~") && !fileName.endsWith("0_封面.xlsx")
                        && !fileName.endsWith("2_目录.xlsx") && !fileName.endsWith("1_变更记录.xlsx")){
                    tableRelaInfos.addAll(getEtlMappTable(file.getAbsolutePath()));
                }else{
                    log.debug("跳过文件: {}, 原因：文件名以 ~ 开头或不是 .xlsx 文件。", file.getAbsolutePath());
                    continue;
                }
            }
        }else{
            tableRelaInfos.addAll(getEtlMappTable(filePath));
        }
        String outputPath = basicExportPath+"模型加工表依赖_"+outPutFileName+"_"+BasicInfo.CURRENT_DATE+".xlsx";
        writeExcel(tableRelaInfos,outputPath);
        //System.out.println("1");
    }

    public static List<TableRelaInfo> getEtlMappTable(String filePath){
        List<EtlMapp> etlMappList = readEtlMappExcel(filePath);
        List<TableRelaInfo> tableRelaInfos = new ArrayList<>();
        for (EtlMapp etlMapp : etlMappList) {
            String tableEnglishName = etlMapp.getTableEnglishName();
            String tableChineseName = etlMapp.getTableChineseName();
            log.info("处理[{}]-[{}]-[{}]",tableChineseName,tableEnglishName,filePath);
            TableRelaInfo tableRelaInfo = new TableRelaInfo(tableEnglishName, tableChineseName);
            List<EtlGroup> etlGroupList = etlMapp.getEtlGroupList();
            for (EtlGroup etlGroup : etlGroupList) {
                List<EtlGroupJoinInfo> etlGroupJoinInfoList = etlGroup.getEtlGroupJoinInfoList();
                for (EtlGroupJoinInfo etlGroupJoinInfo : etlGroupJoinInfoList) {
                    String sourceTableEnglishName = etlGroupJoinInfo.getSourceTableEnglishName();
                    String sourceTableEnglishNameLower = StringUtils.lowerCase(sourceTableEnglishName);
                    if (sourceTableEnglishNameLower.contains("select")){
                        LinkedHashSet<String> srcTableList = getSqlSrcTable(sourceTableEnglishName);
                        tableRelaInfo.addRelatedTables(srcTableList);
                    }else{
                        if (sourceTableEnglishName.contains(" ")){
                            tableRelaInfo.addRelatedTable(sourceTableEnglishName.split(" ")[0]);
                        }else{
                            tableRelaInfo.addRelatedTable(sourceTableEnglishName);
                        }
                    }
                }
            }
            tableRelaInfos.add(tableRelaInfo);
        }
        return tableRelaInfos;
    }

    public static LinkedHashSet<String> getSqlSrcTable(String sql) {
        log.debug("待解析sql语句：[{}]",sql);
        //System.out.println("===============================================================\n\n"+sql);
        //System.out.println(sql);
        String content = extractContent(sql);
        //System.out.println(content);
        // 解析 SQL 语句
        OracleStatementParser parser = new OracleStatementParser(content);

        LinkedHashSet<String> srcTableList = new LinkedHashSet<>();

        try {
            SQLStatement statement = parser.parseStatement();
            // 创建统计访问器
            OracleSchemaStatVisitor visitor = new OracleSchemaStatVisitor();
            statement.accept(visitor);

            // 获取表名
            Map<TableStat.Name, TableStat> tables = visitor.getTables();
            Set<TableStat.Name> tableNames = tables.keySet();
            // 输出表名
            for (TableStat.Name tableName : tableNames) {
                srcTableList.add(tableName.getName());
                //System.out.println("表名: " + tableName.getName());
            }
        }catch (Exception e){
            log.error("解析失败：[{}]=====================================================\n\n",sql);
            srcTableList.add(sql);
        }

        return srcTableList;

    }

    public static String extractContent(String sql) {
        //log.debug("待解析sql语句：[{}]",sql);
        int openIndex = -1;
        int openCount = 0;

        for (int i = 0; i < sql.length(); i++) {
            if (sql.charAt(i) == '(') {
                if (openIndex == -1) {
                    openIndex = i;
                }
                openCount++;
            } else if (sql.charAt(i) == ')') {
                openCount--;
                if (openCount == 0) {
                    return sql.substring(openIndex + 1, i);
                }
            }
        }
        return null;
    }

    public static void writeExcel(List<TableRelaInfo> tableRelaInfos,String outputPath) {
        File templateFile = new File(tpl_path);
        File outputFile = new File(outputPath);
        List<TableRelaInfo> tableRelaInfosAll = new ArrayList<>();
        for (TableRelaInfo tableRelaInfo : tableRelaInfos) {
            String tableNameEn = tableRelaInfo.getTableNameEn();
            String tableNameCn = tableRelaInfo.getTableNameCn();
            LinkedHashSet<String> relatedTablesTmp = tableRelaInfo.getRelatedTables();
            for (String relatedTable : relatedTablesTmp) {
                tableRelaInfosAll.add(new TableRelaInfo(tableNameEn, tableNameCn, relatedTable));
            }
        }
        try(ExcelWriter excelWriter = FastExcel.write(outputPath).withTemplate(tpl_path).build()){
            WriteSheet task_sheet = FastExcel.writerSheet("模型加工依赖表结果").build();
            excelWriter.fill(tableRelaInfosAll,task_sheet);
        }
        log.info("解析依赖表成功：[{}]",outputPath);
    }

}

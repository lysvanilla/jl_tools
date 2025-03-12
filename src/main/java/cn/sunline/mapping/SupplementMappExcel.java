package cn.sunline.mapping;

import cn.hutool.core.io.FileUtil;
import cn.sunline.table.ExcelTableStructureReader;
import cn.sunline.vo.TableFieldInfo;
import cn.sunline.vo.TableStructure;
import cn.sunline.vo.etl.EtlGroup;
import cn.sunline.vo.etl.EtlGroupColMapp;
import cn.sunline.vo.etl.EtlGroupJoinInfo;
import cn.sunline.vo.etl.EtlMapp;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.*;

import static cn.sunline.mapping.EtlMappingExcelRead.readEtlMappExcel;
import static cn.sunline.mapping.GenEtlMappExcel.genEtlMappExcel;

@Slf4j
public class SupplementMappExcel {
    public static void main(String[] args) {
        Map<String, String> argsMap = new HashMap<>();
        argsMap.put("file_name","D:\\svn\\jilin\\04.映射设计\\0402.计量模型层\\宝奇订单指标表.xlsx");
        argsMap.put("model_file_name","D:\\svn\\jilin\\03.模型设计\\0302.智能风控系统\\风险数据集市物理模型-计量层.xlsx");
        supplementMappExcelMain(argsMap);
    }

    public static void supplementMappExcelMain(Map<String, String> argsMap) {
        // 从 HashMap 中获取文件路径
        String filePath = argsMap.get("file_name");
        String modelFilePath = argsMap.get("model_file_name");
        // 检查文件路径是否为空
        if (StringUtils.isBlank(filePath)) {
            log.error("argsMap中缺少file_name参数");
            return;
        }
        if (StringUtils.isBlank(modelFilePath)) {
            log.error("argsMap中缺少model_file_name参数");
            return;
        }
        supplementMappExcelMain(filePath,modelFilePath);
    }
    public static void supplementMappExcelMain(String filePath,String modelFilePath){
        LinkedHashMap<String, TableStructure> tableMap = ExcelTableStructureReader.readExcel(modelFilePath);
        if (tableMap == null){
            log.error("模型文件解析获取表结构信息失败: {}", modelFilePath);
            return;
        }
        if (FileUtil.isDirectory(filePath)){
            for (File file : FileUtil.ls(filePath)) {
                String fileName = file.getName();
                if (fileName.endsWith(".xlsx") && !fileName.startsWith("~") && !fileName.endsWith("0_封面.xlsx")
                        && !fileName.endsWith("2_目录.xlsx") && !fileName.endsWith("1_变更记录.xlsx")){
                    supplementMappExcel(file.getAbsolutePath(),tableMap);
                }else{
                    log.debug("跳过文件: {}, 原因：文件名以 ~ 开头或不是 .xlsx 文件。", file.getAbsolutePath());
                    continue;
                }
            }
        }else{
            supplementMappExcel(filePath,tableMap);
        }
    }

    public static void supplementMappExcel(String filePath,LinkedHashMap<String, TableStructure> tableMap){
        List<EtlMapp> etlMappList = readEtlMappExcel(filePath);
        List<EtlMapp> supplementEtlMappList = supplementEtlMapp(etlMappList,tableMap);
        genEtlMappExcel(supplementEtlMappList);
    }

    public static List<EtlMapp> supplementEtlMapp(List<EtlMapp> etlMappList,LinkedHashMap<String, TableStructure> tableMap){
        //List<EtlMapp> etlMappListResult = new ArrayList<>();
        for (EtlMapp etlMapp : etlMappList) {
            List<EtlGroup> etlGroupList = etlMapp.getEtlGroupList();
            for (EtlGroup etlGroup : etlGroupList) {
                List<EtlGroupColMapp> etlGroupColMappList  = etlGroup.getEtlGroupColMappList();
                String filterCondition = etlGroup.getFilterCondition();
                if (StringUtils.isBlank(filterCondition)){
                    etlGroup.setFilterCondition("PART_DT = '${etl_date}'");
                } else if (!filterCondition.contains("PART_DT")) {
                    etlGroup.setFilterCondition("PART_DT = '${etl_date}' \nand"+filterCondition);
                }
                String targetTableEnglishName = etlGroup.getTargetTableEnglishName();
                TableStructure tableStructure = tableMap.get(targetTableEnglishName);
                if (tableStructure == null){
                    log.error("模型文件中不存在表结构信息: {}", targetTableEnglishName);
                    continue;
                }
                LinkedHashMap<String, TableFieldInfo> fieldCnMap = tableStructure.getFieldCnMap();
                for (EtlGroupColMapp etlGroupColMapp : etlGroupColMappList) {
                    String targetFieldChineseName = etlGroupColMapp.getTargetFieldChineseName();
                    TableFieldInfo tableFieldInfo = fieldCnMap.get(targetFieldChineseName);
                    if (tableFieldInfo == null){
                        continue;
                    }else{
                        etlGroupColMapp.setTargetFieldEnglishName(tableFieldInfo.getFieldNameEn());
                    }
                }

                List<EtlGroupJoinInfo> etlGroupJoinInfoList = etlGroup.getEtlGroupJoinInfoList();
                for (EtlGroupJoinInfo etlGroupJoinInfo : etlGroupJoinInfoList) {
                    String sourceTableSchema = etlGroupJoinInfo.getSourceTableSchema();
                    String sourceTableEnglishName = etlGroupJoinInfo.getSourceTableEnglishName();
                    if (StringUtils.isNotBlank(sourceTableSchema)){
                        continue;
                    } else if (!sourceTableEnglishName.contains("(")) {
                        etlGroupJoinInfo.setSourceTableSchema("pm_ridata");
                    }
                }
            }
        }

        return etlMappList;
    }


}

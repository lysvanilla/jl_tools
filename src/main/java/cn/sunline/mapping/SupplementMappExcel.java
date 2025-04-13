package cn.sunline.mapping;

import cn.hutool.core.io.FileUtil;
import cn.sunline.table.ExcelTableStructureReader;
import cn.sunline.util.BasicInfo;
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
        argsMap.put("file_name","D:\\svn\\jilin\\04.映射设计\\0401.基础模型层\\信用卡五级分类余额表.xlsx");
        //argsMap.put("model_file_name","D:\\svn\\jilin\\03.模型设计\\0303.基础模型层\\风险数据集市物理模型-基础层_v0.2.xlsx");
        argsMap.put("model_file_name", BasicInfo.baseModelPath);
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
        if (tableMap.isEmpty()){
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
        log.info("开始补充映射表: {}", filePath);
        List<EtlMapp> etlMappList = readEtlMappExcel(filePath);
        List<EtlMapp> supplementEtlMappList = supplementEtlMapp(etlMappList,tableMap);
        genEtlMappExcel(supplementEtlMappList);
    }

    public static List<EtlMapp> supplementEtlMapp(List<EtlMapp> etlMappList,LinkedHashMap<String, TableStructure> tableMap){
        //List<EtlMapp> etlMappListResult = new ArrayList<>();
        for (EtlMapp etlMapp : etlMappList) {
            String attributionLevel = StringUtils.defaultIfBlank(etlMapp.getAttributionLevel(),"");
            String tableEnglishName = etlMapp.getTableEnglishName();
            String tableChineseName = etlMapp.getTableChineseName();
            List<EtlGroup> etlGroupList = etlMapp.getEtlGroupList();
            for (EtlGroup etlGroup : etlGroupList) {
                List<EtlGroupJoinInfo> etlGroupJoinInfoList = etlGroup.getEtlGroupJoinInfoList();
                String mainSrcTableEn = etlGroupJoinInfoList.get(0).getSourceTableEnglishName();
                String mainSrcTableCn = etlGroupJoinInfoList.get(0).getSourceTableChineseName();
                for (EtlGroupJoinInfo etlGroupJoinInfo : etlGroupJoinInfoList) {
                    String sourceTableSchema = etlGroupJoinInfo.getSourceTableSchema();
                    String sourceTableEnglishName = etlGroupJoinInfo.getSourceTableEnglishName();
                    String sourceTableEnglishNameLower = StringUtils.lowerCase(sourceTableEnglishName);
                    if (sourceTableEnglishNameLower.startsWith("c") || sourceTableEnglishNameLower.startsWith("m")){
                        etlGroupJoinInfo.setSourceTableEnglishName("NDWJ_"+sourceTableEnglishName);
                        etlGroupJoinInfo.setSourceTableSchema("pdata");
                    }
                    if (StringUtils.isNotBlank(sourceTableSchema)){
                        continue;
                    } else if (!sourceTableEnglishName.contains("(")) {
                        etlGroupJoinInfo.setSourceTableSchema("pdata");
                    }
                }

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
                    String targetFieldEnglishName = etlGroupColMapp.getTargetFieldEnglishName();
                    String targetFieldChineseName = etlGroupColMapp.getTargetFieldChineseName();
                    String targetFieldChineseNameChange = targetFieldChineseName;
                    targetFieldChineseName = targetFieldChineseName.replaceAll("帐","账").replaceAll("戶","户").replaceAll("稅","税")
                            .replaceAll("重新订价日","重新定价日");
                    if (targetFieldChineseName.equals("主键")){
                        targetFieldChineseName = "主键id";
                    }
                    if (!targetFieldChineseName.equals(targetFieldChineseNameChange)){
                        log.info("字段名称做修改: [{}]-[{}]-[{}]-[{}]",tableChineseName,tableEnglishName,targetFieldChineseNameChange, targetFieldChineseName);
                    }
                    String sourceTableEnglishName = etlGroupColMapp.getSourceTableEnglishName();
                    String sourceFieldChineseName = etlGroupColMapp.getSourceFieldChineseName();
                    String sourceFieldEnglishName = etlGroupColMapp.getSourceFieldEnglishName();
                    String sourceFieldType = etlGroupColMapp.getSourceFieldType();
                    String sourceTableEnglishNameLower = StringUtils.lowerCase(sourceTableEnglishName);
                    String mappingRule = etlGroupColMapp.getMappingRule();
                    String remarks = etlGroupColMapp.getRemarks();
                    TableFieldInfo tableFieldInfo = fieldCnMap.get(targetFieldChineseName);
                    if (tableFieldInfo == null){
                        log.error("模型文件中不存在字段信息: \t{}\t{}\t{}\t{}\t{}", tableChineseName,tableEnglishName, targetFieldChineseNameChange,targetFieldEnglishName, targetFieldChineseName);
                    }else{
                        etlGroupColMapp.setTargetFieldEnglishName(tableFieldInfo.getFieldNameEn());
                    }
                    etlGroupColMapp.setTargetFieldChineseName(targetFieldChineseName);
                    if (targetFieldChineseName.equals("法人机构编码")||targetFieldChineseName.equals("法人机构编号")){
                        etlGroupColMapp.setTargetFieldEnglishName("LPR_ORG_ID");
                        etlGroupColMapp.setTargetFieldChineseName("法人机构编号");
                        if (StringUtils.isBlank(mappingRule)){
                            etlGroupColMapp.setMappingRule("null");
                            etlGroupColMapp.setRemarks(StringUtils.defaultIfBlank(remarks,"默认值"));
                        }
                    }else if (targetFieldChineseName.equals("源表名称")){
                        if (StringUtils.isNotBlank(mappingRule) ){
                            String mappingRuleTmp = mappingRule;
                            if (mappingRuleTmp.startsWith("''")){
                                mappingRuleTmp = mappingRuleTmp.substring(1);
                            }
                            if (!mappingRuleTmp.startsWith("'")){
                                mappingRuleTmp = "'"+mappingRuleTmp;
                            }
                            if (!mappingRuleTmp.endsWith("'")){
                                mappingRuleTmp = mappingRuleTmp+"'";
                            }
                            etlGroupColMapp.setMappingRule(mappingRuleTmp);
                        }else{
                            etlGroupColMapp.setMappingRule("'"+mainSrcTableEn+"'");
                        }
                        etlGroupColMapp.setRemarks(StringUtils.defaultIfBlank(remarks,"默认值"));
                    }else if (targetFieldChineseName.equals("分区日期")) {
                        etlGroupColMapp.setMappingRule("PART_DT");
                        etlGroupColMapp.setSourceTableEnglishName(StringUtils.defaultIfBlank(sourceTableEnglishName,mainSrcTableEn));
                        etlGroupColMapp.setSourceFieldChineseName(StringUtils.defaultIfBlank(sourceFieldChineseName,mainSrcTableCn));
                        etlGroupColMapp.setSourceFieldEnglishName(StringUtils.defaultIfBlank(sourceFieldEnglishName,"PART_DT"));
                        etlGroupColMapp.setSourceFieldType(StringUtils.defaultIfBlank(sourceFieldType,"VARCHAR(10)"));
                    }else if (targetFieldChineseName.equals("ETL时间")) {
                        etlGroupColMapp.setMappingRule("SYSTIMESTAMP");
                        etlGroupColMapp.setRemarks(StringUtils.defaultIfBlank(remarks,"默认值"));
                    }
                    if (sourceTableEnglishNameLower.startsWith("c_") || sourceTableEnglishNameLower.startsWith("m_")){
                        etlGroupColMapp.setSourceTableEnglishName("NDWJ_"+sourceTableEnglishName);
                    }

                }


            }
        }

        return etlMappList;
    }


}

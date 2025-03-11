package cn.sunline.mapping;

import cn.sunline.table.ExcelTableStructureReader;
import cn.sunline.vo.TableFieldInfo;
import cn.sunline.vo.TableStructure;
import cn.sunline.vo.etl.EtlGroup;
import cn.sunline.vo.etl.EtlGroupColMapp;
import cn.sunline.vo.etl.EtlGroupJoinInfo;
import cn.sunline.vo.etl.EtlMapp;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

import static cn.sunline.mapping.GenEtlMappExcel.genEtlMappExcel;
import static cn.sunline.util.BasicInfo.TEMPLATE_SETTING;

@Slf4j
public class TableToEtlMapp {
    public static void main(String[] args) {
        Map<String, String> argsMap = new HashMap<>();
        argsMap.put("file_name","D:\\svn\\jilin\\03.模型设计\\0302.智能风控系统\\风险数据集市物理模型-接口层.xlsx");
        tableToEtlMapp(argsMap);
    }
    public static void tableToEtlMapp(Map<String, String> argsMap) {
        // 从 HashMap 中获取文件路径
        String filePath = argsMap.get("file_name");
        // 检查文件路径是否为空
        if (StringUtils.isBlank(filePath)) {
            // 若为空，记录错误日志
            log.error("argsMap中缺少file_name参数");
            return;
        }
        tableToEtlMapp(filePath);
    }

    public static void tableToEtlMapp(String filePath) {
        LinkedHashMap<String, TableStructure> tableMap = ExcelTableStructureReader.readExcel(filePath);
        List<EtlMapp> etlMappList = new ArrayList<>();
        for (String tableName : tableMap.keySet()) {
            TableStructure tableStructure = tableMap.get(tableName);
            EtlMapp etlMapp = tableToEtlMapp(tableStructure);
            etlMappList.add(etlMapp);
        }
        genEtlMappExcel(etlMappList);
    }

    public static EtlMapp tableToEtlMapp(TableStructure tableStructure) {
        EtlMapp etlMapp = new EtlMapp();
        String id = tableStructure.getId();
        String systemModule = tableStructure.getSystemModule();
        String subject = tableStructure.getSubject();
        String tableNameEn = tableStructure.getTableNameEn();
        String tableNameCn = tableStructure.getTableNameCn();
        String description = tableStructure.getDescription();
        String tableCreationType = tableStructure.getTableCreationType();
        String algorithmType = tableStructure.getAlgorithmType();
        String hasPrimaryKey = tableStructure.getHasPrimaryKey();
        String partitionMethod = tableStructure.getPartitionMethod();
        String bucketCount = tableStructure.getBucketCount();
        String importanceLevel = tableStructure.getImportanceLevel();
        String onlineTime = tableStructure.getOnlineTime();
        String downstreamApplications = tableStructure.getDownstreamApplications();
        String publicStatus = tableStructure.getPublicStatus();
        String sourceSystem = tableStructure.getSourceSystem();
        String sourceTableNameEn = tableStructure.getSourceTableNameEn();
        String designer = tableStructure.getDesigner();
        String status = tableStructure.getStatus();
        String updateDate = tableStructure.getUpdateDate();
        String remark = tableStructure.getRemark();
        String updatePerson = tableStructure.getUpdatePerson();
        String tableSchema = TEMPLATE_SETTING.get(systemModule);

        etlMapp.setSheetName(tableNameCn);
        etlMapp.setTableEnglishName(tableNameEn);
        etlMapp.setTableChineseName(tableNameCn);
        etlMapp.setAnalyst(designer);
        etlMapp.setCreationDate(onlineTime);
        etlMapp.setDescription(remark);
        etlMapp.setAttributionLevel("应用接口层");
        etlMapp.setTimeGranularity("日");
        etlMapp.setRetentionPeriod("永久");

        // 存储分桶键的列表
        List<String> bucketKeys = new ArrayList<>();
        List<String> primaryKeys = new ArrayList<>();


        EtlGroup etlGroup = new EtlGroup();
        etlGroup.setTargetTableEnglishName(tableNameEn);
        etlGroup.setTargetTableChineseName(tableNameCn);
        etlGroup.setFilterCondition("t1.PART_DT='${etl_date}'");
        etlGroup.setTemplateType("N2");

        EtlGroupJoinInfo etlGroupJoinInfo = new EtlGroupJoinInfo();
        etlGroupJoinInfo.setSourceTableSchema(tableSchema);
        etlGroupJoinInfo.setSourceTableAlias("t1");
        etlGroupJoinInfo.setSourceTableEnglishName(sourceTableNameEn);
        etlGroupJoinInfo.setSourceTableChineseName(tableNameCn);

        etlGroup.addEtlGroupJoinInfo(etlGroupJoinInfo);

        // 遍历表的字段信息，找出分桶键
        for (TableFieldInfo tableFieldInfo : tableStructure.getFields()) {
            String idField = tableFieldInfo.getId();
            String systemModuleField = tableFieldInfo.getSystemModule();
            String subjectField = tableFieldInfo.getSubject();
            String tableNameEnField = tableFieldInfo.getTableNameEn();
            String tableNameCnField = tableFieldInfo.getTableNameCn();
            String fieldNameEn = tableFieldInfo.getFieldNameEn();
            String fieldNameCn = tableFieldInfo.getFieldNameCn();
            String primaryKey = tableFieldInfo.getPrimaryKey();
            String bucketKey = tableFieldInfo.getBucketKey();
            String notNull = tableFieldInfo.getNotNull();
            Integer fieldOrder = tableFieldInfo.getFieldOrder();
            String fieldType = tableFieldInfo.getFieldType();
            String foreignKey = tableFieldInfo.getForeignKey();
            String ifCodeField = tableFieldInfo.getIfCodeField();
            String referenceCode = tableFieldInfo.getReferenceCode();
            String codeDescription = tableFieldInfo.getCodeDescription();
            String checkRule = tableFieldInfo.getCheckRule();
            String sensitiveType = tableFieldInfo.getSensitiveType();
            String onlineTimeField = tableFieldInfo.getOnlineTime();
            String sourceSystemField = tableFieldInfo.getSourceSystem();
            String downstreamApplicationsField = tableFieldInfo.getDownstreamApplications();
            String remarkField = tableFieldInfo.getRemark();
            String updateDateField = tableFieldInfo.getUpdateDate();
            String updatePersonField = tableFieldInfo.getUpdatePerson();
            String sourceFieldNameEn = tableFieldInfo.getSourceFieldNameEn();

            if ("Y".equals(bucketKey)) {
                bucketKeys.add(fieldNameEn);
            }
            if ("Y".equals(primaryKey)) {
                primaryKeys.add(fieldNameEn);
            }

            if (StringUtils.isBlank(sourceFieldNameEn)){
                sourceFieldNameEn = fieldNameEn;
            }

            EtlGroupColMapp etlGroupColMapp = new EtlGroupColMapp();
            etlGroupColMapp.setTargetTableEnglishName(tableNameEn);
            etlGroupColMapp.setTargetTableChineseName(tableNameCn);
            etlGroupColMapp.setTargetFieldType(fieldType);
            etlGroupColMapp.setTargetFieldEnglishName(fieldNameEn);
            etlGroupColMapp.setTargetFieldChineseName(fieldNameCn);
            etlGroupColMapp.setSourceTableSchema(tableSchema);
            etlGroupColMapp.setSourceTableEnglishName(sourceTableNameEn);
            etlGroupColMapp.setSourceTableChineseName(tableNameCn);
            etlGroupColMapp.setSourceFieldType(fieldType);
            etlGroupColMapp.setSourceFieldEnglishName(sourceFieldNameEn);
            etlGroupColMapp.setSourceFieldChineseName(fieldNameCn);
            etlGroupColMapp.setRemarks(remarkField);
            etlGroupColMapp.setMappingRule("t1."+StringUtils.lowerCase(sourceFieldNameEn));
            etlGroup.addEtlGroupColMapp(etlGroupColMapp);
        }

        etlMapp.setPrimaryKeyField(String.join(",",primaryKeys));
        etlMapp.addEtlGroup(etlGroup);

        return etlMapp;
    }
}

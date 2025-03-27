package cn.sunline.mapping;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.sunline.table.ExcelTableStructureReader;
import cn.sunline.util.BasicInfo;
import cn.sunline.vo.StandardizedMappingRelation;
import cn.sunline.vo.TableFieldInfo;
import cn.sunline.vo.TableStructure;
import cn.sunline.vo.etl.EtlGroup;
import cn.sunline.vo.etl.EtlGroupColMapp;
import cn.sunline.vo.etl.EtlMapp;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.*;

import static cn.sunline.mapping.EtlMappingExcelRead.readEtlMappExcel;
import static cn.sunline.mapping.GenEtlMappExcel.genEtlMappExcel;
import static cn.sunline.mapping.GenStandardizedInfoExcel.writeStandardizedInfoExcel;
import static cn.sunline.mapping.GenTableStructureExcel.writeTableStructureExcel;
import static cn.sunline.table.StandardizedMappingRelationReader.readExcel;

@Slf4j
public class BatchStandardizedModelExcel {
    private static LinkedHashMap<String, StandardizedMappingRelation> mappingMap = readExcel();
    private static List<StandardizedMappingRelation> mappingList = new ArrayList<>();
    // 定义基础导出路径，使用 BasicInfo 类的方法获取
    private static final String BASIC_EXPORT_PATH = BasicInfo.getBasicExportPath("");
    public static void main(String[] args) {
        Map<String, String> argsMap = new HashMap<>();
        //argsMap.put("file_name","D:\\svn\\jilin\\03.模型设计\\0303.基础模型层\\风险数据集市物理模型-基础层_v0.2.xlsx");
        argsMap.put("file_name",BasicInfo.baseModelPath);
        batchUpdateModelExcelMain(argsMap);
        System.out.println("finish");
    }

    public static void batchUpdateModelExcelMain(Map<String, String> argsMap) {
        // 从 HashMap 中获取文件路径
        String filePath = argsMap.get("file_name");
        // 检查文件路径是否为空
        if (StringUtils.isBlank(filePath)) {
            // 若为空，记录错误日志
            log.error("argsMap中缺少file_name参数");
            return;
        }

        batchUpdateModelExcel(filePath);
        writeStandardizedInfoExcel(mappingList);
    }

    public static void batchUpdateModelExcel(String filePath){
        log.info("开始补充映射表: {}", filePath);
        // 调用 ExcelTableStructureReader 类的 readExcel 方法读取 Excel 文件，获取表结构信息
        LinkedHashMap<String, TableStructure> tableMap = ExcelTableStructureReader.readExcel(filePath);
        List<TableStructure> stringTableStructureLinkedHashMap = getTableStructureExcel(tableMap);
        String outputPath = BASIC_EXPORT_PATH + FileUtil.mainName(filePath)+"_标准化变更后物理模型" + DateUtil.format(DateUtil.date(), "YYYYMMdd_HHmmss") + ".xlsx";
        writeTableStructureExcel(stringTableStructureLinkedHashMap,outputPath);
    }

    public static List<TableStructure> getTableStructureExcel(LinkedHashMap<String, TableStructure> tableMap) {
        List<TableStructure> tableStructureList = new ArrayList<>();
        for (Map.Entry<String, TableStructure> entry : tableMap.entrySet()) {
            String key = entry.getKey();
            TableStructure value = entry.getValue();
            List<TableFieldInfo> tableFieldInfoList = value.getFields();
            if (tableFieldInfoList == null){
                tableFieldInfoList = new ArrayList<>();
                log.error("[{}]-[{}]在字段级信息中未存在",key,value.getTableNameCn());
            }
            for (TableFieldInfo tableFieldInfo : tableFieldInfoList) {
                String tableNameEn = tableFieldInfo.getTableNameEn();
                String tableNameCn = tableFieldInfo.getTableNameCn();
                String fieldNameEn = tableFieldInfo.getFieldNameEn();
                String fieldNameCn = tableFieldInfo.getFieldNameCn();
                String fieldType = tableFieldInfo.getFieldType();
                if (mappingMap.containsKey(fieldNameEn)){
                    StandardizedMappingRelation standardizedMappingRelation = mappingMap.get(fieldNameEn);
                    String fieldChineseNameStd = standardizedMappingRelation.getFieldChineseName();
                    String fieldEnglishNameStd = standardizedMappingRelation.getFieldEnglishName();
                    String fieldTypeStd = standardizedMappingRelation.getFieldType();

                    String changeType = "";
                    if (StringUtils.isNotBlank(fieldNameEn)) {
                        if (!fieldEnglishNameStd.equals(fieldNameEn)) {
                            changeType = "字段英文名变更";
                            tableFieldInfo.setFieldNameEn(fieldEnglishNameStd);
                        }
                    }
                    if (StringUtils.isNotBlank(fieldNameCn)) {
                        if (!fieldChineseNameStd.equals(fieldNameCn)) {
                            changeType = changeType + ",字段中文名变更";
                            tableFieldInfo.setFieldNameCn(fieldChineseNameStd);
                        }
                    }
                    if (StringUtils.isNotBlank(fieldTypeStd)){
                        if (!fieldTypeStd.equals(fieldType)){
                            changeType = changeType+",字段类型变更";
                            tableFieldInfo.setFieldType(fieldTypeStd);
                        }
                    }

                    if (StringUtils.isNotBlank(changeType)){
                        StandardizedMappingRelation standardizedMappingRelation1 = new StandardizedMappingRelation(tableNameCn,tableNameEn,fieldNameCn,fieldNameEn,fieldType,fieldChineseNameStd,fieldEnglishNameStd,fieldTypeStd,changeType);
                        //System.out.println(standardizedMappingRelation1.toString());
                        mappingList.add(standardizedMappingRelation1);
                    }
                }
            }
            tableStructureList.add(value);
        }

        return tableStructureList;
    }
}

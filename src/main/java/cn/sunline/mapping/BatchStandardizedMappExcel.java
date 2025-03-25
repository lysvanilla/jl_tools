package cn.sunline.mapping;

import cn.hutool.core.io.FileUtil;
import cn.sunline.vo.StandardizedMappingRelation;
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
import static cn.sunline.table.StandardizedMappingRelationReader.readExcel;

@Slf4j
public class BatchStandardizedMappExcel {
    private static LinkedHashMap<String, StandardizedMappingRelation> mappingMap = readExcel();
    private static List<StandardizedMappingRelation> mappingList = new ArrayList<>();
    public static void main(String[] args) {
        Map<String, String> argsMap = new HashMap<>();
        argsMap.put("file_name","D:\\svn\\jilin\\04.映射设计\\0401.基础模型层\\");
        batchUpdateMappExcelMain(argsMap);
    }

    public static void batchUpdateMappExcelMain(Map<String, String> argsMap) {
        // 从 HashMap 中获取文件路径
        String filePath = argsMap.get("file_name");
        // 检查文件路径是否为空
        if (StringUtils.isBlank(filePath)) {
            // 若为空，记录错误日志
            log.error("argsMap中缺少file_name参数");
            return;
        }
        batchUpdateMappExcelMain(filePath);
        writeStandardizedInfoExcel(mappingList);
    }
    public static void batchUpdateMappExcelMain(String filePath){
        if (FileUtil.isDirectory(filePath)){
            List<File> files = FileUtil.loopFiles(filePath);
            // 使用 Collections.sort 方法和自定义的 Comparator 对列表进行排序
            Collections.sort(files, Comparator.comparing(File::getName));
            for (File file : files) {
                String fileName = file.getName();
                if (fileName.startsWith("~") && !fileName.equals(".xlsx")){
                    continue;
                }
                batchUpdateMappExcel(file.getAbsolutePath());
            }
        }else{
            batchUpdateMappExcel(filePath);
        }
    }

    public static void batchUpdateMappExcel(String filePath){
        log.info("开始补充映射表: {}", filePath);
        List<EtlMapp> etlMappList = readEtlMappExcel(filePath);
        List<EtlMapp> standardizedEtlMappList = getEtlMappExcel(etlMappList);
        genEtlMappExcel(standardizedEtlMappList);
        //System.out.println("1");
    }

    public static List<EtlMapp> getEtlMappExcel(List<EtlMapp> etlMappList) {
        for (EtlMapp etlMapp : etlMappList) {
            String sheetName = etlMapp.getSheetName();
            String tableEnglishName = etlMapp.getTableEnglishName();
            String tableChineseName = etlMapp.getTableChineseName();
            List<EtlGroup> etlGroupList = etlMapp.getEtlGroupList();
            for (EtlGroup etlGroup : etlGroupList) {
                /*List<EtlGroupJoinInfo> etlGroupJoinInfoList = etlGroup.getEtlGroupJoinInfoList();
                for (EtlGroupJoinInfo etlGroupJoinInfo : etlGroupJoinInfoList) {
                    String sourceTableSchema = etlGroupJoinInfo.getSourceTableSchema();
                    String sourceTableEnglishName = etlGroupJoinInfo.getSourceTableEnglishName();
                    String sourceTableEnglishNameLower = StringUtils.lowerCase(sourceTableEnglishName);
                }*/

                List<EtlGroupColMapp> etlGroupColMappList  = etlGroup.getEtlGroupColMappList();
                for (EtlGroupColMapp etlGroupColMapp : etlGroupColMappList) {
                    String targetFieldChineseName = etlGroupColMapp.getTargetFieldChineseName();
                    String targetFieldEnglishName = etlGroupColMapp.getTargetFieldEnglishName();
                    String targetFieldType = etlGroupColMapp.getTargetFieldType();
                    if (mappingMap.containsKey(targetFieldEnglishName)){
                        StandardizedMappingRelation standardizedMappingRelation = mappingMap.get(targetFieldEnglishName);
                        String fieldChineseName = standardizedMappingRelation.getFieldChineseName();
                        String fieldEnglishName = standardizedMappingRelation.getFieldEnglishName();
                        String fieldType = standardizedMappingRelation.getFieldType();
                        etlGroupColMapp.setTargetFieldChineseName(fieldChineseName);
                        etlGroupColMapp.setTargetFieldEnglishName(fieldEnglishName);

                        String changeType = "";
                        if (!targetFieldEnglishName.equals(fieldEnglishName)){
                            changeType = "字段英文名变更";
                        }
                        if (!targetFieldChineseName.equals(fieldChineseName)){
                            changeType = changeType+",字段中文名变更";
                        }
                        if (StringUtils.isNotBlank(fieldType)){
                            etlGroupColMapp.setTargetFieldType(fieldType);
                            if (!targetFieldType.equals(fieldType)){
                                changeType = changeType+",字段类型变更";
                            }
                        }

                        if (StringUtils.isNotBlank(changeType)){
                            StandardizedMappingRelation standardizedMappingRelation1 = new StandardizedMappingRelation(tableChineseName,tableEnglishName,targetFieldChineseName,targetFieldEnglishName,targetFieldType,fieldChineseName,fieldEnglishName,fieldType,changeType);
                            System.out.println(standardizedMappingRelation1.toString());
                            mappingList.add(standardizedMappingRelation1);
                        }
                    }
                }
            }
        }

        return etlMappList;
    }
}

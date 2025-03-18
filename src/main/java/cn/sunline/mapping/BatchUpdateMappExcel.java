package cn.sunline.mapping;

import cn.hutool.core.io.FileUtil;
import cn.sunline.vo.etl.EtlMapp;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.Comparator;

import static cn.sunline.mapping.EtlMappingExcelRead.readEtlMappExcel;
import static cn.sunline.mapping.GenEtlMappExcel.genEtlMappExcel;

@Slf4j
public class BatchUpdateMappExcel {
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
        genEtlMappExcel(etlMappList);
    }
}

package cn.sunline;

import cn.idev.excel.FastExcel;
import cn.sunline.vo.TableFieldInfo;
import cn.sunline.vo.TableStructure;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;

@Slf4j
public class ExcelTableStructureReader {

    public static void main(String[] args) {
        String filePath = "D:\\BaiduSyncdisk\\工作目录\\项目\\202501-吉林银行风险集市\\物理模型模板.xlsx";
        LinkedHashMap<String, TableStructure> tableMap = ExcelTableStructureReader.readExcel(filePath);
        tableMap.get("kdpa_zhxinx").getFields().forEach(System.out::println);
        System.out.println("1");
    }

    public static LinkedHashMap<String, TableStructure> readExcel(String filePath) {
        LinkedHashMap<String, TableStructure> tableMap = new LinkedHashMap<>();
        File file = new File(filePath);

        if (!file.exists() || !file.isFile()) {
            log.error("指定的Excel文件不存在或不是一个有效的文件: {}", filePath);
            return tableMap;
        }

        // 读取表基本信息
        List<TableStructure> tableStructures = FastExcel.read(file)
                .sheet("表级信息")
                .head(TableStructure.class)
                .doReadSync();

        // 读取字段信息
        List<TableFieldInfo> tableFieldInfos = FastExcel.read(file)
                .sheet("字段级信息")
                .head(TableFieldInfo.class)
                .doReadSync();

        // 将表信息放入Map
        tableStructures.stream()
        .filter(table -> table.getTableNameEn() != null && !table.getTableNameEn().trim().isEmpty())
        .forEach(table -> tableMap.put(table.getTableNameEn(), table));

        // 将字段信息添加到对应的表结构中
        tableFieldInfos.stream()
        .filter(field -> field.getTableNameEn() != null && !field.getTableNameEn().trim().isEmpty())
        .forEach(field -> {
            TableStructure table = tableMap.get(field.getTableNameEn());
            if (table != null) {
                table.addField(field);
            } else {
                log.warn("未找到对应的表结构: {}", field.getTableNameEn());
            }
        });

        /*for (TableStructure table : tableStructures) {
            if (table.getTableNameEn() != null && !table.getTableNameEn().trim().isEmpty()) {
                tableMap.put(table.getTableNameEn(), table);
            }
        }

        for (TableFieldInfo field : tableFieldInfos) {
            String tableNameEn = field.getTableNameEn();
            if (tableNameEn != null && !tableNameEn.trim().isEmpty()) {
                TableStructure table = tableMap.get(tableNameEn);
                if (table != null) {
                    table.addField(field);
                }
            }
        }*/


        
        return tableMap;
    }
}
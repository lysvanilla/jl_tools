package cn.sunline.mapping;

import cn.sunline.vo.etl.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class EtlMappingExcelRead {
    public static void main(String[] args) {
        List<EtlMapp> etlMappList = readEtlMappExcel("D:\\svn\\jilin\\04.映射设计\\0401.基础模型层\\个人业务申请信息.xlsx");
        System.out.println("1"+etlMappList.size());
    }

    public static List<EtlMapp> readEtlMappExcel(String filePath)  {
        // 调整最小压缩率限制，防止因压缩率问题导致文件读取失败
        ZipSecureFile.setMinInflateRatio(0);
        List<EtlMapp> etlMappList = new ArrayList<>();
        File file = new File(filePath);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            Workbook workbook = new XSSFWorkbook(fis);
            int sheetCount = workbook.getNumberOfSheets();
            // 遍历输入文件中的每个工作表
            for (int i = 0; i < sheetCount; i++) {
                EtlMapp etlMapp = new EtlMapp();
                Sheet sheet = workbook.getSheetAt(i);
                // 读取表级信息
                readEtlMappInfo(sheet, etlMapp);

                // 读取EtlGroup相关内容
                readEtlGroups(sheet, etlMapp);
                etlMappList.add(etlMapp);
            }
            workbook.close();
            fis.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return etlMappList;
    }

    private static void readEtlMappInfo(Sheet sheet, EtlMapp etlMapp) {
        Row row4 = sheet.getRow(3);
        Row row5 = sheet.getRow(4);
        Row row6 = sheet.getRow(5);

        etlMapp.setSheetName(sheet.getSheetName());
        etlMapp.setTableChineseName(getCellValue(row4, 2));  //中文名称(*)
        etlMapp.setPrimaryKeyField(getCellValue(row4, 4));  //主键字段
        etlMapp.setAnalyst(getCellValue(row4, 7));  //分析人员
        etlMapp.setAttributionLevel(getCellValue(row4, 9));  //归属层次
        etlMapp.setTimeGranularity(getCellValue(row4, 11));  //时间粒度
        etlMapp.setTableEnglishName(getCellValue(row5, 2));  //英文名称(*)
        etlMapp.setMainApplication(getCellValue(row5, 4));  //主要应用
        etlMapp.setCreationDate(getCellValue(row5, 7));  //创建日期
        etlMapp.setAttributionTheme(getCellValue(row5, 9));  //归属主题
        etlMapp.setRetentionPeriod(getCellValue(row5, 11));  //保留周期
        etlMapp.setDescription(getCellValue(row6, 2));  //注释
    }

    private static void readEtlGroups(Sheet sheet, EtlMapp etlMapp) {
        int etlGroupColMappStartRow = -1;
        int etlGroupJoinInfoStartRow = -1;
        int updateRecodeStartRow = -1;
        boolean isUpdateRecode = false;
        EtlGroup currentGroup = null;
        int group_index = 0;

        for (int rowNum = 0; rowNum <= sheet.getLastRowNum(); rowNum++) {
            Row row = sheet.getRow(rowNum);
            if (row == null) continue;
            Cell cellB = row.getCell(1);
            String cellBValue = getCellValue(cellB);

            if  (cellBValue.startsWith("更新记录")) {
                updateRecodeStartRow = rowNum +2;
                isUpdateRecode = true;
            }else if (cellBValue.startsWith("字段映射第")) {
                if (isUpdateRecode) {
                    readEtlUpdateRecord(sheet, updateRecodeStartRow, rowNum - 1, etlMapp);
                }
                isUpdateRecode = false;
                group_index++;
                etlGroupColMappStartRow = rowNum + 4;
                currentGroup = new EtlGroup();
                currentGroup.setGroupId(String.valueOf(group_index));
                currentGroup.setTargetTableChineseName(getCellValue(sheet,rowNum+1, 2));  //目标表中文名(*)
                currentGroup.setTargetTableEnglishName(getCellValue(sheet,rowNum+1,4));  //目标表英文名(*)
                currentGroup.setGroupRemarks(getCellValue(sheet,rowNum+1, 7));  //注释
                currentGroup.setTemplateType(getCellValue(sheet,rowNum+1,11));  //是否临时表(*)
                etlMapp.addEtlGroup(currentGroup);
            } else if("分布键（distributed by）".equals(cellBValue)){
                currentGroup.setDistributionKey(getCellValue(row, 2));
                int etlGroupColMappEndRow = rowNum - 1;
                readEtlGroupColMapp(sheet, etlGroupColMappStartRow, etlGroupColMappEndRow, currentGroup);
            }else if ("表关联信息".equals(cellBValue)) {
                etlGroupJoinInfoStartRow = rowNum + 2;
            }else if("过滤条件（where）".equals(cellBValue)){
                currentGroup.setFilterCondition(getCellValue(row, 2));
                int associationEndRow = rowNum - 1;
                readEtlGroupJoinInfo(sheet, etlGroupJoinInfoStartRow, associationEndRow, currentGroup);
            }else if("分组条件（group by）".equals(cellBValue)){
                currentGroup.setGroupingCondition(getCellValue(row, 2));
            }else if("排序条件（order by）".equals(cellBValue)){
                currentGroup.setSortingCondition(getCellValue(row, 2));
            }else if("初始设置".equals(cellBValue)){
                etlMapp.setInitialSettings(getCellValue(row, 2));
            }else if("初始加载".equals(cellBValue)){
                etlMapp.setInitialLoad(getCellValue(row, 2));
            }else if("每日加载".equals(cellBValue)){
                etlMapp.setDailyLoad(getCellValue(row, 2));
            }
        }

    }

    private static void readEtlGroupColMapp(Sheet sheet, int startRow, int endRow, EtlGroup group) {
        for (int rowNum = startRow; rowNum <= endRow; rowNum++) {
            Row row = sheet.getRow(rowNum);
            if (row == null) continue;
            String targetFieldEnglishName = getCellValue(row, 2);   //字段英文名(*)
            String targetTableChineseName = getCellValue(row, 1);  //字段中文名(*)
            if (StringUtils.isBlank(targetFieldEnglishName) && StringUtils.isBlank(targetTableChineseName)){
                continue;
            }
            EtlGroupColMapp etlGroupColMapp = new EtlGroupColMapp();
            etlGroupColMapp.setTargetFieldChineseName(targetTableChineseName);  //字段中文名(*)
            etlGroupColMapp.setTargetFieldEnglishName(targetFieldEnglishName);  //字段英文名(*)
            etlGroupColMapp.setTargetFieldType(getCellValue(row, 3));  //字段类型
            etlGroupColMapp.setSourceTableSchema(getCellValue(row, 4));  //源表schema
            etlGroupColMapp.setSourceTableChineseName(getCellValue(row, 5));  //源表中文名
            etlGroupColMapp.setSourceTableEnglishName(getCellValue(row, 6));  //源表英文名(*)
            etlGroupColMapp.setSourceFieldChineseName(getCellValue(row, 7));  //源字段中文名
            etlGroupColMapp.setSourceFieldEnglishName(getCellValue(row, 8));  //源字段英文名
            etlGroupColMapp.setSourceFieldType(getCellValue(row, 9));  //源字段类型
            etlGroupColMapp.setMappingRule(getCellValue(row, 10));  //映射规则(*)
            etlGroupColMapp.setRemarks(getCellValue(row, 11));  //注释(*)

            etlGroupColMapp.setTargetTableEnglishName(group.getTargetTableEnglishName());
            etlGroupColMapp.setTargetTableChineseName(group.getTargetTableChineseName());

            group.getEtlGroupColMappList().add(etlGroupColMapp);
        }
    }

    private static void readEtlGroupJoinInfo(Sheet sheet, int startRow, int endRow, EtlGroup group) {
        for (int rowNum = startRow; rowNum <= endRow; rowNum++) {
            Row row = sheet.getRow(rowNum);
            if (row == null) continue;
            String sourceTableEnglishName = getCellValue(row, 3);  //源表英文名(*)
            if (StringUtils.isBlank(sourceTableEnglishName)){
                continue;
            }
            EtlGroupJoinInfo joinInfo = new EtlGroupJoinInfo();
            joinInfo.setSourceTableSchema(getCellValue(row, 1));  //源表schema
            joinInfo.setSourceTableChineseName(getCellValue(row, 2));  //源表中文名
            joinInfo.setSourceTableEnglishName(sourceTableEnglishName);  //源表英文名(*)
            joinInfo.setSourceTableAlias(getCellValue(row, 4));  //源表别名(*)
            joinInfo.setJoinType(getCellValue(row, 5));  //关联类型
            joinInfo.setJoinCondition(getCellValue(row, 6));  //关联条件（on）
            joinInfo.setComment(getCellValue(row, 11));  //注释

            group.getEtlGroupJoinInfoList().add(joinInfo);
        }
    }

    private static void readEtlUpdateRecord(Sheet sheet, int startRow, int endRow, EtlMapp etlMapp) {
        for (int rowNum = startRow; rowNum <= endRow; rowNum++) {
            Row row = sheet.getRow(rowNum);
            if (row == null) continue;
            EtlUpdateRecord etlUpdateRecord = new EtlUpdateRecord();
            etlUpdateRecord.setDate(getCellValue(row, 1));  //日期
            etlUpdateRecord.setUpdater(getCellValue(row, 2));  //更新人
            etlUpdateRecord.setDescription(getCellValue(row, 3));  //说明

            etlMapp.getEtlUpdateRecordList().add(etlUpdateRecord);
        }
    }

    private static String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue().trim();
    }

    private static String getCellValue(Row row, int columnIndex) {
        if (row == null) {
            return "";
        }
        Cell cell = row.getCell(columnIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        return getCellValue(cell);
       /*cell.setCellType(CellType.STRING);
        return cell.getStringCellValue().trim();*/
    }

    private static String getCellValue(Sheet sheet,int rowIndex, int columnIndex) {
        Row row = sheet.getRow(rowIndex);
        return getCellValue(row,columnIndex);
    }
}

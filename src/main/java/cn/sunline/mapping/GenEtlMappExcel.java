package cn.sunline.mapping;

import cn.hutool.core.io.FileUtil;
import cn.hutool.poi.excel.RowUtil;
import cn.hutool.poi.excel.cell.CellUtil;
import cn.sunline.util.BasicInfo;
import cn.sunline.vo.etl.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.CellCopyPolicy;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static cn.sunline.mapping.EtlMappingExcelRead.readEtlMappExcel;
import static cn.sunline.mapping.GetMappRows.getMappingMap;
import static cn.sunline.mapping.GetMappRows.getNumber2StrMap;

@Slf4j
public class GenEtlMappExcel {
    public static final String base_export_path = BasicInfo.getBasicExportPath("映射");
    private static final String MAPP_TPL_PATH = BasicInfo.TPL_PATH + "excel/dml_mapping_template.xlsx";
    private static final HashMap<String, String> num2strmap = getNumber2StrMap();
    private static final HashMap<String, List<Row>> mapping_map = getMappingMap(MAPP_TPL_PATH);
    private static final CellCopyPolicy cellCopyPolicy = new CellCopyPolicy();

    public static void main(String[] args) {
        List<EtlMapp> etlMappList = readEtlMappExcel("D:\\svn\\jilin\\04.映射设计\\0402.计量模型层\\宝奇订单指标表.xlsx");
        genEtlMappExcel(etlMappList);
    }

    public static void genEtlMappExcel(List<EtlMapp> etlMappList) {

        for (EtlMapp etlMapp : etlMappList) {
            String sheetName = etlMapp.getSheetName();
            String temp_deal_file = base_export_path + sheetName + ".xlsx";
            FileUtil.copy(MAPP_TPL_PATH, temp_deal_file, true);
            File outFile = new File(temp_deal_file);

            try (FileInputStream fis = new FileInputStream(outFile);
                 XSSFWorkbook wb = new XSSFWorkbook(fis);
                 FileOutputStream out = new FileOutputStream(outFile)) {

                int blank_sheet = wb.getSheetIndex("空白模板");
                if (sheetName.length()>=32){
                    log.error("sheetName名字超长，[{}]-[{}]",sheetName.length(),sheetName);
                    sheetName = sheetName.substring(0,31);
                }
                wb.setSheetName(blank_sheet, sheetName);
                XSSFSheet sheet = wb.getSheet(sheetName);
                int sheet_deal_row = 0;
                // 复制初始行
                copyRows(sheet, mapping_map.get("row_gp1"), 0);

                // 设置基本信息
                setBasicInfo(sheet, etlMapp);

                // 处理更新记录
                sheet_deal_row = processUpdateRecords(sheet, etlMapp.getEtlUpdateRecordList());

                // 处理分组信息
                sheet_deal_row = processGroups(sheet, etlMapp.getEtlGroupList(),sheet_deal_row);

                // 处理加载信息
                processLoadInfo(sheet, etlMapp,sheet_deal_row);

                // 删除模板工作表
                wb.removeSheetAt(wb.getSheetIndex("mapping模板"));

                // 写入文件
                wb.write(out);
                out.flush();
                log.info("生成sheet完成：[{}]" ,sheetName);
            } catch (IOException e) {
                System.err.println("处理文件 " + temp_deal_file + " 时发生错误: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static void copyRows(XSSFSheet sheet, List<Row> rows, int startRow) {
        log.debug("startRow:[{}]",startRow);
        if (rows != null) {
            sheet.copyRows(rows, startRow, cellCopyPolicy);
        }
    }

    private static void setBasicInfo(XSSFSheet sheet, EtlMapp etlMapp) {
        String tableEnglishName = etlMapp.getTableEnglishName();
        String tableChineseName = etlMapp.getTableChineseName();
        String primaryKeyField = etlMapp.getPrimaryKeyField();
        String analyst = etlMapp.getAnalyst();
        String attributionLevel = etlMapp.getAttributionLevel();
        String mainApplication = etlMapp.getMainApplication();
        String timeGranularity = etlMapp.getTimeGranularity();
        String creationDate = etlMapp.getCreationDate();
        String attributionTheme = etlMapp.getAttributionTheme();
        String retentionPeriod = etlMapp.getRetentionPeriod();
        String description = etlMapp.getDescription();

        Row row1 = RowUtil.getOrCreateRow(sheet, 3); // 中文名称(*)
        Row row2 = RowUtil.getOrCreateRow(sheet, 4); // 英文名称(*)
        Row row3 = RowUtil.getOrCreateRow(sheet, 5); // 注释
        CellUtil.setCellValue(CellUtil.getCell(row1, 2), tableChineseName);  // 中文名称(*)
        CellUtil.setCellValue(CellUtil.getCell(row1, 4), primaryKeyField);  // 主键字段
        CellUtil.setCellValue(CellUtil.getCell(row1, 7), analyst);  // 分析人员
        CellUtil.setCellValue(CellUtil.getCell(row1, 9), attributionLevel);  // 归属层次
        CellUtil.setCellValue(CellUtil.getCell(row1, 11), timeGranularity);  // 时间粒度
        CellUtil.setCellValue(CellUtil.getCell(row2, 2), tableEnglishName);  // 英文名称(*)
        CellUtil.setCellValue(CellUtil.getCell(row2, 4), mainApplication);  // 主要应用
        CellUtil.setCellValue(CellUtil.getCell(row2, 7), creationDate);  // 创建日期
        CellUtil.setCellValue(CellUtil.getCell(row2, 9), attributionTheme);  // 归属主题
        CellUtil.setCellValue(CellUtil.getCell(row2, 11), retentionPeriod);  // 保留周期
        CellUtil.setCellValue(CellUtil.getCell(row3, 2), description);  // 注释
    }

    private static int processUpdateRecords(XSSFSheet sheet, List<EtlUpdateRecord> etlUpdateRecordList) {
        int sheet_deal_row = 7;
        for (EtlUpdateRecord etlUpdateRecord : etlUpdateRecordList) {
            copyRows(sheet, mapping_map.get("update_blank"), sheet.getPhysicalNumberOfRows());
            String date = StringUtils.defaultString(etlUpdateRecord.getDate(), "");
            String updater = StringUtils.defaultString(etlUpdateRecord.getUpdater(), "");
            String updateRecordDescription = StringUtils.defaultString(etlUpdateRecord.getDescription(), "");
            sheet_deal_row++;
            Row row4 = RowUtil.getOrCreateRow(sheet, sheet_deal_row);
            CellUtil.setCellValue(CellUtil.getCell(row4, 2), date);  // 日期
            CellUtil.setCellValue(CellUtil.getCell(row4, 3), updater);  // 更新人
            CellUtil.setCellValue(CellUtil.getCell(row4, 4), updateRecordDescription);  // 说明
        }
        return sheet_deal_row;
    }

    private static int processGroups(XSSFSheet sheet, List<EtlGroup> etlGroupList,int sheet_deal_row) {
        String etlGroupListSize = num2strmap.get(String.valueOf(etlGroupList.size()));
        for (int j = 0; j < etlGroupList.size(); j++) {
            EtlGroup etlGroup = etlGroupList.get(j);
            String groupId = etlGroup.getGroupId();
            String targetTableEnglishName = etlGroup.getTargetTableEnglishName();
            String targetTableChineseName = etlGroup.getTargetTableChineseName();
            String groupRemarks = etlGroup.getGroupRemarks();
            String templateType = etlGroup.getTemplateType();
            String distributionKey = StringUtils.defaultString(etlGroup.getDistributionKey(), "");
            String filterCondition = etlGroup.getFilterCondition();
            String groupingCondition = etlGroup.getGroupingCondition();
            String sortingCondition = etlGroup.getSortingCondition();

            copyRows(sheet, mapping_map.get("mapp_begin_row"), sheet.getPhysicalNumberOfRows());
            sheet_deal_row++;
            Row row_g_title = RowUtil.getOrCreateRow(sheet, sheet_deal_row); // 字段映射（第?组）
            CellUtil.setCellValue(CellUtil.getCell(row_g_title, 1), "字段映射第" + num2strmap.get(String.valueOf(j + 1)) + "组(共" + etlGroupListSize + "组)");  // 字段映射（第?组）

            sheet_deal_row++;
            Row row4 = RowUtil.getOrCreateRow(sheet, sheet_deal_row); // 中文名称(*)
            CellUtil.setCellValue(CellUtil.getCell(row4, 2), targetTableChineseName);  // 中文名称(*)
            CellUtil.setCellValue(CellUtil.getCell(row4, 4), targetTableEnglishName);  // 英文名称(*)
            CellUtil.setCellValue(CellUtil.getCell(row4, 7), groupRemarks);  // 注释
            CellUtil.setCellValue(CellUtil.getCell(row4, 11), templateType);  // 是否临时表(*)

            List<EtlGroupColMapp> etlGroupColMappList = etlGroup.getEtlGroupColMappList();
            sheet_deal_row += 3;

            for (int k = 0; k < etlGroupColMappList.size(); k++) {
                EtlGroupColMapp etlGroupColMapp = etlGroupColMappList.get(k);
                copyRows(sheet, mapping_map.get("mapp_blank"), sheet.getPhysicalNumberOfRows());
                Row row_col_mapp = RowUtil.getOrCreateRow(sheet, sheet_deal_row); // 字段映射处理行

                String sourceTableSchema = etlGroupColMapp.getSourceTableSchema();
                String sourceTableEnglishName = etlGroupColMapp.getSourceTableEnglishName();
                String sourceTableChineseName = etlGroupColMapp.getSourceTableChineseName();
                String sourceFieldEnglishName = etlGroupColMapp.getSourceFieldEnglishName();
                String sourceFieldChineseName = etlGroupColMapp.getSourceFieldChineseName();
                String sourceSystemCode = etlGroupColMapp.getSourceSystemCode();
                String sourceFieldType = etlGroupColMapp.getSourceFieldType();
                String colMappTargetTableEnglishName = etlGroupColMapp.getTargetTableEnglishName();
                String colMappTargetTableChineseName = etlGroupColMapp.getTargetTableChineseName();
                String targetFieldEnglishName = etlGroupColMapp.getTargetFieldEnglishName();
                String targetFieldChineseName = etlGroupColMapp.getTargetFieldChineseName();
                String targetFieldType = etlGroupColMapp.getTargetFieldType();
                String mappingRule = etlGroupColMapp.getMappingRule();
                String remarks = etlGroupColMapp.getRemarks();

                CellUtil.setCellValue(CellUtil.getCell(row_col_mapp, 1), targetFieldChineseName);  // 字段中文名(*)
                CellUtil.setCellValue(CellUtil.getCell(row_col_mapp, 2), targetFieldEnglishName);  // 字段英文名(*)
                CellUtil.setCellValue(CellUtil.getCell(row_col_mapp, 3), targetFieldType);  // 字段类型
                CellUtil.setCellValue(CellUtil.getCell(row_col_mapp, 4), sourceTableSchema);  // 源表schema
                CellUtil.setCellValue(CellUtil.getCell(row_col_mapp, 5), sourceTableChineseName);  // 源表中文名
                CellUtil.setCellValue(CellUtil.getCell(row_col_mapp, 6), sourceTableEnglishName);  // 源表英文名(*)
                CellUtil.setCellValue(CellUtil.getCell(row_col_mapp, 7), sourceFieldChineseName);  // 源字段中文名
                CellUtil.setCellValue(CellUtil.getCell(row_col_mapp, 8), sourceFieldEnglishName);  // 源字段英文名
                CellUtil.setCellValue(CellUtil.getCell(row_col_mapp, 9), sourceFieldType);  // 源字段类型
                CellUtil.setCellValue(CellUtil.getCell(row_col_mapp, 10), mappingRule);  // 映射规则(*)
                CellUtil.setCellValue(CellUtil.getCell(row_col_mapp, 11), remarks);  // 注释(*)

                sheet_deal_row++;
            }

            copyRows(sheet, mapping_map.get("mapp_blank"), sheet.getPhysicalNumberOfRows());
            sheet_deal_row++;

            copyRows(sheet, mapping_map.get("mapp_dist"), sheet.getPhysicalNumberOfRows());
            Row row_dist = RowUtil.getOrCreateRow(sheet, sheet_deal_row); // 分布键（distributed by）
            CellUtil.setCellValue(CellUtil.getCell(row_dist, 2), distributionKey);  // 分布键（distributed by）

            copyRows(sheet, mapping_map.get("glob_blank"), sheet.getPhysicalNumberOfRows());
            copyRows(sheet, mapping_map.get("join_info"), sheet.getPhysicalNumberOfRows());

            sheet_deal_row += 4;

            List<EtlGroupJoinInfo> etlGroupJoinInfoList = etlGroup.getEtlGroupJoinInfoList();
            for (int k = 0; k < etlGroupJoinInfoList.size(); k++) {
                EtlGroupJoinInfo etlGroupJoinInfo = etlGroupJoinInfoList.get(k);
                copyRows(sheet, mapping_map.get("join_blank"), sheet.getPhysicalNumberOfRows());
                Row row_join_mapp = RowUtil.getOrCreateRow(sheet, sheet_deal_row); // 关联处理行

                String sourceTableSchema = etlGroupJoinInfo.getSourceTableSchema();
                String sourceTableEnglishName = etlGroupJoinInfo.getSourceTableEnglishName();
                String sourceTableChineseName = etlGroupJoinInfo.getSourceTableChineseName();
                String sourceTableAlias = etlGroupJoinInfo.getSourceTableAlias();
                String joinType = etlGroupJoinInfo.getJoinType();
                String joinCondition = etlGroupJoinInfo.getJoinCondition();
                String comment = etlGroupJoinInfo.getComment();

                CellUtil.setCellValue(CellUtil.getCell(row_join_mapp, 1), sourceTableSchema);  // 源表schema
                CellUtil.setCellValue(CellUtil.getCell(row_join_mapp, 2), sourceTableChineseName);  // 源表中文名
                CellUtil.setCellValue(CellUtil.getCell(row_join_mapp, 3), sourceTableEnglishName);  // 源表英文名(*)
                CellUtil.setCellValue(CellUtil.getCell(row_join_mapp, 4), sourceTableAlias);  // 源表别名(*)
                CellUtil.setCellValue(CellUtil.getCell(row_join_mapp, 5), joinType);  // 关联类型
                CellUtil.setCellValue(CellUtil.getCell(row_join_mapp, 6), joinCondition);  // 关联条件（on）
                CellUtil.setCellValue(CellUtil.getCell(row_join_mapp, 11), comment);  // 注释

                sheet_deal_row++;
            }

            copyRows(sheet, mapping_map.get("join_blank"), sheet.getPhysicalNumberOfRows());
            sheet_deal_row++;

            copyRows(sheet, mapping_map.get("join_condition"), sheet.getPhysicalNumberOfRows());
            Row row_where = RowUtil.getOrCreateRow(sheet, sheet_deal_row); // 过滤条件（where）
            CellUtil.setCellValue(CellUtil.getCell(row_where, 2), filterCondition);  // 过滤条件（where）
            sheet_deal_row++;

            Row row_group = RowUtil.getOrCreateRow(sheet, sheet_deal_row); // 分组条件（group by）
            CellUtil.setCellValue(CellUtil.getCell(row_group, 2), groupingCondition);  // 分组条件（group by）
            sheet_deal_row++;

            Row row_sort = RowUtil.getOrCreateRow(sheet, sheet_deal_row); // 排序条件（order by）
            CellUtil.setCellValue(CellUtil.getCell(row_sort, 2), sortingCondition);  // 排序条件（order by）
        }
        sheet_deal_row++;

        return sheet_deal_row;
    }

    private static void processLoadInfo(XSSFSheet sheet, EtlMapp etlMapp,int sheet_deal_row) {

        // 复制加载信息行
        copyRows(sheet, mapping_map.get("load_info"), sheet_deal_row);
        sheet_deal_row++;

        // 设置初始设置
        Row rowInitialSettings = RowUtil.getOrCreateRow(sheet, sheet_deal_row);
        CellUtil.setCellValue(CellUtil.getCell(rowInitialSettings, 2), etlMapp.getInitialSettings());
        sheet_deal_row++;

        // 设置初始加载
        Row rowInitialLoad = RowUtil.getOrCreateRow(sheet, sheet_deal_row);
        CellUtil.setCellValue(CellUtil.getCell(rowInitialLoad, 2), etlMapp.getInitialLoad());
        sheet_deal_row++;

        // 设置每日加载
        Row rowDailyLoad = RowUtil.getOrCreateRow(sheet, sheet_deal_row);
        CellUtil.setCellValue(CellUtil.getCell(rowDailyLoad, 2), etlMapp.getDailyLoad());
        sheet_deal_row++;
    }
}
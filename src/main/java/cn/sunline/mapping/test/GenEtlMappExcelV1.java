package cn.sunline.mapping.test;

import cn.hutool.core.io.FileUtil;
import cn.hutool.poi.excel.RowUtil;
import cn.hutool.poi.excel.cell.CellUtil;
import cn.sunline.mapping.GetMappRows;
import cn.sunline.util.BasicInfo;
import cn.sunline.vo.etl.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.CellCopyPolicy;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.HashMap;
import java.util.List;

import static cn.sunline.mapping.EtlMappingExcelRead.readEtlMappExcel;
import static cn.sunline.mapping.GetMappRows.getNumber2StrMap;

public class GenEtlMappExcelV1 {
    public static final String base_export_path = BasicInfo.getBasicExportPath("映射");
    private static final String MAPP_TPL_PATH = BasicInfo.TPL_PATH + "excel/dml_mapping_template.xlsx";
    private static HashMap<String,String> num2strmap = getNumber2StrMap();
    public static void main(String[] args) {
        List<EtlMapp> etlMappList = readEtlMappExcel("D:\\svn\\jilin\\04.映射设计\\0402.计量模型层\\宝奇订单指标表.xlsx");
        genEtlMappExcel(etlMappList);
    }

    public static void genEtlMappExcel(List<EtlMapp> etlMappList){
        HashMap<String,List<Row>> mapping_map = GetMappRows.getMappingMap(MAPP_TPL_PATH);
        FileInputStream fis = null;
        FileOutputStream out = null;
        XSSFWorkbook wb = null;
        XSSFSheet sheet = null;

        try {
            CellCopyPolicy cellCopyPolicy = new CellCopyPolicy();
            for (EtlMapp etlMapp : etlMappList) {
                int sheet_deal_row = 0;
                String sheetName = etlMapp.getSheetName();
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
                String initialSettings = etlMapp.getInitialSettings();
                String initialLoad = etlMapp.getInitialLoad();
                String dailyLoad = etlMapp.getDailyLoad();
                List<EtlUpdateRecord> etlUpdateRecordList = etlMapp.getEtlUpdateRecordList();


                String temp_deal_file = base_export_path+sheetName+".xlsx";
                FileUtil.copy(MAPP_TPL_PATH,temp_deal_file,true);

                File outFile = new File(temp_deal_file);
                fis = new FileInputStream(temp_deal_file);
                wb = new XSSFWorkbook(fis);
                int tpl_sheet = wb.getSheetIndex("mapping模板");
                int blank_sheet = wb.getSheetIndex("空白模板");
                wb.setSheetName(blank_sheet, sheetName);
                sheet = wb.getSheet(sheetName);

                sheet.copyRows(mapping_map.get("row_gp1"),0,cellCopyPolicy);

                Row row1 = RowUtil.getOrCreateRow(sheet,3); //中文名称(*)
                Row row2 = RowUtil.getOrCreateRow(sheet,4); //英文名称(*)
                Row row3 = RowUtil.getOrCreateRow(sheet,5); //注释
                CellUtil.setCellValue(CellUtil.getCell(row1,2),tableChineseName);  //中文名称(*)
                CellUtil.setCellValue(CellUtil.getCell(row1,4),primaryKeyField);  //主键字段
                CellUtil.setCellValue(CellUtil.getCell(row1,7),analyst);  //分析人员
                CellUtil.setCellValue(CellUtil.getCell(row1,9),attributionLevel);  //归属层次
                CellUtil.setCellValue(CellUtil.getCell(row1,11),timeGranularity);  //时间粒度
                CellUtil.setCellValue(CellUtil.getCell(row2,2),tableEnglishName);  //英文名称(*)
                CellUtil.setCellValue(CellUtil.getCell(row2,4),mainApplication);  //主要应用
                CellUtil.setCellValue(CellUtil.getCell(row2,7),creationDate);  //创建日期
                CellUtil.setCellValue(CellUtil.getCell(row2,9),attributionTheme);  //归属主题
                CellUtil.setCellValue(CellUtil.getCell(row2,11),retentionPeriod);  //保留周期
                CellUtil.setCellValue(CellUtil.getCell(row3,2),description);  //注释

                sheet_deal_row = sheet_deal_row+7;  //定位到第一个更新记录空行
                //System.out.println("etlUpdateRecordList:"+etlUpdateRecordList.size()+"\t"+sheet_deal_row);
                for (EtlUpdateRecord etlUpdateRecord : etlUpdateRecordList) {
                    sheet.copyRows(mapping_map.get("update_blank"),sheet.getPhysicalNumberOfRows(),cellCopyPolicy);
                    String date = StringUtils.defaultString(etlUpdateRecord.getDate(),"");
                    String updater = StringUtils.defaultString(etlUpdateRecord.getUpdater(),"");
                    String updateRecordDescription = StringUtils.defaultString(etlUpdateRecord.getDescription(),"");
                    sheet_deal_row++;
                    Row row4 = RowUtil.getOrCreateRow(sheet,sheet_deal_row);
                    CellUtil.setCellValue(CellUtil.getCell(row4,2),date);  //日期
                    CellUtil.setCellValue(CellUtil.getCell(row4,3),updater);  //更新人
                    CellUtil.setCellValue(CellUtil.getCell(row4,4),updateRecordDescription);  //说明
                }

                List<EtlGroup> etlGroupList = etlMapp.getEtlGroupList();
                String etlGroupListSize = num2strmap.get(String.valueOf(etlGroupList.size()));
                //System.out.println("etlGroupList.size():"+etlGroupList.size());
                for (int j = 0; j < etlGroupList.size(); j++) {
                    EtlGroup etlGroup= etlGroupList.get(j);
                    String groupId = etlGroup.getGroupId();
                    String targetTableEnglishName = etlGroup.getTargetTableEnglishName();
                    String targetTableChineseName = etlGroup.getTargetTableChineseName();
                    String groupRemarks = etlGroup.getGroupRemarks();
                    String templateType = etlGroup.getTemplateType();
                    String distributionKey = StringUtils.defaultString(etlGroup.getDistributionKey(),"");
                    String filterCondition = etlGroup.getFilterCondition();
                    String groupingCondition = etlGroup.getGroupingCondition();
                    String sortingCondition = etlGroup.getSortingCondition();
                    sheet.copyRows(mapping_map.get("mapp_begin_row"),sheet.getPhysicalNumberOfRows(),cellCopyPolicy);
                    sheet_deal_row = sheet_deal_row+1;  //分组的记录 字段映射 所属分组的行 比如字段映射（第一组）
                    //System.out.println("分组的记录 字段映射::"+sheet_deal_row);
                    Row row_g_title = RowUtil.getOrCreateRow(sheet,sheet_deal_row); //字段映射（第?组）
                    CellUtil.setCellValue(CellUtil.getCell(row_g_title,1),"字段映射第"+num2strmap.get(String.valueOf(j+1))+"组(共"+etlGroupListSize+"组)");  //字段映射（第?组）

                    sheet_deal_row = sheet_deal_row+1;  //分组的记录目标表的行
                    Row row4 = RowUtil.getOrCreateRow(sheet,sheet_deal_row); //中文名称(*)
                    CellUtil.setCellValue(CellUtil.getCell(row4,2),targetTableChineseName);  //中文名称(*)
                    CellUtil.setCellValue(CellUtil.getCell(row4,4),targetTableEnglishName);  //英文名称(*)
                    CellUtil.setCellValue(CellUtil.getCell(row4,7),groupRemarks);  //注释
                    CellUtil.setCellValue(CellUtil.getCell(row4,11),templateType);  //是否临时表(*)

                    List<EtlGroupColMapp> etlGroupColMappList = etlGroup.getEtlGroupColMappList();
                    //System.out.println("etlGroupColMappList.size():"+etlGroupColMappList.size());
                    sheet_deal_row = sheet_deal_row+3;  //分组字段映射第一行

                    for (int k = 0; k < etlGroupColMappList.size(); k++) {
                        EtlGroupColMapp etlGroupColMapp = etlGroupColMappList.get(k);
                        sheet.copyRows(mapping_map.get("mapp_blank"),sheet.getPhysicalNumberOfRows(),cellCopyPolicy);
                        Row row_col_mapp = RowUtil.getOrCreateRow(sheet,sheet_deal_row); //字段映射处理行

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
                        //System.out.println("targetFieldChineseName::"+targetFieldChineseName+"\t"+sheet_deal_row+"\t"+etlGroupColMappList.size());
                        CellUtil.setCellValue(CellUtil.getCell(row_col_mapp,1),targetFieldChineseName);  //字段中文名(*)
                        CellUtil.setCellValue(CellUtil.getCell(row_col_mapp,2),targetFieldEnglishName);  //字段英文名(*)
                        CellUtil.setCellValue(CellUtil.getCell(row_col_mapp,3),targetFieldType);  //字段类型
                        CellUtil.setCellValue(CellUtil.getCell(row_col_mapp,4),sourceTableSchema);  //源表schema
                        CellUtil.setCellValue(CellUtil.getCell(row_col_mapp,5),sourceTableChineseName);  //源表中文名
                        CellUtil.setCellValue(CellUtil.getCell(row_col_mapp,6),sourceTableEnglishName);  //源表英文名(*)
                        CellUtil.setCellValue(CellUtil.getCell(row_col_mapp,7),sourceFieldChineseName);  //源字段中文名
                        CellUtil.setCellValue(CellUtil.getCell(row_col_mapp,8),sourceFieldEnglishName);  //源字段英文名
                        CellUtil.setCellValue(CellUtil.getCell(row_col_mapp,9),sourceFieldType);  //源字段类型
                        CellUtil.setCellValue(CellUtil.getCell(row_col_mapp,10),mappingRule);  //映射规则(*)
                        CellUtil.setCellValue(CellUtil.getCell(row_col_mapp,11),remarks);  //注释(*)

                        sheet_deal_row = sheet_deal_row+1;  //每插入一行加1
                    }
                    sheet.copyRows(mapping_map.get("mapp_blank"),sheet.getPhysicalNumberOfRows(),cellCopyPolicy);
                    sheet_deal_row = sheet_deal_row+1;  //每插入一行加1

                    sheet.copyRows(mapping_map.get("mapp_dist"),sheet.getPhysicalNumberOfRows(),cellCopyPolicy);
                    //System.out.println("mapp_dist::"+sheet_deal_row);
                    Row row_dist = RowUtil.getOrCreateRow(sheet,sheet_deal_row); //分布键（distributed by）
                    CellUtil.setCellValue(CellUtil.getCell(row_dist,2),distributionKey);  //分布键（distributed by）

                    sheet.copyRows(mapping_map.get("glob_blank"),sheet.getPhysicalNumberOfRows(),cellCopyPolicy);
                    sheet.copyRows(mapping_map.get("join_info"),sheet.getPhysicalNumberOfRows(),cellCopyPolicy);

                    sheet_deal_row = sheet_deal_row+4;  //表关联信息第一个空白行
                    //System.out.println("join_info::"+sheet_deal_row);
                    List<EtlGroupJoinInfo> etlGroupJoinInfoList = etlGroup.getEtlGroupJoinInfoList();
                    for (int k = 0; k < etlGroupJoinInfoList.size(); k++) {
                        EtlGroupJoinInfo etlGroupJoinInfo = etlGroupJoinInfoList.get(k);
                        sheet.copyRows(mapping_map.get("join_blank"),sheet.getPhysicalNumberOfRows(),cellCopyPolicy);
                        Row row_join_mapp = RowUtil.getOrCreateRow(sheet,sheet_deal_row); //关联处理行

                        String sourceTableSchema = etlGroupJoinInfo.getSourceTableSchema();
                        String sourceTableEnglishName = etlGroupJoinInfo.getSourceTableEnglishName();
                        String sourceTableChineseName = etlGroupJoinInfo.getSourceTableChineseName();
                        String sourceTableAlias = etlGroupJoinInfo.getSourceTableAlias();
                        String joinType = etlGroupJoinInfo.getJoinType();
                        String joinCondition = etlGroupJoinInfo.getJoinCondition();
                        String comment = etlGroupJoinInfo.getComment();

                        CellUtil.setCellValue(CellUtil.getCell(row_join_mapp,1),sourceTableSchema);  //源表schema
                        CellUtil.setCellValue(CellUtil.getCell(row_join_mapp,2),sourceTableChineseName);  //源表中文名
                        CellUtil.setCellValue(CellUtil.getCell(row_join_mapp,3),sourceTableEnglishName);  //源表英文名(*)
                        CellUtil.setCellValue(CellUtil.getCell(row_join_mapp,4),sourceTableAlias);  //源表别名(*)
                        CellUtil.setCellValue(CellUtil.getCell(row_join_mapp,5),joinType);  //关联类型
                        CellUtil.setCellValue(CellUtil.getCell(row_join_mapp,6),joinCondition);  //关联条件（on）
                        CellUtil.setCellValue(CellUtil.getCell(row_join_mapp,11),comment);  //注释

                        sheet_deal_row = sheet_deal_row+1;  //每插入一行加1
                    }

                    sheet.copyRows(mapping_map.get("join_blank"),sheet.getPhysicalNumberOfRows(),cellCopyPolicy);
                    sheet_deal_row = sheet_deal_row+1;  //每插入一行加1

                    sheet.copyRows(mapping_map.get("join_condition"),sheet.getPhysicalNumberOfRows(),cellCopyPolicy);
                    Row row_where = RowUtil.getOrCreateRow(sheet,sheet_deal_row); //过滤条件（where）
                    CellUtil.setCellValue(CellUtil.getCell(row_where,2),filterCondition);  //过滤条件（where）
                    sheet_deal_row = sheet_deal_row+1;  //每插入一行加1

                    Row row_group = RowUtil.getOrCreateRow(sheet,sheet_deal_row); //分组条件（group by）
                    CellUtil.setCellValue(CellUtil.getCell(row_group,2),groupingCondition);  //分组条件（group by）
                    sheet_deal_row = sheet_deal_row+1;  //每插入一行加1

                    Row row_sort = RowUtil.getOrCreateRow(sheet,sheet_deal_row); //排序条件（order by）
                    CellUtil.setCellValue(CellUtil.getCell(row_sort,2),sortingCondition);  //排序条件（order by）
                }

                sheet_deal_row = sheet_deal_row+1;  //每插入一行加1

                sheet.copyRows(mapping_map.get("load_info"),sheet.getPhysicalNumberOfRows(),cellCopyPolicy);
                Row rowInitialSettings = RowUtil.getOrCreateRow(sheet,sheet_deal_row); //初始设置
                CellUtil.setCellValue(CellUtil.getCell(rowInitialSettings,2),initialSettings);  //初始设置
                sheet_deal_row = sheet_deal_row+1;  //每插入一行加1

                Row rowInitialLoad = RowUtil.getOrCreateRow(sheet,sheet_deal_row); //初始加载
                CellUtil.setCellValue(CellUtil.getCell(rowInitialLoad,2),initialLoad);  //初始加载
                sheet_deal_row = sheet_deal_row+1;  //每插入一行加1

                Row rowDailyLoad = RowUtil.getOrCreateRow(sheet,sheet_deal_row); //每日加载
                CellUtil.setCellValue(CellUtil.getCell(rowDailyLoad,2),dailyLoad);  //每日加载
                sheet_deal_row = sheet_deal_row+1;  //每插入一行加1

                System.out.println("生成sheet完成："+sheetName);
                wb.removeSheetAt(wb.getSheetIndex("mapping模板"));
                //wb.removeSheetAt(wb.getSheetIndex("空白模板"));
                out = new FileOutputStream(outFile);

                wb.write(out);
                out.flush();
                out.close();
            }

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}

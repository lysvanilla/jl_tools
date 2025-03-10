package cn.sunline.mapping;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ReUtil;
import cn.sunline.util.BasicInfo;
import cn.sunline.vo.etl.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

import static cn.sunline.mapping.EtlMappingExcelRead.readEtlMappExcel;
import static cn.sunline.util.BasicInfo.TEMPLATE_SETTING;

/**
 * SqlTemplateFiller 类用于根据 Excel 文件中的表结构信息生成 DML SQL 语句。
 * 它会读取 Excel 文件，获取表结构信息，然后根据模板填充数据，最终生成 DML SQL 文件。
 */
@Slf4j
public class DmlTemplateFillerV1 {
    // 定义导出文件的基础路径，通过 BasicInfo 类的方法获取
    public static final String base_export_path = BasicInfo.getBasicExportPath("autocode/dml");
    private static String tpl_path = System.getProperty("user.dir")+"/template/sql/dml/inceptor/";
    private static String main_tpl = new FileReader(tpl_path+"dml_template.sql").readString();
    private static String mapp_n1_tpl = new FileReader(tpl_path+"dml_template_mapping_N1.sql").readString();
    private static String mapp_n2_tpl = new FileReader(tpl_path+"dml_template_mapping_N2.sql").readString();
    private static String mapp_y1_tpl = new FileReader(tpl_path+"dml_template_mapping_Y1.sql").readString();
    private static String mapp_y2_tpl = new FileReader(tpl_path+"dml_template_mapping_Y2.sql").readString();
    private static String mapp_y3_tpl = new FileReader(tpl_path+"dml_template_mapping_Y3.sql").readString();
    private static String mapp_dqc_tpl = new FileReader(tpl_path+"dml_template_mapping_DQC.sql").readString();
    private static String mapp_d1_tpl = new FileReader(tpl_path+"dml_template_mapping_D1.sql").readString();

    /**
     * 程序入口方法，用于测试生成 DML SQL 语句的功能。
     *
     * @param args 命令行参数，此处未使用
     */
    public static void main(String[] args) {
        // 定义 Excel 文件的路径
        // 调用 genDmlSql 方法生成 DML SQL 语句
        genDmlSql("D:\\svn\\jilin\\04.映射设计\\0402.计量模型层\\宝奇订单指标表.xlsx");
    }

    /**
     * 重载的 genDmlSql 方法，接受一个包含参数的 HashMap。
     * 从 HashMap 中获取文件路径，并调用另一个 genDmlSql 方法生成 DML SQL 语句。
     *
     * @param args_map 包含参数的 HashMap，其中应包含 "file_name" 键，对应 Excel 文件的路径
     */
    public void genDmlSql(HashMap<String, String> args_map) {
        // 从 HashMap 中获取文件路径
        String filePath = args_map.get("file_name");
        // 检查文件路径是否为空
        if (filePath == null) {
            // 若为空，记录错误日志
            log.error("args_map中缺少file_name参数");
            return;
        }
        // 调用另一个 genDmlSql 方法生成 DML SQL 语句
        genDmlSql(filePath);
    }

    /**
     * 生成 DML SQL 语句的核心方法。
     * 该方法会读取指定路径的 Excel 文件，获取表结构信息，然后为每个表生成 DML SQL 语句并保存到文件中。
     *
     * @param filePath Excel 文件的路径
     */
    public static void genDmlSql(String filePath) {
        // 检查文件是否存在
        if (!FileUtil.exist(filePath)) {
            // 若文件不存在，记录错误日志
            log.error("file_name参数对应的文件不存在,[{}]", filePath);
            return;
        }

        // 调用 ExcelTableStructureReader 类的 readExcel 方法读取 Excel 文件，获取表结构信息
        List<EtlMapp> etlMappList = readEtlMappExcel(filePath);
        // 检查是否成功获取表结构信息
        if (etlMappList == null) {
            // 若未获取到，记录错误日志
            log.error("读取Excel文件失败，无法获取mapping信息");
            return;
        }

        for (EtlMapp etlMapp:etlMappList){
            String tableEnglishName = StringUtils.lowerCase(etlMapp.getTableEnglishName());
            String tableChineseName = etlMapp.getTableChineseName();
            try {
                // 调用 fillTemplate 方法填充模板，生成 DML SQL 语句
                String filledSql = fillTemplate(etlMapp);
                // 定义导出文件的路径
                String outputPath = base_export_path + tableEnglishName + ".sql";
                // 将生成的 DML SQL 语句写入文件
                FileUtil.writeString(filledSql, outputPath, "UTF-8");
                // 记录成功日志
                log.info("ddl建表语句生成功[{}]-[{}]，输出文件路径: [{}]", tableEnglishName, tableChineseName, outputPath);
            } catch (Exception e) {
                // 若生成过程中出现异常，记录错误日志
                log.error("生成表[{}]-[{}]的DML语句时出错", tableEnglishName, tableChineseName, e);
            }
        }
    }

    /**
     * 根据映射信息填充模板，生成 DML SQL 语句。
     *
     * @param etlMapp 映射信息对象
     * @return 填充后的 DML SQL 语句
     */
    public static String fillTemplate(EtlMapp etlMapp) {
        // 获取表英文名
        String tableEnglishName = etlMapp.getTableEnglishName();
        // 获取表中文名
        String tableChineseName = etlMapp.getTableChineseName();
        String creationDate = etlMapp.getCreationDate();
        String primaryKeyField = etlMapp.getPrimaryKeyField();
        String attributionLevel = etlMapp.getAttributionLevel();  //归属层次
        String attributionTheme = etlMapp.getAttributionTheme();  //归属主题
        String mainApplication = etlMapp.getMainApplication();
        String analyst = etlMapp.getAnalyst();
        String timeGranularity = etlMapp.getTimeGranularity();
        String retentionPeriod = etlMapp.getRetentionPeriod();
        String description = etlMapp.getDescription();
        String tableSchema = TEMPLATE_SETTING.get(attributionLevel);
        if (StringUtils.isBlank(tableSchema)){
            log.error("归属层次[{}]未在配置对应的schema",attributionLevel);
            tableSchema  = "未配置";
        }

        List<String> updateLogList = new ArrayList<>();
        updateLogList.add("");
        List<EtlUpdateRecord> etlUpdateRecordList = etlMapp.getEtlUpdateRecordList();
        for (EtlUpdateRecord etlUpdateRecord : etlUpdateRecordList){
            String date = etlUpdateRecord.getDate();
            String updater = etlUpdateRecord.getUpdater();
            String updateDescription = etlUpdateRecord.getDescription();
            updateLogList.add(date+"    "+updater+"    "+updateDescription);
        }

        List<String> descriptionList = Arrays.asList(description.split("\n"));


        // 替换模板中的表英文名和表中文名
        String mainSql = main_tpl.replaceAll(ReUtil.escape("${belong_level}"),attributionLevel);
        mainSql = mainSql.replaceAll(ReUtil.escape("${target_table_schema}"),tableSchema);
        mainSql = mainSql.replaceAll(ReUtil.escape("${target_table_cn_name}"),tableChineseName);
        mainSql = mainSql.replaceAll(ReUtil.escape("${mapping_analyst}"),analyst);
        mainSql = mainSql.replaceAll(ReUtil.escape("${create_time}"),creationDate);
        mainSql = mainSql.replaceAll(ReUtil.escape("${target_table_en_name}"),tableEnglishName);
        mainSql = mainSql.replaceAll(ReUtil.escape("${primary_key}"),primaryKeyField);
        mainSql = mainSql.replaceAll(ReUtil.escape("${belong_subject}"),attributionTheme);
        mainSql = mainSql.replaceAll(ReUtil.escape("${main_application}"),mainApplication);
        mainSql = mainSql.replaceAll(ReUtil.escape("${time_granule}"),timeGranularity);
        mainSql = mainSql.replaceAll(ReUtil.escape("${retention_period}"),retentionPeriod);
        mainSql = mainSql.replaceAll(ReUtil.escape("${table_comment}"),String.join("\n               ",descriptionList));
        mainSql = mainSql.replaceAll(ReUtil.escape("${update_log}"),String.join("\n               ",updateLogList));

        List<String> group_list = new ArrayList<>();
        List<EtlGroup> etlGroupList = etlMapp.getEtlGroupList();
        for (int i = 0; i < etlGroupList.size(); i++) {
            EtlGroup etlGroup = etlGroupList.get(i);
            String targetTableEnglishName = etlGroup.getTargetTableEnglishName();
            String targetTableChineseName = etlGroup.getTargetTableChineseName();
            String groupRemarks = etlGroup.getGroupRemarks();
            String templateType = etlGroup.getTemplateType();
            String distributionKey = etlGroup.getDistributionKey();

            String filterCondition = etlGroup.getFilterCondition();     //过滤条件
            String groupingCondition = etlGroup.getGroupingCondition();     //分组条件
            String sortingCondition = etlGroup.getSortingCondition();     //排序条件

            String group_mapping_sql = "";
            switch (templateType){
                case "Y1":group_mapping_sql = mapp_y1_tpl;break;
                case "Y2":group_mapping_sql = mapp_y2_tpl;break;
                case "Y3":group_mapping_sql = mapp_y3_tpl;break;
                case "N1":group_mapping_sql = mapp_n1_tpl;break;
                case "N2":group_mapping_sql = mapp_n2_tpl;break;
                case "D1":group_mapping_sql = mapp_d1_tpl;break;
            }

            group_mapping_sql = group_mapping_sql.replaceAll(ReUtil.escape("${target_table_schema}"),tableSchema);
            group_mapping_sql = group_mapping_sql.replaceAll(ReUtil.escape("${target_table_cn_name}"),targetTableChineseName);
            group_mapping_sql = group_mapping_sql.replaceAll(ReUtil.escape("${target_table_comment}"),groupRemarks);
            group_mapping_sql = group_mapping_sql.replaceAll(ReUtil.escape("${target_table_name}"),targetTableEnglishName);
            if (StringUtils.isNotEmpty(filterCondition)){
                filterCondition = "where "+filterCondition;
            }
            if (StringUtils.isNotEmpty(groupingCondition)){
                groupingCondition = "group by "+groupingCondition;
            }
            if (StringUtils.isNotEmpty(sortingCondition)){
                sortingCondition = "order by "+sortingCondition;
            }
            group_mapping_sql = group_mapping_sql.replaceAll(ReUtil.escape("${where_condition}"),ReUtil.escape(filterCondition));
            group_mapping_sql = group_mapping_sql.replaceAll(ReUtil.escape("${groupby_condition}"),groupingCondition);
            group_mapping_sql = group_mapping_sql.replaceAll(ReUtil.escape("${orderby_condition}"),sortingCondition);
            group_mapping_sql = group_mapping_sql.replaceAll(ReUtil.escape("@{db_key}"),distributionKey);

            List<String> target_column_names_list = new ArrayList<>();
            List<String> mapping_detail_list = new ArrayList<>();
            List<String> target_column_names_with_data_type_list = new ArrayList<>();
            List<EtlGroupColMapp> etlGroupColMappList = etlGroup.getEtlGroupColMappList();
            for (int j = 0; j < etlGroupColMappList.size(); j++) {
                EtlGroupColMapp etlGroupColMapp = etlGroupColMappList.get(j);
                String targetFieldEnglishName = etlGroupColMapp.getTargetFieldEnglishName();
                String targetFieldChineseName = etlGroupColMapp.getTargetFieldChineseName();
                String mappingRule = etlGroupColMapp.getMappingRule();
                String sourceFieldChineseName = etlGroupColMapp.getSourceFieldChineseName();
                String sourceFieldEnglishName = etlGroupColMapp.getSourceFieldEnglishName();
                String group_mapping_col_remark = etlGroupColMapp.getRemarks();
                String targetFieldType = etlGroupColMapp.getTargetFieldType();
                if (StringUtils.isEmpty(sourceFieldChineseName)){
                    sourceFieldChineseName = targetFieldChineseName;
                }
                if (StringUtils.isBlank(mappingRule)){
                    mappingRule = sourceFieldEnglishName;
                }
                String mapping_detail_col = mappingRule+" as "+targetFieldEnglishName+"    -- "+sourceFieldChineseName+"  "+group_mapping_col_remark;
                if (mappingRule.contains(" as ")){
                    mapping_detail_col = mappingRule+"    -- "+sourceFieldChineseName+"  "+group_mapping_col_remark;
                }

                target_column_names_list.add(targetFieldEnglishName+"    -- "+targetFieldChineseName);
                mapping_detail_list.add(mapping_detail_col);
                target_column_names_with_data_type_list.add(targetFieldEnglishName+"  "+targetFieldType+"    -- "+targetFieldChineseName);
            }


            List<String> table_relation_list = new ArrayList<>();
            List<EtlGroupJoinInfo> etlGroupJoinInfoList = etlGroup.getEtlGroupJoinInfoList();
            for (int j = 0; j < etlGroupJoinInfoList.size(); j++) {
                EtlGroupJoinInfo etlGroupJoinInfo = etlGroupJoinInfoList.get(j);
                String sourceTableSchema = etlGroupJoinInfo.getSourceTableSchema();
                String sourceTableEnglishName = etlGroupJoinInfo.getSourceTableEnglishName();
                String sourceTableChineseName = etlGroupJoinInfo.getSourceTableChineseName();
                String joinType = etlGroupJoinInfo.getJoinType();
                String sourceTableAlias = etlGroupJoinInfo.getSourceTableAlias();
                String comment = etlGroupJoinInfo.getComment();
                String joinCondition = etlGroupJoinInfo.getJoinCondition();

                String join_table_full_name = sourceTableEnglishName;
                if (StringUtils.isNotEmpty(sourceTableSchema)){
                    join_table_full_name = sourceTableSchema+"."+sourceTableEnglishName;
                }
                join_table_full_name = appendLineStr(join_table_full_name).trim();

                String join_info = "";
                if (StringUtils.isEmpty(joinType)){
                    join_info = "FROM "+ join_table_full_name+" "+sourceTableAlias+"    --"+sourceTableChineseName+"  "+comment;
                }else{
                    join_info = joinType+" " +join_table_full_name+" "+sourceTableAlias+"    --"+sourceTableChineseName+"  "+comment;
                    join_info = join_info+"\n"+appendLineStr(" ON "+joinCondition);
                }

                table_relation_list.add(join_info);
            }


            group_mapping_sql = group_mapping_sql.replace("${target_column_names}",appendLineStr(String.join("\n,",target_column_names_list)));
            group_mapping_sql = group_mapping_sql.replace("${mapping_detail}",appendLineStr(String.join("\n,",mapping_detail_list)));
            group_mapping_sql = group_mapping_sql.replace("${table_relation}",String.join("\n",table_relation_list));
            group_mapping_sql = group_mapping_sql.replace("${target_column_names_with_data_type}",appendLineStr(String.join("\n,",target_column_names_with_data_type_list)));


            //System.out.println(group_mapping_sql);
            group_list.add(group_mapping_sql);

        }


        mainSql = mainSql.replace("${mapping}",String.join("\n",group_list));

        // 返回填充后的 DML SQL 语句
        return mainSql;
    }

    /**
     * 根据 SQL 类型和算法类型获取模板文件名。
     *
     * @param sqlType       SQL 类型，如 "ddl"
     * @param algorithmType 算法类型
     * @return 模板文件名
     */
    public static String getTplName(String sqlType, String algorithmType) {
        // 拼接模板文件名的键
        String tplFileNameKey = String.join("_", sqlType, StringUtils.lowerCase(algorithmType), "tpl");
        // 从模板设置中获取模板文件名
        String tplFileName = TEMPLATE_SETTING.get(tplFileNameKey);
        // 检查模板文件名是否为空
        if (StringUtils.isEmpty(tplFileName)) {
            // 若为空，记录错误日志
            log.error("sql模板查询失败，未找到匹配的模板:[{}]", tplFileNameKey);
        }
        // 返回模板文件名
        return tplFileName;
    }

    public static String appendLineStr(String src_str,String append_str){
        String[] src_str_arr = src_str.split("\n");
        return String.join("\n", ArrayUtil.edit(src_str_arr, t -> append_str+t));
    }

    public static String appendLineStr(String src_str){
        String append_str = "    ";
        return appendLineStr(src_str,append_str);
    }
}

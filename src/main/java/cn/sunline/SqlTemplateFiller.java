package cn.sunline;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.util.ReUtil;
import cn.sunline.util.BasicInfo;
import cn.sunline.vo.TableFieldInfo;
import cn.sunline.vo.TableStructure;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.*;

import static cn.sunline.util.BasicInfo.template_setting;
import static cn.sunline.util.GetTemplateInfo.getCircleLine;

@Slf4j
public class SqlTemplateFiller {
    public static final String base_export_path = BasicInfo.getBasicExportPath("autocode");

    public static void main(String[] args) {
        String filePath = "D:\\svn\\jilin\\03.模型设计\\风险数据集市物理模型-模板.xlsx";

    }

    public void genDdlSql(HashMap<String,String> args_map){
        String file_name=args_map.get("file_name");
        genDdlSql(file_name);
    }

    public static void genDdlSql(String filePath) {
        if (!FileUtil.exist(filePath)){
            log.error("file_name参数对应的文件不存在,[{}]",filePath);
            System.exit(1);
        }

        LinkedHashMap<String, TableStructure> tableMap = ExcelTableStructureReader.readExcel(filePath);

        // 循环 tableMap
        for (Map.Entry<String, TableStructure> entry : tableMap.entrySet()) {
            TableStructure tableStructure = entry.getValue();
            String systemModule = StringUtils.toRootLowerCase(tableStructure.getSystemModule());
            String tableNameEn = StringUtils.toRootLowerCase(tableStructure.getTableNameEn());
            String tableNameCn = StringUtils.toRootLowerCase(tableStructure.getTableNameCn());
            String filledSql = fillTemplate(tableStructure);
            String outputPath = base_export_path +"create_table_"+ tableNameEn +"_.sql";
            FileUtil.writeString(filledSql,outputPath,"UTF-8");
            log.info("ddl建表语句生成功[{}]-[{}]，输出文件路径: [{}]",tableNameEn,tableNameCn, outputPath);
            //System.out.println(filledSql);
        }
    }

    public static String fillTemplate(TableStructure tableStructure) {
        String tpl_file_name_name = getTplName("ddl",tableStructure.getAlgorithmType());
        String tpl_file_name = BasicInfo.tpl_path+tpl_file_name_name;
        String tpl_info =new FileReader(tpl_file_name).readString();
        List<String> circle_line_list = getCircleLine(tpl_file_name);
        String tableNameEn = tableStructure.getTableNameEn();
        String tableNameCn = tableStructure.getTableNameCn();
        LinkedHashMap<String, TableFieldInfo> fieldMap  = tableStructure.getFieldMap();

        // 替换 ${table_name_en} 和 ${table_name_cn}
        String export_sql = tpl_info.replace("${table_name_en}", tableNameEn);
        export_sql = export_sql.replace("${table_name_cn}", tableNameCn);

        // 拼接 ${bucketKey}
        List<String> bucketKeys = new ArrayList<>();
        for (TableFieldInfo field : tableStructure.getFields()) {
            if ("Y".equals(field.getBucketKey())) {
                bucketKeys.add(field.getFieldNameEn());
            }
        }
        String bucketKeyStr = String.join(",", bucketKeys);
        if (StringUtils.isEmpty(bucketKeyStr)){
            log.error("分桶键不允许为空,[{}]-[{}]",tableNameEn,tableNameCn);
        }
        export_sql = export_sql.replace("${bucketKey}", bucketKeyStr);

        for (int i = 0; i < circle_line_list.size(); i++) {
            String circle_line_tpl = circle_line_list.get(i);
            String circle_line_tpl_origin = circle_line_tpl;
            boolean cut_first_occu_flag = false;
            if (circle_line_tpl.contains("column_name_en}")||circle_line_tpl.contains("column_type}")||circle_line_tpl.contains("column_name_cn}")||circle_line_tpl.contains("column_default}")){
                List<String> circle_line_replace_list = new ArrayList<>();
                Iterator iter_col = fieldMap.entrySet().iterator();
                while (iter_col.hasNext()) {
                    Map.Entry entry_col = (Map.Entry) iter_col.next();
                    TableFieldInfo tableFieldInfo = (TableFieldInfo) entry_col.getValue();
                    String fieldNameEn = StringUtils.toRootLowerCase(tableFieldInfo.getFieldNameEn());     //字段英文名
                    String fieldNameCn = StringUtils.toRootLowerCase(tableFieldInfo.getFieldNameCn());     //字段英文名
                    String fieldType = StringUtils.toRootLowerCase(tableFieldInfo.getFieldType());     //字段类型
                    String notNull = StringUtils.toRootLowerCase(tableFieldInfo.getNotNull());     //是否不为空
                    String if_null = "";
                    if ("Y".equals(notNull)){
                        if_null = "not null";
                    }else{
                        if_null = "default null";
                    }


                    String circle_line = circle_line_tpl;
                    circle_line = circle_line.replaceAll("\\@\\{column_name_en}", ReUtil.escape(StringUtils.defaultString(fieldNameEn,"")));
                    circle_line = circle_line.replaceAll("\\@\\{column_name_cn}", ReUtil.escape(StringUtils.defaultString(fieldNameCn,"")));
                    circle_line = circle_line.replaceAll("\\@\\{column_type}", ReUtil.escape(StringUtils.defaultString(fieldType,"")));
                    circle_line = circle_line.replaceAll("\\@\\{if_null}", ReUtil.escape(StringUtils.defaultString(if_null,"")));

                    circle_line_replace_list.add(circle_line);
                }
                String circle_line_info = String.join("\n",circle_line_replace_list);
                export_sql = export_sql.replaceAll(ReUtil.escape(circle_line_tpl_origin),ReUtil.escape(circle_line_info));
            }
        }

        return export_sql;
    }



    public static String getTplName(String sql_type,String algorithmType){
        String tpl_file_name = "";
        List<String> tpl_list = new ArrayList<>();
        tpl_list.add(sql_type);
        tpl_list.add(StringUtils.toRootLowerCase(algorithmType));
        tpl_list.add("tpl");

        String tpl_file_name_key = String.join("_",tpl_list);
        tpl_file_name=template_setting.get(tpl_file_name_key);
        if (StringUtils.isEmpty(tpl_file_name)){
            log.error("sql模板查询:[{}]-[{}]",tpl_file_name_key,tpl_file_name);
        }else{
            //log.info("sql模板查询:[{}]-[{}]",tpl_file_name_key,tpl_file_name);
        }


        return tpl_file_name;
    }
}

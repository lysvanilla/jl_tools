package cn.sunline.util;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;

import java.util.ArrayList;
import java.util.List;

public class GetTemplateInfo {
    private static final String db_type = BasicInfo.getBasicPara("db_type");
    private static String base_sql_tpl_path = System.getProperty("user.dir")+"/config/"+db_type+"/";

    private static List<String> split_str_list = ListUtil.toLinkedList(
             ";"
            , "and "
            , "or "
            , "inner join "
            , "full join "
            , "left join "
            , "right join "
            , "AND "
            , "OR "
            , "INNER JOIN "
            , "FULL JOIN "
            , "LEFT JOIN "
            , "RIGHT JOIN "
            , "||");
    public static void main(String[] args) {
        //String content = "@{itl_column_name} @{itl_column_type} comment '@{itl_column_cn_name}'";
        //List<String> resultFindAll = ReUtil.findAll("\\@\\{[\\d|\\w]*}", content, 0, new ArrayList<String>());
        getCircleLine(base_sql_tpl_path+"itl_table_ddl.sql");

    }

    public static List<String> getCircleLine(String file_name){
        List<String> circle_line_list = new ArrayList<>();
        String list_parameter_format = "\\@\\{[\\d|\\w]*}";
        String tpl_file = new FileReader(file_name).readString();
        String schema_name = file_name.substring(0,3);
        String[] tpl_file_arr = tpl_file.split("\n");
        //System.out.println(tpl_file_arr);
        for (int i = 0; i < tpl_file_arr.length; i++) {
            String line_str = tpl_file_arr[i];
            String line_tmp = ObjectUtil.cloneByStream(line_str);
            if (line_str.contains("@")){
                //List<String> resultFindAll = ReUtil.findAll(list_parameter_format, line_str, 0, new ArrayList<String>());
                //System.out.println("finish!");
                if (!circle_line_list.contains(line_str)){
                    circle_line_list.add(line_str);
                }
            }
        }
        return circle_line_list;
    }

    public static String cutStr(String str){
        String str_rlst = str;
        char blank_char = ' ';
        String str_tmp = str.trim();
        for (int i = 0; i < split_str_list.size(); i++) {
            String split_str = split_str_list.get(i);
            if (str_tmp.startsWith(split_str)){
                //System.out.println(StrUtil.subBefore(str,split_str,false)+StrUtil.subAfter(str,split_str,false));
                return StrUtil.subBefore(str,split_str,false)+StrUtil.fill("",blank_char,split_str.length(),false)+StrUtil.subAfter(str,split_str,false);
            }
        }
        return str_rlst;
    }

    public static String cutddlStr(String str){
        String str_rlst = str;
        char blank_char = ' ';
        String str_tmp = str.trim();
        for (int i = 0; i < split_str_list.size(); i++) {
            String split_str = split_str_list.get(i);
            if (str_tmp.startsWith(split_str)){
                //System.out.println(StrUtil.subBefore(str,split_str,false)+StrUtil.subAfter(str,split_str,false));
                return StrUtil.subBefore(str,split_str,false)+StrUtil.fill("",blank_char,split_str.length(),false)+StrUtil.subAfter(str,split_str,false);
            }
        }
        return str_rlst;
    }

    public static String removeFirstOccurence(String str,char ch){
        int index = str.indexOf(ch);
        if (index == -1){
            return str;
        }else{
            return str.substring(0,index)+" "+str.substring(index+1);
        }
    }



}

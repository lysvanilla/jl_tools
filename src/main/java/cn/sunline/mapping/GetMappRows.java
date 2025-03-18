package cn.sunline.mapping;

import cn.hutool.poi.excel.RowUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GetMappRows {
    private static String workspacePath = System.getProperty("user.dir");
    private static String prePath = workspacePath+"\\template\\excel\\";
    private static String template_file = prePath+"dml_mapping_template.xlsx";

    public static void main(String[] args) {
        HashMap<String,List<Row>> mapping_map = getMappingMap(template_file);
    }

    public static HashMap<String,List<Row>> getMappingMap(String template_excel){
        HashMap<String,List<Row>> mapping_map = new HashMap<>();
        FileInputStream fis = null;
        FileOutputStream out = null;
        XSSFWorkbook wb = null;
        try {
            fis = new FileInputStream(template_excel);
            wb = new XSSFWorkbook(fis);
            XSSFSheet sheet_tpl = wb.getSheet("mapping模板");
            Row row1 = RowUtil.getOrCreateRow(sheet_tpl,0);
            Row row2 = RowUtil.getOrCreateRow(sheet_tpl,1);
            Row row3 = RowUtil.getOrCreateRow(sheet_tpl,2);
            Row row4 = RowUtil.getOrCreateRow(sheet_tpl,3);
            Row row5 = RowUtil.getOrCreateRow(sheet_tpl,4);
            Row row6 = RowUtil.getOrCreateRow(sheet_tpl,5);
            Row row7 = RowUtil.getOrCreateRow(sheet_tpl,6);
            Row row8 = RowUtil.getOrCreateRow(sheet_tpl,7);
            Row row9 = RowUtil.getOrCreateRow(sheet_tpl,8);
            Row row10 = RowUtil.getOrCreateRow(sheet_tpl,9);
            Row row11 = RowUtil.getOrCreateRow(sheet_tpl,10);
            Row row12 = RowUtil.getOrCreateRow(sheet_tpl,11);
            Row row13 = RowUtil.getOrCreateRow(sheet_tpl,12);
            Row row14 = RowUtil.getOrCreateRow(sheet_tpl,13);
            Row row15 = RowUtil.getOrCreateRow(sheet_tpl,14);
            Row row16 = RowUtil.getOrCreateRow(sheet_tpl,15);
            Row row17 = RowUtil.getOrCreateRow(sheet_tpl,16);
            Row row18 = RowUtil.getOrCreateRow(sheet_tpl,17);
            Row row19 = RowUtil.getOrCreateRow(sheet_tpl,18);
            Row row20 = RowUtil.getOrCreateRow(sheet_tpl,19);
            Row row21 = RowUtil.getOrCreateRow(sheet_tpl,20);
            Row row22 = RowUtil.getOrCreateRow(sheet_tpl,21);
            Row row23 = RowUtil.getOrCreateRow(sheet_tpl,22);
            Row row24 = RowUtil.getOrCreateRow(sheet_tpl,23);
            Row row25 = RowUtil.getOrCreateRow(sheet_tpl,24);
            Row row26 = RowUtil.getOrCreateRow(sheet_tpl,25);

            List<Row> menu_row_list = new ArrayList<>();  //返回菜单
            menu_row_list.add(row1);
            mapping_map.put("menu",menu_row_list);

            List<Row> blank_row_list = new ArrayList<>();  //空白分割行
            blank_row_list.add(row2);
            mapping_map.put("glob_blank",blank_row_list);

            List<Row> basic_info_row_list = new ArrayList<>();  //基本信息组
            basic_info_row_list.add(row3);
            basic_info_row_list.add(row4);
            basic_info_row_list.add(row5);
            basic_info_row_list.add(row6);
            mapping_map.put("basic_info",basic_info_row_list);

            List<Row> update_info_row_list = new ArrayList<>();  //更新记录
            update_info_row_list.add(row7);
            update_info_row_list.add(row8);
            mapping_map.put("update_info",update_info_row_list);

            List<Row> update_blank_row_list = new ArrayList<>();  //更新记录空白行
            update_blank_row_list.add(row9);
            mapping_map.put("update_blank",update_blank_row_list);

            List<Row> mapp_begin_row_list = new ArrayList<>();  //字段映射
            mapp_begin_row_list.add(row10);
            mapp_begin_row_list.add(row11);
            mapp_begin_row_list.add(row12);
            mapp_begin_row_list.add(row13);
            mapping_map.put("mapp_begin_row",mapp_begin_row_list);

            List<Row> mapp_blank_row_list = new ArrayList<>();  //字段映射空白行
            mapp_blank_row_list.add(row14);
            mapping_map.put("mapp_blank",mapp_blank_row_list);

            List<Row> mapp_dist_row_list = new ArrayList<>();  //分布键（distributed by）
            mapp_dist_row_list.add(row15);
            mapping_map.put("mapp_dist",mapp_dist_row_list);

            List<Row> join_info_row_list = new ArrayList<>();  //表关联信息
            join_info_row_list.add(row17);
            join_info_row_list.add(row18);
            mapping_map.put("join_info",join_info_row_list);

            List<Row> join_blank_row_list = new ArrayList<>();  //表关联信息空白行
            join_blank_row_list.add(row19);
            mapping_map.put("join_blank",join_blank_row_list);

            List<Row> join_condition_row_list = new ArrayList<>();  //表关联信息筛选行
            join_condition_row_list.add(row20);  //过滤条件（where）
            join_condition_row_list.add(row21);  //分组条件（group by）
            join_condition_row_list.add(row22);  //排序条件（order by）
            mapping_map.put("join_condition",join_condition_row_list);

            List<Row> load_info_row_list = new ArrayList<>();  //加载过程描述
            load_info_row_list.add(row23);  //加载过程描述
            load_info_row_list.add(row24);  //初始设置
            load_info_row_list.add(row25);  //初始加载
            load_info_row_list.add(row26);  //每日加载
            mapping_map.put("load_info",load_info_row_list);

            List<Row> row_list1 = new ArrayList<>();  //第一组
            row_list1.add(row1);
            row_list1.add(row2);
            row_list1.add(row3);
            row_list1.add(row4);
            row_list1.add(row5);
            row_list1.add(row6);
            row_list1.add(row7);
            row_list1.add(row8);
            mapping_map.put("row_gp1",row_list1);



            fis.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
        return mapping_map;
    }

    public static HashMap<String,String> getNumber2StrMap(){
        HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put("1","一");
        hashMap.put("2","二");
        hashMap.put("3","三");
        hashMap.put("4","四");
        hashMap.put("5","五");
        hashMap.put("6","六");
        hashMap.put("7","七");
        hashMap.put("8","八");
        hashMap.put("9","九");
        hashMap.put("10","十");
        hashMap.put("11","十一");
        hashMap.put("12","十二");
        return hashMap;
    }
}

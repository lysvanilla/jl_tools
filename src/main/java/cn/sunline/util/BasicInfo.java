package cn.sunline.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.setting.Setting;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

public class BasicInfo {
    private static String workspacePath = System.getProperty("user.dir");
    public static String base_path = System.getProperty("user.dir")+"/config/";
    public static String base_excel_path = System.getProperty("user.dir")+"/config/excel/";
    public static String base_sql_tpl_path = System.getProperty("user.dir")+"/config/sql_tpl/";
    public static String tpl_path = System.getProperty("user.dir")+"/config/template/";
    public static String help_file = new FileReader(base_path+"help.txt.txt").readString();
    public static String globarConfigPath = workspacePath+"/config/";

    public static final Log log = LogFactory.get("service_log");
    public static String env_type = getBasicPara("env_type");
    public static void main(String[] args) {
        System.out.println(env_type);
        //System.out.println(getBasicExportPath("isl","all_col_compare_sql"));
    }

    public static String getBasicExportPath(String subpath){
        return getBasicExportPath("ddw",subpath);
    }
    public static String getBasicExportPath(String deal_file_sign,String subpath){
        String deal_time = DateUtil.format(DateUtil.date(),"YYYYMMdd_HHmmss").substring(0,8);
        String log_time = DateUtil.format(DateUtil.date(),"YYYYMMdd_HHmmss");

        //Setting setting = new Setting("config/config.txt");
        Setting setting = new Setting(workspacePath+ "/config/config.txt");
        String out_base_path = setting.getStr("out_base_path")+"\\";

        out_base_path = out_base_path+deal_file_sign+"_"+deal_time+"\\";
        String export_file_path = out_base_path+subpath+"\\";
        return export_file_path;
    }

    public static String getTimeInterval(LocalDateTime begin_time,LocalDateTime end_time){
        Duration duration = LocalDateTimeUtil.between(begin_time,end_time);
        long days = duration.toDays();
        long hours = duration.minusDays(days).toHours();
        long minutes = duration.minusHours(hours).toMinutes();
        long seconds = duration.minusHours(hours).minusMinutes(minutes).getSeconds();
        //System.out.printf("%d天 %d小时 %d分钟 %d秒%n",days,hours,minutes,seconds);
        return days+"天 "+hours+"小时 "+minutes+"分钟 "+seconds+"秒";
    }

    public static String getBasicPara(String para_name){
        Setting setting = new Setting(workspacePath+ "/config/config.txt");
        return setting.getStr(para_name);
    }

   /* public static String getTemplate(String para_name){
        return template_setting.getStr(para_name);
    }*/


}

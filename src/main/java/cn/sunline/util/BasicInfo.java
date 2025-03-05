package cn.sunline.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import cn.hutool.setting.Setting;
import cn.idev.excel.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.internal.StringUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
public class BasicInfo {
    private static String separator = File.separator;
    private static String workspacePath = System.getProperty("user.dir");
    public static String base_path = System.getProperty("user.dir")+separator+"config"+separator;
    public static String tpl_path = System.getProperty("user.dir")+separator+"template"+separator;
    public static String help_file = new FileReader(base_path+"help.txt").readString();
    public static String globarConfigPath = workspacePath+"/config/";
    public static String currentDate = DateUtil.format(DateUtil.date(),"YYYYMMdd");
    public static String dist_suffix = DateUtil.format(DateUtil.date(),"MMdd_HHmmss");;
    //public static final Log log = LogFactory.get("service_log");

    public static void main(String[] args) {
        System.out.println(getBasicExportPath(""));
    }

    public static String getBasicExportPath(String subpath){
        return getBasicExportPath("risk",subpath);
    }
    public static String getBasicExportPath(String deal_file_sign,String subpath){
        String deal_time = DateUtil.format(DateUtil.date(),"YYYYMMdd_HHmmss").substring(0,8);
        String log_time = DateUtil.format(DateUtil.date(),"YYYYMMdd_HHmmss");

        Setting setting = new Setting(workspacePath+ "/config/config.txt");
        String out_base_path = setting.getStr("out_base_path")+separator;

        out_base_path = out_base_path+deal_file_sign+"_"+deal_time+separator;
        String export_file_path = out_base_path;
        if (!StringUtils.isEmpty(subpath)){
            export_file_path = out_base_path+subpath+separator;
        }
        FileUtil.mkdir(export_file_path);
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

    public static boolean isDirectoryEmpty(String directoryPath) {
        Path path = Paths.get(directoryPath);
        try {
            if (Files.exists(path) && Files.isDirectory(path)) {
                //log.debug("开始检查目录 {} 是否为空", directoryPath);
                boolean isEmpty = !Files.list(path).findFirst().isPresent();
                /*if (isEmpty) {
                    log.info("目录 {} 为空", directoryPath);
                }*/
                return isEmpty;
            } else {
                log.error("路径 {} 不存在或不是一个有效的目录", directoryPath);
            }
        } catch (IOException e) {
            log.error("检查目录 {} 时发生 I/O 错误", directoryPath, e);
        }
        return false;
    }



}

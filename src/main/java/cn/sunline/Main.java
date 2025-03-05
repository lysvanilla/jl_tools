package cn.sunline;

import cn.sunline.util.BasicInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;

import static cn.sunline.util.ArgsUtil.parseArgs;


@Slf4j
public class Main {
    public static void main(String[] args) {
        String version="202503050907";
        System.out.println("当前编译版本："+version);
        if(args.length == 0 || args[0].equalsIgnoreCase("help")){
            System.out.println(BasicInfo.help_file);  //输出帮助说明
        }else{
            HashMap<String,String> args_map = parseArgs(args);
            String deal_fun = args_map.get("f");
            if(StringUtils.isNotEmpty(deal_fun)) {
                switch (deal_fun) {
                    case "wlh": new ChineseToEnglishTranslator().writeTranslatorExcel(args_map); break;  //物理化翻译
                    case "ddl": new SqlTemplateFiller().genDdlSql(args_map); break;  //ddl建表语句生成
                    case "zb": new IndexExcelWrite().writeIndexExcel(args_map); break;  //风控指标翻译
                    case "cf": new ExcelSheetSplitter().splitExcelSheets(args_map); break;  //拆分EXCEL
                    case "hb": new ExcelMerger().mergeExcelFiles(args_map); break;  //合并EXCEL
                    default: System.out.println("输入的命令不支持，目前只支持下述操作：\n"+BasicInfo.help_file);
                }
            }else{
                log.error("未输入f参数，该参数必输，目前支持下述操作：\n"+BasicInfo.help_file);
                //System.out.println("未输入f参数，该参数必输，目前支持下述操作：\n"+BasicInfo.help_file);
            }
        }
    }
}
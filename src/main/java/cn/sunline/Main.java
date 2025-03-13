package cn.sunline;

import cn.sunline.excel.ExcelMerger;
import cn.sunline.excel.ExcelSheetSplitter;
import cn.sunline.index.IndexExcelWrite;
import cn.sunline.mapping.*;
import cn.sunline.table.ChineseToEnglishTranslator;
import cn.sunline.table.DdlTemplateFiller;
import cn.sunline.util.BasicInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import java.util.HashMap;
import static cn.sunline.util.ArgsUtil.parseArgs;

@Slf4j
public class Main {
    private static final String VERSION = "202503050907";

    public static void main(String[] args) {
        // 输出当前编译版本
        log.info("当前编译版本：{}", VERSION);

        // 处理无参数或 help 参数的情况
        if (args.length == 0 || isHelpArgument(args[0])) {
            printHelpInfo();
            return;
        }

        // 解析命令行参数
        HashMap<String, String> argsMap = parseArgs(args);
        String dealFun = argsMap.get("f");

        // 检查是否提供了 f 参数
        if (StringUtils.isEmpty(dealFun)) {
            log.error("未输入 f 参数，该参数必输，目前支持下述操作：\n{}", BasicInfo.HELP_FILE);
            return;
        }

        // 根据 f 参数的值执行相应的操作
        executeOperation(dealFun, argsMap);
    }

    /**
     * 检查输入的参数是否为 help
     * @param arg 输入的参数
     * @return 如果是 help 返回 true，否则返回 false
     */
    private static boolean isHelpArgument(String arg) {
        return arg.equalsIgnoreCase("help");
    }

    /**
     * 打印帮助信息
     */
    private static void printHelpInfo() {
        System.out.println(BasicInfo.HELP_FILE);
    }

    /**
     * 根据处理函数名执行相应的操作
     * @param dealFun 处理函数名
     * @param argsMap 命令行参数映射
     */
    private static void executeOperation(String dealFun, HashMap<String, String> argsMap) {
        switch (dealFun) {
            case "wlh":  //物理化
                new ChineseToEnglishTranslator().writeTranslatorExcel(argsMap);
                break;
            case "ddl":  //创建DDL建表语句
                new DdlTemplateFiller().genDdlSql(argsMap);
                break;
            case "dml":  //创建DML脚本
                new DmlTemplateFiller().genDmlSqlMain(argsMap);
                break;
            case "gen_mapp":  //接口层映射文档生成
                new TableToEtlMapp().tableToEtlMapp(argsMap);
                break;
            case "gen_table":  //物理模型初稿生成
                new EtlMappToTable().etlMappToTableMain(argsMap);
                break;
            case "supp_mapp":  //补充映射文档模板
                new SupplementMappExcel().supplementMappExcelMain(argsMap);
                break;
            case "update_mapp":  //更新映射文档模板
                new BatchUpdateMappExcel().batchUpdateMappExcelMain(argsMap);
                break;
            case "get_rela_tab":  //获取模型依赖表
                new GetEtlMappTable().getEtlMappTableMain(argsMap);
                break;
            case "zb":  //智能风控系统指标转换成标准模板
                new IndexExcelWrite().writeIndexExcel(argsMap);
                break;
            case "cf":  //EXCEL拆分
                new ExcelSheetSplitter().splitExcelSheets(argsMap);
                break;
            case "hb":   //EXCEL合并
                new ExcelMerger().mergeExcelFiles(argsMap);
                break;
            default:
                log.error("输入的命令不支持，目前只支持下述操作：\n{}", BasicInfo.HELP_FILE);
        }
    }
}
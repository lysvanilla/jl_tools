package cn.sunline;

import cn.sunline.command.Command;
import cn.sunline.command.CommandException;
import cn.sunline.command.CommandFactory;
import cn.sunline.util.BasicInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import java.util.HashMap;

import static cn.sunline.util.ArgsUtil.parseArgs;
import static cn.sunline.util.BasicInfo.verifyLicense;

/**
 * 主程序入口类，负责解析命令行参数并执行相应的命令
 */
@Slf4j
public class Main {
    private static final String VERSION = "202503141826";

    /**
     * 程序入口方法
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        // 使用MDC记录操作上下文
        MDC.put("version", VERSION);

        try {
            // 输出当前编译版本
            log.info("当前版本: {}", VERSION);

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

            // 验证许可证
            verifyLicense();

            // 根据 f 参数的值执行相应的操作
            executeOperation(dealFun, argsMap);
        } catch (Exception e) {
            log.error("程序执行过程中发生异常: {}", e.getMessage(), e);
        } finally {
            // 清理MDC上下文
            MDC.remove("version");
        }
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
        log.info("显示帮助信息");
        System.out.println(BasicInfo.HELP_FILE);
    }

    /**
     * 根据处理函数名执行相应的操作
     * @param dealFun 处理函数名
     * @param argsMap 命令行参数映射
     */
    private static void executeOperation(String dealFun, HashMap<String, String> argsMap) {
        // 从命令工厂获取命令
        Command command = CommandFactory.getCommand(dealFun);

        if (command != null) {
            try {
                // 执行命令
                command.execute(argsMap);
            } catch (CommandException e) {
                log.error("执行命令 '{}' 时发生错误: {}", dealFun, e.getMessage(), e);
            }
        } else {
            log.error("输入的命令 '{}' 不支持，目前只支持下述操作：\n{}", dealFun, BasicInfo.HELP_FILE);
        }
    }
}
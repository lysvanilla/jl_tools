package cn.sunline.command.impl;

import cn.sunline.command.Command;
import cn.sunline.command.CommandException;
import cn.sunline.index.IndexExcelWrite;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.HashMap;

/**
 * 指标Excel写入命令实现
 */
@Slf4j
public class IndexExcelCommand implements Command {
    private static final String CODE = "zb";
    private static final String DESCRIPTION = "智能风控系统指标转换成标准模板";
    
    @Override
    public void execute(HashMap<String, String> args) throws CommandException {
        try {
            // 使用MDC记录操作上下文
            MDC.put("command", CODE);
            MDC.put("operation", DESCRIPTION);
            
            log.info("开始执行{}命令", DESCRIPTION);
            
            // 参数验证
            if (!args.containsKey("file_name")) {
                throw new CommandException("缺少必要参数: file_name");
            }
            
            // 执行命令
            new IndexExcelWrite().writeIndexExcel(args);
            
            log.info("{}命令执行完成", DESCRIPTION);
        } catch (Exception e) {
            log.error("{}命令执行失败: {}", DESCRIPTION, e.getMessage(), e);
            throw new CommandException("执行" + DESCRIPTION + "命令时发生错误: " + e.getMessage(), e);
        } finally {
            // 清理MDC上下文
            MDC.remove("command");
            MDC.remove("operation");
        }
    }
    
    @Override
    public String getCode() {
        return CODE;
    }
    
    @Override
    public String getDescription() {
        return DESCRIPTION;
    }
}

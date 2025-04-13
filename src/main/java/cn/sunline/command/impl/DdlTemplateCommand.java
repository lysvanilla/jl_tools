package cn.sunline.command.impl;

import cn.sunline.command.Command;
import cn.sunline.command.CommandException;
import cn.sunline.table.template.DdlTemplateFiller;
import cn.sunline.table.template.TemplateFillerFactory;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.HashMap;

/**
 * DDL模板填充命令实现
 */
@Slf4j
public class DdlTemplateCommand implements Command {
    private static final String CODE = "ddl";
    private static final String DESCRIPTION = "创建DDL建表语句";

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

            // 使用工厂创建DdlTemplateFiller实例
            DdlTemplateFiller filler = TemplateFillerFactory.createDdlFiller();
            try {
                filler.genDdlSql(args);
            } catch (Exception ex) {
                throw new CommandException("执行DDL生成失败: " + ex.getMessage(), ex);
            }

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

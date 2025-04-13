package cn.sunline.command;

import cn.sunline.command.impl.*;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 命令工厂类，负责创建和管理命令对象
 */
@Slf4j
public class CommandFactory {
    private static final Map<String, Command> commandMap = new HashMap<>();
    
    // 静态初始化块，注册所有命令
    static {
        registerCommands();
    }
    
    /**
     * 注册所有可用的命令
     */
    private static void registerCommands() {
        // 注册所有命令实现
        register(new ChineseToEnglishCommand());
        register(new DdlTemplateCommand());
        register(new DmlTemplateCommand());
        register(new GenMappCommand());
        register(new GenTableCommand());
        register(new StdTableCommand());
        register(new StdMappCommand());
        register(new SuppMappCommand());
        register(new UpdateMappCommand());
        register(new GetRelaTabCommand());
        register(new IndexExcelCommand());
        register(new ExcelSplitCommand());
        register(new ExcelMergeCommand());
        
        log.info("已注册 {} 个命令", commandMap.size());
    }
    
    /**
     * 注册命令
     * @param command 命令对象
     */
    private static void register(Command command) {
        commandMap.put(command.getCode(), command);
        log.debug("注册命令: {} - {}", command.getCode(), command.getDescription());
    }
    
    /**
     * 获取命令
     * @param code 命令代码
     * @return 命令对象，如果不存在则返回null
     */
    public static Command getCommand(String code) {
        Command command = commandMap.get(code);
        if (command == null) {
            log.warn("未找到命令: {}", code);
        }
        return command;
    }
    
    /**
     * 获取所有可用命令
     * @return 命令映射
     */
    public static Map<String, Command> getAllCommands() {
        return new HashMap<>(commandMap);
    }
}

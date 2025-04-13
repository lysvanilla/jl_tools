package cn.sunline.command;

import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 命令帮助类，提供命令相关的辅助方法
 */
@Slf4j
public class CommandHelper {
    
    /**
     * 获取所有命令的代码和描述映射
     * @return 命令代码到描述的映射
     */
    public static Map<String, String> getCommandCodeToDescriptionMap() {
        Map<String, String> codeToDescMap = new LinkedHashMap<>();
        
        for (Command command : CommandFactory.getAllCommands().values()) {
            codeToDescMap.put(command.getCode(), command.getDescription());
        }
        
        return codeToDescMap;
    }
    
    /**
     * 获取所有命令的描述和代码映射
     * @return 命令描述到代码的映射
     */
    public static Map<String, String> getCommandDescriptionToCodeMap() {
        Map<String, String> descToCodeMap = new LinkedHashMap<>();
        
        for (Command command : CommandFactory.getAllCommands().values()) {
            descToCodeMap.put(command.getDescription(), command.getCode());
        }
        
        return descToCodeMap;
    }
    
    /**
     * 根据命令代码获取命令描述
     * @param code 命令代码
     * @return 命令描述，如果命令不存在则返回null
     */
    public static String getDescriptionByCode(String code) {
        Command command = CommandFactory.getCommand(code);
        return command != null ? command.getDescription() : null;
    }
    
    /**
     * 根据命令描述获取命令代码
     * @param description 命令描述
     * @return 命令代码，如果命令不存在则返回null
     */
    public static String getCodeByDescription(String description) {
        for (Command command : CommandFactory.getAllCommands().values()) {
            if (command.getDescription().equals(description)) {
                return command.getCode();
            }
        }
        return null;
    }
}

package cn.sunline.command;

import java.util.HashMap;

/**
 * 命令接口，所有具体命令都需要实现此接口
 */
public interface Command {
    /**
     * 执行命令
     * @param args 命令参数
     * @throws CommandException 命令执行异常
     */
    void execute(HashMap<String, String> args) throws CommandException;
    
    /**
     * 获取命令代码
     * @return 命令代码
     */
    String getCode();
    
    /**
     * 获取命令描述
     * @return 命令描述
     */
    String getDescription();
}

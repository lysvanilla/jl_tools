package cn.sunline.command;

/**
 * 命令执行异常类
 */
public class CommandException extends Exception {
    
    /**
     * 创建一个新的命令执行异常
     * @param message 异常信息
     */
    public CommandException(String message) {
        super(message);
    }
    
    /**
     * 创建一个新的命令执行异常
     * @param message 异常信息
     * @param cause 原始异常
     */
    public CommandException(String message, Throwable cause) {
        super(message, cause);
    }
}

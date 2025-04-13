package cn.sunline.service;

import cn.sunline.command.Command;
import cn.sunline.command.CommandException;
import cn.sunline.command.CommandFactory;
import cn.sunline.command.CommandHelper;
import cn.sunline.constant.AppConstants;
import cn.sunline.exception.BusinessException;
import cn.sunline.vo.Function;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import java.util.*;

/**
 * 基于命令模式的功能服务类
 */
@Slf4j
public class CommandFunctionService {
    // 功能名称到命令代码的映射
    private final Map<String, String> functionNameToCommandCode;
    // 命令代码到功能对象的映射
    private final Map<String, Function> commandCodeToFunction;
    
    /**
     * 构造函数，初始化映射关系
     */
    public CommandFunctionService() {
        // 初始化映射
        functionNameToCommandCode = new HashMap<>();
        commandCodeToFunction = new HashMap<>();
        
        // 获取所有命令
        Map<String, Command> commands = CommandFactory.getAllCommands();
        
        // 获取所有功能
        Map<String, Function> functions = FunctionService.FUNCTION_MAP;
        
        // 建立映射关系
        for (Function function : functions.values()) {
            String functionNameCn = function.getFunctionNameCn();
            String commandCode = function.getFunctionNameEn();
            
            // 检查命令是否存在
            if (commands.containsKey(commandCode)) {
                functionNameToCommandCode.put(functionNameCn, commandCode);
                commandCodeToFunction.put(commandCode, function);
                log.debug("映射功能 '{}' 到命令 '{}'", functionNameCn, commandCode);
            } else {
                log.warn("命令 '{}' 不存在，功能 '{}' 将不可用", commandCode, functionNameCn);
            }
        }
        
        log.info("已初始化 {} 个功能映射", functionNameToCommandCode.size());
    }
    
    /**
     * 获取所有功能名称
     * @return 功能名称列表
     */
    public List<String> getAllFunctionNames() {
        return new ArrayList<>(functionNameToCommandCode.keySet());
    }
    
    /**
     * 获取功能对象
     * @param functionName 功能名称
     * @return 功能对象
     */
    public Function getFunction(String functionName) {
        String commandCode = functionNameToCommandCode.get(functionName);
        if (commandCode != null) {
            return commandCodeToFunction.get(commandCode);
        }
        return null;
    }
    
    /**
     * 执行指定功能
     * @param functionName 功能名称
     * @param fileName 文件名
     * @param modelFileName 模型文件名（可选）
     * @throws BusinessException 业务异常
     */
    public void executeFunction(String functionName, String fileName, String modelFileName) {
        try {
            // 参数验证
            if (StringUtils.isBlank(functionName)) {
                throw new BusinessException(AppConstants.ERROR_FUNCTION_EMPTY);
            }
            if (StringUtils.isBlank(fileName)) {
                throw new BusinessException(AppConstants.ERROR_FILE_NAME_EMPTY);
            }
            
            // 获取命令代码
            String commandCode = functionNameToCommandCode.get(functionName);
            if (commandCode == null) {
                throw new BusinessException(AppConstants.ERROR_FUNCTION_NOT_SUPPORTED + functionName);
            }
            
            // 获取命令对象
            Command command = CommandFactory.getCommand(commandCode);
            if (command == null) {
                throw new BusinessException(AppConstants.ERROR_FUNCTION_NOT_SUPPORTED + functionName);
            }
            
            // 构建参数
            HashMap<String, String> args = new HashMap<>();
            args.put("f", commandCode);
            args.put("file_name", fileName);
            if (StringUtils.isNotBlank(modelFileName)) {
                args.put("model_file_name", modelFileName);
            }
            
            // 使用MDC记录上下文
            MDC.put("command", commandCode);
            MDC.put("operation", command.getDescription());
            
            // 执行命令
            log.info("开始执行命令: {} ({})", command.getDescription(), commandCode);
            command.execute(args);
            log.info("命令执行成功: {} ({})", command.getDescription(), commandCode);
        } catch (CommandException e) {
            log.error("命令执行失败: {}", e.getMessage(), e);
            throw new BusinessException(AppConstants.ERROR_EXECUTION_FAILED + e.getMessage());
        } finally {
            // 清理MDC上下文
            MDC.remove("command");
            MDC.remove("operation");
        }
    }
}

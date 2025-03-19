package cn.sunline.service;

import cn.sunline.constant.AppConstants;
import cn.sunline.exception.BusinessException;
import cn.sunline.function.DefaultFunctionData;
import cn.sunline.vo.Function;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * 功能服务类
 */
@Slf4j
public class FunctionService {
    public static final Map<String, Function> FUNCTION_MAP;
    
    static {
        LinkedHashMap<String, Function> functionMap = DefaultFunctionData.getDefaultFunctions();

        FUNCTION_MAP = Collections.unmodifiableMap(functionMap);
    }

    /**
     * 获取所有功能名称
     */
    public List<String> getAllFunctionNames() {
        return new ArrayList<>(FUNCTION_MAP.keySet());
    }

    /**
     * 执行指定功能
     *
     * @param functionName 功能名称
     * @param fileName 文件名
     * @param modelFileName 模型文件名（可选）
     */
    public void executeFunction(String functionName, String fileName, String modelFileName) {
        if (StringUtils.isBlank(functionName)) {
            throw new BusinessException(AppConstants.ERROR_FUNCTION_EMPTY);
        }
        if (StringUtils.isBlank(fileName)) {
            throw new BusinessException(AppConstants.ERROR_FILE_NAME_EMPTY);
        }

        String functionCode = FUNCTION_MAP.get(functionName).getFunctionNameEn();
        if (functionCode == null) {
            throw new BusinessException(AppConstants.ERROR_FUNCTION_NOT_SUPPORTED + functionName);
        }

        // 构建参数
        Map<String, String> params = new HashMap<>();
        params.put(AppConstants.PARAM_FUNCTION, functionCode);
        params.put(AppConstants.PARAM_FILE_NAME, fileName);
        if (StringUtils.isNotBlank(modelFileName)) {
            params.put(AppConstants.PARAM_MODEL_FILE_NAME, modelFileName);
        }

        try {
            // 调用Main类的处理方法
            cn.sunline.Main.main(buildArgs(params));
            log.info("功能执行成功: {}", functionName);
        } catch (Exception e) {
            log.error("功能执行失败: {}", functionName, e);
            throw new BusinessException(AppConstants.ERROR_EXECUTION_FAILED + e.getMessage());
        }
    }

    /**
     * 构建命令行参数
     */
    private String[] buildArgs(Map<String, String> params) {
        List<String> args = new ArrayList<>();
        params.forEach((key, value) -> args.add(key + "=" + value));
        return args.toArray(new String[0]);
    }
} 
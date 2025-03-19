package cn.sunline.exception;

import lombok.extern.slf4j.Slf4j;

/**
 * 全局异常处理器
 */
@Slf4j
public class ExceptionHandler {
    
    /**
     * 处理业务异常
     */
    public static void handleBusinessException(BusinessException e) {
        log.error("业务异常: code=[{}], message=[{}]", e.getCode(), e.getMessage());
        // 可以添加其他处理逻辑，如发送告警等
    }
    
    /**
     * 处理系统异常
     */
    public static void handleSystemException(Exception e) {
        log.error("系统异常:", e);
        // 可以添加其他处理逻辑，如发送告警等
    }
    
    /**
     * 统一异常处理入口
     */
    public static void handle(Exception e) {
        if (e instanceof BusinessException) {
            handleBusinessException((BusinessException) e);
        } else {
            handleSystemException(e);
        }
    }
} 
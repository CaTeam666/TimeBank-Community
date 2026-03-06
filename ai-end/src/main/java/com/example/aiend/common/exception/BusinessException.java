package com.example.aiend.common.exception;

import lombok.Getter;

/**
 * 业务异常类
 *
 * @author AI-End
 * @since 2025-12-19
 */
@Getter
public class BusinessException extends RuntimeException {
    
    /**
     * 错误码
     */
    private final Integer code;
    
    /**
     * 构造方法
     *
     * @param message 错误消息
     */
    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }
    
    /**
     * 构造方法
     *
     * @param code 错误码
     * @param message 错误消息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}

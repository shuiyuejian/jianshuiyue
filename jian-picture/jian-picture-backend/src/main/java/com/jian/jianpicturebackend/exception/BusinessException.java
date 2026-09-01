package com.jian.jianpicturebackend.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException{

    private final int code;

    /**
     * 直接传错误码和消息
     * @param code 错误码
     * @param message 错误信息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 从 ErrorCode 枚举/对象中取码和消息，统一管理错误定义
     * @param errorCode 错误码
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    /**
     * 用 ErrorCode 的码，但自定义消息（覆盖默认文案）
     * @param errorCode 错误码
     * @param message 错误信息
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }
}

package com.jian.jianpicturebackend.common;

import com.jian.jianpicturebackend.exception.ErrorCode;
import lombok.Data;

import java.io.Serializable;

/**
 * 全局响应封装类
 * @param <T>
 */
@Data
public class BaseResponse<T> implements Serializable {

    private int code;

    private T data;

    private String message;

    /**
     * 成功响应带消息
     * @param code 状态码
     * @param data 数据
     * @param message 消息
     */
    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    /**
     * 成功响应
     * @param code 状态码
     * @param data 数据
     */
    public BaseResponse(int code, T data) {
        this(code, data, "");
    }

    /**
     * 失败响应（从 ErrorCode 取值）
     * @param errorCode 错误码
     */
    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }
}

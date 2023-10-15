package com.xk.usercenter.common;

import lombok.Data;

/**
 * 统一异常返回
 */
@Data
public class BaseResponse<T> {
    private Integer code;
    private T data;
    private String message;
    private String description;

    public BaseResponse(Integer code, T data, String message, String description) {
        this.code = code;
        this.data = data;
        this.message = message;
        this.description = description;
    }

    public BaseResponse(Integer code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public BaseResponse(Integer code, String message, String description) {
        this(code,null,message,description);
    }


    public BaseResponse(Integer code, T data) {
        this.code = code;
        this.data = data;
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(),null, errorCode.getMessage(), errorCode.getDescription());
    }

    public BaseResponse(ErrorCode errorCode,T data, String message, String description) {
        this.code = errorCode.getCode();
        this.message = message;
        this.description = description;
        this.data = data;
    }
}

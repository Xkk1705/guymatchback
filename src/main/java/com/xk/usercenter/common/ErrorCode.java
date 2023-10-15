package com.xk.usercenter.common;

import lombok.Getter;

/**
 * 错误代码
 */
@Getter
public enum ErrorCode {
    PARAMS_ERROR(40000,"请求参数错误",""),
    NULL_ERROR(40001,"请求参数为空",""),
    NOT_LOGIN(41000,"没有登录",""),
    NOT_AUTH(41001,"无权限",""),
    SYSTEM_ERROR(50000,"系统异常","")
    ;

    private  final Integer code;
    private  final String message;
    private  final String description;

    ErrorCode(Integer code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }

}

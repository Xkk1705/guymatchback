package com.xk.usercenter.exception;

import com.xk.usercenter.common.BaseResponse;
import com.xk.usercenter.common.ErrorCode;
import com.xk.usercenter.common.ResultUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常统一处理类
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler{


    /**
     * 处理 BusinessException 异常
     * @param e 异常
     * @return 处理结果
     */
    @ExceptionHandler(BusinessException.class)
    public BaseResponse handlerBusinessException(BusinessException e) {
        log.error("BusinessException" + e.getMessage() , e);
        return ResultUtil.error(e.getCode(),e.getMessage(),e.getDescription());
    }

    /**
     * 处理RuntimeException异常
     * @param e 异常
     * @return 处理结果
     */
    @ExceptionHandler(RuntimeException.class)
    public BaseResponse handlerBaseResponse(RuntimeException e) {
        log.error("RuntimeException", e);
        return ResultUtil.error(ErrorCode.SYSTEM_ERROR,e.getMessage(),"");
    }

}

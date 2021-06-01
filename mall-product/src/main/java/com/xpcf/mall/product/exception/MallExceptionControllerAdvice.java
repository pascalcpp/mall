package com.xpcf.mall.product.exception;

import com.xpcf.common.exception.BizCodeEnum;
import com.xpcf.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * TODO
 *
 * @author XPCF
 * @version 1.0
 * @date 12/18/2020 3:18 AM
 */
//@ControllerAdvice(basePackages = "com.xpcf.mall.product.controller")
@RestControllerAdvice(basePackages = "com.xpcf.mall.product.controller")
@Slf4j
public class MallExceptionControllerAdvice {

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleValidException(MethodArgumentNotValidException e){
        log.error("数据校验出现问题{},异常类{}",e.getMessage(),e.getClass());
        e.printStackTrace();
        Map<String, String> errorMap = new HashMap<>();
        BindingResult bindingResult = e.getBindingResult();
        bindingResult.getFieldErrors().forEach((fieldError)->{
            errorMap.put(fieldError.getField(),fieldError.getDefaultMessage());
        });



        return R.error(BizCodeEnum.VALID_EXCEPTION.getCode(),BizCodeEnum.VALID_EXCEPTION.getMsg()).put("data",errorMap);
    }

    @ExceptionHandler(value = Throwable.class)
    public R handleException(Throwable e){
        log.error("出现问题{},异常类{}",e.getMessage(),e.getClass());
        e.printStackTrace();
        return R.error(BizCodeEnum.UNKNOWN_EXCEPTION.getCode(),BizCodeEnum.UNKNOWN_EXCEPTION.getMsg());
    }
}

package com.v.core.base.advice;

import com.v.core.base.api.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author angry_beard
 * @date 2021/8/17 4:15 下午
 */
@Slf4j
@RestControllerAdvice
public class ExceptionAdvice {

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public R<String> validException(MethodArgumentNotValidException e) {
        ObjectError error = e.getBindingResult().getAllErrors().get(0);
        return R.fail(error.getDefaultMessage());
    }

    @ExceptionHandler({HttpMessageNotReadableException.class})
    public R<String> validRequest(HttpMessageNotReadableException e) {
        log.error("输入参数不合法，detail:", e);
        return R.fail("输入参数不合法");
    }
}

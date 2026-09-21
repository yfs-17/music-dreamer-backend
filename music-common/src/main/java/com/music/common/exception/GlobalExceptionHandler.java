package com.music.common.exception;

import com.music.common.core.R;
import com.music.common.core.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 由 CommonWebAutoConfiguration 注册，且仅在 Servlet 应用中生效。
 * 不要在上面加 @Component —— 否则网关（WebFlux）扫描到它会因缺少 servlet 类而启动失败。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public R<Void> handleBizException(BizException e) {
        log.warn("业务异常: {}", e.getMessage());
        return R.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        return R.fail(ResultCode.PARAM_ERROR.getCode(), resolveFieldMessage(e.getBindingResult().getFieldError()));
    }

    @ExceptionHandler(BindException.class)
    public R<Void> handleBindException(BindException e) {
        return R.fail(ResultCode.PARAM_ERROR.getCode(), resolveFieldMessage(e.getBindingResult().getFieldError()));
    }

    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return R.fail(ResultCode.FAIL);
    }

    private String resolveFieldMessage(FieldError fieldError) {
        return fieldError == null ? ResultCode.PARAM_ERROR.getMsg() : fieldError.getDefaultMessage();
    }
}

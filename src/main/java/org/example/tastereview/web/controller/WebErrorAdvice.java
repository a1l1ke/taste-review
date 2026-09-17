package org.example.tastereview.web.controller;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/** 숫자가 아닌 ID 경로 변수는 404 페이지. 없는 리소스(NotFoundException)는 config.GlobalExceptionHandler가 처리.
 * 스택 트레이스·예외 메시지 노출 금지(REQ-FUNC-013).
 * config.GlobalExceptionHandler의 범용 Exception 처리보다 먼저 매칭되도록 순서를 지정한다. */
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class WebErrorAdvice {

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String typeMismatch() {
        return "error/404";
    }

    /** 매핑 없는 경로. 목록에 없는 경로는 404(REQ-IF-021). */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String noResource() {
        return "error/404";
    }
}

package org.example.tastereview.web.error;

import java.util.NoSuchElementException;

/** 존재하지 않는 리소스 접근 시 발생. config.GlobalExceptionHandler에서 404 페이지로 변환한다. */
public class NotFoundException extends NoSuchElementException {

    public NotFoundException(String messageKey) {
        super(messageKey);
    }
}

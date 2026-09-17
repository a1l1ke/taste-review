package org.example.tastereview.web.controller;

import java.util.List;
import org.example.tastereview.config.AppProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** 모든 화면 공통 모델: 사이트 이름·설명, 카테고리 선택지(REQ-FUNC-001 순서대로). 값은 AppProperties 단일 원천. */
@ControllerAdvice
public class WebCommonAdvice {

    private final AppProperties appProperties;
    private final MessageSource messageSource;

    public WebCommonAdvice(AppProperties appProperties, MessageSource messageSource) {
        this.appProperties = appProperties;
        this.messageSource = messageSource;
    }

    @ModelAttribute("siteName")
    public String siteName() {
        return getSiteName();
    }

    @ModelAttribute("siteDescription")
    public String siteDescription() {
        return getSiteDescription();
    }

    @ModelAttribute("categories")
    public List<String> categories() {
        return getCategories();
    }

    public String getSiteName() {
        return appProperties.getSite().getName();
    }

    public String getSiteDescription() {
        return appProperties.getSite().getDescription();
    }

    public boolean isAllowedCategory(String category) {
        return getCategories().contains(category);
    }

    public List<String> getCategories() {
        return appProperties.getStore().getCategories();
    }

    public String message(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }
}

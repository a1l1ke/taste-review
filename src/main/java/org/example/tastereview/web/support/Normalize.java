package org.example.tastereview.web.support;

import java.util.Locale;

/** 문자열 정규화. SRS-001 1.3절 정의: plain=앞뒤 공백 제거, comparable=모든 공백 제거+소문자. */
public final class Normalize {

    private Normalize() {
    }

    public static String plain(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static String comparable(String value) {
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        return trimmed.replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
    }

    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

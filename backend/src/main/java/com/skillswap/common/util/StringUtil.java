package com.skillswap.common.util;

import java.util.Locale;

import org.springframework.util.StringUtils;

public final class StringUtil {

    private StringUtil() {
    }

    public static String normalizeEmail(String value) {
        return StringUtils.hasText(value) ? value.trim().toLowerCase(Locale.ROOT) : value;
    }
}

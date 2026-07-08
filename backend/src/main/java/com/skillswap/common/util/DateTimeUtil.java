package com.skillswap.common.util;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public final class DateTimeUtil {

    private static final DateTimeFormatter ISO_UTC_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private DateTimeUtil() {
    }

    public static Instant nowUtc() {
        return Instant.now();
    }

    public static String formatUtc(Instant instant) {
        return ISO_UTC_FORMATTER.format(instant.atOffset(ZoneOffset.UTC));
    }
}

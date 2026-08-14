package com.schoolerp.usermanagement.common.util;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public final class DateUtil {
    private DateUtil() {}

    public static final DateTimeFormatter ISO = DateTimeFormatter.ISO_DATE_TIME;

    public static String nowIsoUTC() {
        return OffsetDateTime.now(ZoneOffset.UTC).format(ISO);
    }

    public static String formatIso(LocalDateTime dt) {
        return dt.atOffset(ZoneOffset.UTC).format(ISO);
    }

    public static LocalDateTime parseIso(String text) {
        return OffsetDateTime.parse(text).toLocalDateTime();
    }
}

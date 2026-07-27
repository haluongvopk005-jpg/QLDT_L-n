package com.phonemanager.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

/** Tạo mã nghiệp vụ ngắn, dễ đọc và không trùng khi nhiều tác vụ chạy đồng thời. */
public final class BusinessCodeGenerator {
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private BusinessCodeGenerator() {
    }

    public static String create(String prefix) {
        String safePrefix = prefix == null ? "" : prefix.trim().toUpperCase(Locale.ROOT);
        String randomPart = UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, 6)
                .toUpperCase(Locale.ROOT);
        return safePrefix + LocalDateTime.now().format(TIME_FORMAT) + randomPart;
    }
}

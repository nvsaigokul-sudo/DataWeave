package com.dataweave.backend.domain;

public enum SslMode {
    DISABLE,
    PREFER,
    REQUIRE,
    VERIFY_CA,
    VERIFY_FULL;

    public String jdbcParam() {
        return name().toLowerCase();
    }

    public static SslMode fromString(String value) {
        if (value == null || value.isBlank()) {
            return PREFER;
        }
        return SslMode.valueOf(value.trim().toUpperCase());
    }
}

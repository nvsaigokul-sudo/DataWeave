package com.dataweave.backend.domain;

public record ConnectionTestResult(boolean success, String message, String errorKind) {
    public static ConnectionTestResult ok(String message) {
        return new ConnectionTestResult(true, message, null);
    }

    public static ConnectionTestResult fail(String kind, String message) {
        return new ConnectionTestResult(false, message, kind);
    }
}

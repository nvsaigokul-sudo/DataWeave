package com.dataweave.backend.sql;

public final class SqlIdentifiers {

    private SqlIdentifiers() {}

    public static String quote(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            throw new IllegalArgumentException("Identifier required");
        }
        return "\"" + identifier.replace("\"", "\"\"") + "\"";
    }
}

package com.dataweave.backend.domain;

public record ForeignKeyRef(
        String schema,
        String table,
        String column,
        String referencedSchema,
        String referencedTable,
        String referencedColumn) {}

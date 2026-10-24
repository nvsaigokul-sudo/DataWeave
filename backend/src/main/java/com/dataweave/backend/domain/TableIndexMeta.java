package com.dataweave.backend.domain;

import java.util.List;

public record TableIndexMeta(String schema, String table, String indexName, boolean unique, List<String> columnNames) {}

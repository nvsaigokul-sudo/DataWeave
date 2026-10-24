package com.dataweave.backend.domain;

import java.util.List;

public record TablePrimaryKeyMeta(String schema, String table, List<String> columnNames) {}

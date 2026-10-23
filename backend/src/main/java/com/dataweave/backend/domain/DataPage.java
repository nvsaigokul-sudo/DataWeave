package com.dataweave.backend.domain;

import java.util.List;
import java.util.Map;

public record DataPage(
        List<ColumnMeta> columns,
        List<Map<String, Object>> rows,
        long offset,
        int limit,
        Long totalRowsEstimate,
        boolean hasPrimaryKey,
        String orderingDescription) {}

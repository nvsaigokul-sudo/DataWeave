package com.dataweave.backend.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record DatasetOutputDefinition(
        UUID id,
        UUID projectId,
        UUID connectionId,
        String sourceSchema,
        String sourceTable,
        List<String> outputColumns,
        Instant updatedAt) {}

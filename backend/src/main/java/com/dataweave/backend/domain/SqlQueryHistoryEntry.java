package com.dataweave.backend.domain;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public record SqlQueryHistoryEntry(
        UUID id,
        Optional<UUID> connectionId,
        String sqlText,
        Instant executedAt,
        int rowCount,
        long durationMs,
        Optional<String> errorMessage) {}

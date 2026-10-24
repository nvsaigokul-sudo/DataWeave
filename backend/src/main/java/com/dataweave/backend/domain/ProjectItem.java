package com.dataweave.backend.domain;

import java.time.Instant;
import java.util.UUID;

public record ProjectItem(
        UUID id,
        UUID projectId,
        String itemType,
        UUID refId,
        int sortOrder,
        String label,
        Instant createdAt) {

    public static final String TYPE_DATASET = "dataset";
}

package com.dataweave.backend.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CsvSource(
        UUID id, UUID projectId, String name, String filePath, List<String> headers, Long rowEstimate, Instant createdAt) {}

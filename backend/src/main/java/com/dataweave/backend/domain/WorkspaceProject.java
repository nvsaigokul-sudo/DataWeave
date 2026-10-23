package com.dataweave.backend.domain;

import java.time.Instant;
import java.util.UUID;

public record WorkspaceProject(
        UUID id, String name, String description, Instant createdAt, Instant updatedAt) {}

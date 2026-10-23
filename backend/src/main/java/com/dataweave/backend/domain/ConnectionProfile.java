package com.dataweave.backend.domain;

import java.time.Instant;
import java.util.UUID;

public record ConnectionProfile(
        UUID id,
        String name,
        String host,
        int port,
        String databaseName,
        String username,
        SslMode sslMode,
        String credentialKey,
        int connectTimeoutSeconds,
        Instant createdAt,
        Instant updatedAt) {}

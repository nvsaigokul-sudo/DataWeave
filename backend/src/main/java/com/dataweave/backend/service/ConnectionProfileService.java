package com.dataweave.backend.service;

import com.dataweave.backend.cache.CacheInvalidationService;
import com.dataweave.backend.domain.ConnectionProfile;
import com.dataweave.backend.domain.ConnectionTestResult;
import com.dataweave.backend.domain.SslMode;
import com.dataweave.backend.persistence.ConnectionProfileRepository;
import com.dataweave.backend.persistence.SchemaVersionRepository;
import com.dataweave.backend.postgres.PostgresConnectionManager;
import com.dataweave.backend.security.CredentialVault;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ConnectionProfileService {

    private final ConnectionProfileRepository repository;
    private final CredentialVault vault;
    private final PostgresConnectionManager connectionManager;
    private final SchemaVersionRepository schemaVersionRepository;
    private final CacheInvalidationService cacheInvalidationService;

    public ConnectionProfileService(
            ConnectionProfileRepository repository,
            CredentialVault vault,
            PostgresConnectionManager connectionManager,
            SchemaVersionRepository schemaVersionRepository,
            CacheInvalidationService cacheInvalidationService) {
        this.repository = repository;
        this.vault = vault;
        this.connectionManager = connectionManager;
        this.schemaVersionRepository = schemaVersionRepository;
        this.cacheInvalidationService = cacheInvalidationService;
    }

    public List<ConnectionProfile> list() {
        return repository.findAll();
    }

    public ConnectionProfile update(
            UUID id,
            String name,
            String host,
            int port,
            String database,
            String username,
            char[] passwordOrNull,
            SslMode sslMode,
            int timeoutSeconds) {
        ConnectionProfile existing =
                repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Unknown connection"));
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Connection name is required");
        }
        if (passwordOrNull != null && passwordOrNull.length > 0) {
            vault.store(existing.credentialKey(), passwordOrNull);
        }
        connectionManager.disconnect(id);
        Instant now = Instant.now();
        ConnectionProfile updated =
                new ConnectionProfile(
                        id,
                        name.trim(),
                        host.trim(),
                        port,
                        database.trim(),
                        username.trim(),
                        sslMode,
                        existing.credentialKey(),
                        timeoutSeconds,
                        existing.createdAt(),
                        now);
        repository.update(updated);
        cacheInvalidationService.invalidateConnection(id);
        return updated;
    }

    public ConnectionProfile create(
            String name,
            String host,
            int port,
            String database,
            String username,
            char[] password,
            SslMode sslMode,
            int timeoutSeconds) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Connection name is required");
        }
        UUID id = UUID.randomUUID();
        String credentialKey = "conn/" + id;
        vault.store(credentialKey, password);
        Instant now = Instant.now();
        ConnectionProfile profile =
                new ConnectionProfile(
                        id,
                        name.trim(),
                        host.trim(),
                        port,
                        database.trim(),
                        username.trim(),
                        sslMode,
                        credentialKey,
                        timeoutSeconds,
                        now,
                        now);
        repository.insert(profile);
        return profile;
    }

    public ConnectionTestResult test(UUID id, char[] passwordOverride) {
        ConnectionProfile profile =
                repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Unknown connection"));
        return connectionManager.test(profile, passwordOverride);
    }

    public ConnectionTestResult testNew(ConnectionProfile draft, char[] password) {
        return connectionManager.test(draft, password);
    }

    public boolean hasLivePool(UUID id) {
        return connectionManager.isConnected(id);
    }

    public void delete(UUID id) {
        connectionManager.disconnect(id);
        repository.findById(id).ifPresent(p -> vault.delete(p.credentialKey()));
        repository.delete(id);
        cacheInvalidationService.invalidateConnection(id);
    }

    public long refreshSchema(UUID connectionId) {
        long version = schemaVersionRepository.bump(connectionId);
        cacheInvalidationService.invalidateConnection(connectionId);
        return version;
    }

    public ConnectionProfile require(UUID id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Unknown connection"));
    }
}

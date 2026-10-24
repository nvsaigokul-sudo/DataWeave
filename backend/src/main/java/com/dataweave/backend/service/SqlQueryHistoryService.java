package com.dataweave.backend.service;

import com.dataweave.backend.domain.SqlQueryHistoryEntry;
import com.dataweave.backend.persistence.SqlQueryHistoryRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class SqlQueryHistoryService {

    private final SqlQueryHistoryRepository repository;

    public SqlQueryHistoryService(SqlQueryHistoryRepository repository) {
        this.repository = repository;
    }

    public List<SqlQueryHistoryEntry> listRecent(int limit) {
        return repository.findRecent(limit);
    }

    public SqlQueryHistoryEntry record(
            UUID connectionId, String sql, int rowCount, long durationMs, Optional<String> error) {
        SqlQueryHistoryEntry entry =
                new SqlQueryHistoryEntry(
                        UUID.randomUUID(),
                        Optional.ofNullable(connectionId),
                        sql,
                        Instant.now(),
                        rowCount,
                        durationMs,
                        error);
        repository.insert(entry);
        return entry;
    }
}

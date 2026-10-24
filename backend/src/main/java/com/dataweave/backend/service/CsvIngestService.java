package com.dataweave.backend.service;

import com.dataweave.backend.domain.ColumnMeta;
import com.dataweave.backend.domain.CsvSource;
import com.dataweave.backend.domain.DataPage;
import com.dataweave.backend.persistence.CsvSourceRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class CsvIngestService {

    private static final long MAX_FILE_BYTES = 512L * 1024 * 1024;
    private static final long ROW_SCAN_CAP = 100_000L;
    private static final int PREVIEW_MAX = 500;

    private final CsvSourceRepository repository;

    public CsvIngestService(CsvSourceRepository repository) {
        this.repository = repository;
    }

    public CsvSource register(UUID projectId, String name, Path filePath) throws Exception {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("CSV name is required");
        }
        if (filePath == null || !Files.isRegularFile(filePath)) {
            throw new IllegalArgumentException("CSV file path must exist");
        }
        long size = Files.size(filePath);
        if (size > MAX_FILE_BYTES) {
            throw new IllegalArgumentException("CSV exceeds 512 MB limit");
        }
        List<String> headers = CsvParsing.readHeader(filePath);
        if (headers.isEmpty()) {
            throw new IllegalArgumentException("CSV header row is empty");
        }
        long estimate = CsvParsing.estimateRowCount(filePath, ROW_SCAN_CAP);
        CsvSource source =
                new CsvSource(
                        UUID.randomUUID(),
                        projectId,
                        name.trim(),
                        filePath.toAbsolutePath().normalize().toString(),
                        headers,
                        estimate < 0 ? null : estimate,
                        Instant.now());
        repository.insert(source);
        return source;
    }

    public List<CsvSource> listForProject(UUID projectId) {
        return repository.findByProject(projectId);
    }

    public DataPage preview(UUID sourceId, long offset, int limit) throws Exception {
        CsvSource source = repository.require(sourceId);
        int cappedLimit = Math.min(Math.max(limit, 1), PREVIEW_MAX);
        Path path = Path.of(source.filePath());
        if (!Files.isRegularFile(path)) {
            throw new IllegalStateException("CSV file no longer exists: " + source.filePath());
        }
        List<List<String>> raw = CsvParsing.readPage(path, offset, cappedLimit);
        List<ColumnMeta> columns =
                source.headers().stream().map(h -> new ColumnMeta(h, "text", true)).toList();
        List<Map<String, Object>> rows =
                raw.stream()
                        .map(
                                cells -> {
                                    Map<String, Object> row = new LinkedHashMap<>();
                                    for (int i = 0; i < source.headers().size(); i++) {
                                        String key = source.headers().get(i);
                                        row.put(key, i < cells.size() ? cells.get(i) : null);
                                    }
                                    return row;
                                })
                        .toList();
        Long total = source.rowEstimate();
        return new DataPage(columns, rows, offset, cappedLimit, total, false, "CSV file order");
    }
}

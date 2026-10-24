package com.dataweave.backend.service;

import com.dataweave.backend.domain.DataPage;
import com.dataweave.backend.domain.TableExportResult;
import com.dataweave.backend.domain.TableRef;
import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class TableExportService {

    private static final int PAGE_SIZE = 500;

    private final TableDataService tableDataService;

    public TableExportService(TableDataService tableDataService) {
        this.tableDataService = tableDataService;
    }

    /** Streams all rows unless {@code maxRows} is set (explicit cap — never silent truncation). */
    public TableExportResult exportTableToCsv(UUID connectionId, TableRef table, Path destination, Long maxRows)
            throws Exception {
        long exported = 0;
        long offset = 0;
        boolean truncated = false;
        try (BufferedWriter writer = Files.newBufferedWriter(destination, StandardCharsets.UTF_8)) {
            boolean headerWritten = false;
            List<String> columnNames = List.of();
            while (maxRows == null || exported < maxRows) {
                int pageSize = PAGE_SIZE;
                if (maxRows != null) {
                    pageSize = (int) Math.min(PAGE_SIZE, maxRows - exported);
                    if (pageSize <= 0) {
                        break;
                    }
                }
                DataPage page = tableDataService.fetchPage(connectionId, table, offset, pageSize);
                if (!headerWritten) {
                    columnNames = page.columns().stream().map(c -> c.name()).toList();
                    writer.write(toCsvLine(columnNames));
                    writer.newLine();
                    headerWritten = true;
                }
                if (page.rows().isEmpty()) {
                    break;
                }
                for (Map<String, Object> row : page.rows()) {
                    writer.write(toCsvLine(columnNames.stream().map(row::get).toList()));
                    writer.newLine();
                    exported++;
                    if (maxRows != null && exported >= maxRows) {
                        truncated = page.rows().size() >= pageSize;
                        break;
                    }
                }
                if (page.rows().size() < pageSize) {
                    break;
                }
                offset += page.rows().size();
            }
        }
        return new TableExportResult(exported, truncated, maxRows);
    }

    public long exportTableToCsv(UUID connectionId, TableRef table, Path destination) throws Exception {
        return exportTableToCsv(connectionId, table, destination, null).rowsWritten();
    }

    public static String toCsvLine(List<?> values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(escapeCsv(values.get(i)));
        }
        return sb.toString();
    }

    private static String escapeCsv(Object value) {
        if (value == null) {
            return "";
        }
        String s = String.valueOf(value);
        if (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}

package com.dataweave.backend.service;

import com.dataweave.backend.domain.DataPage;
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
    private static final long MAX_EXPORT_ROWS = 100_000L;

    private final TableDataService tableDataService;

    public TableExportService(TableDataService tableDataService) {
        this.tableDataService = tableDataService;
    }

    public long exportTableToCsv(UUID connectionId, TableRef table, Path destination) throws Exception {
        long exported = 0;
        long offset = 0;
        try (BufferedWriter writer = Files.newBufferedWriter(destination, StandardCharsets.UTF_8)) {
            boolean headerWritten = false;
            List<String> columnNames = List.of();
            while (exported < MAX_EXPORT_ROWS) {
                DataPage page = tableDataService.fetchPage(connectionId, table, offset, PAGE_SIZE);
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
                    if (exported >= MAX_EXPORT_ROWS) {
                        break;
                    }
                }
                if (page.rows().size() < PAGE_SIZE) {
                    break;
                }
                offset += page.rows().size();
            }
        }
        return exported;
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

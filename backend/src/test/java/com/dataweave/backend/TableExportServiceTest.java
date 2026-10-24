package com.dataweave.backend;

import com.dataweave.backend.service.TableExportService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TableExportServiceTest {

    @Test
    void escapesCsvSpecialCharacters() {
        String line = TableExportService.toCsvLine(java.util.List.of("plain", "a,b", "say \"hi\""));
        Assertions.assertEquals("plain,\"a,b\",\"say \"\"hi\"\"\"", line);
    }
}

package com.dataweave.backend;

import com.dataweave.backend.domain.DataPage;
import com.dataweave.backend.service.CsvIngestService;
import com.dataweave.backend.service.WorkspaceProjectService;
import com.dataweave.backend.support.IntegrationTestBase;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CsvIngestServiceTest extends IntegrationTestBase {

    @Autowired WorkspaceProjectService projects;
    @Autowired CsvIngestService csvIngest;

    @Test
    void registersAndPreviewsCsv() throws Exception {
        var project = projects.create("csv", "test");
        Path csv = Files.createTempFile("sample", ".csv");
        Files.writeString(
                csv,
                """
                id,name
                1,alpha
                2,beta
                """);
        var source = csvIngest.register(project.id(), "sample", csv);
        DataPage page = csvIngest.preview(source.id(), 0, 10);
        Assertions.assertEquals(2, page.rows().size());
        Assertions.assertEquals("alpha", page.rows().getFirst().get("name"));
    }
}

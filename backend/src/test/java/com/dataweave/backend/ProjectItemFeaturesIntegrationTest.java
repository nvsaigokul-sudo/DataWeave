package com.dataweave.backend;

import com.dataweave.backend.domain.ProjectItem;
import com.dataweave.backend.service.DatasetOutputDefinitionService;
import com.dataweave.backend.service.ProjectItemService;
import com.dataweave.backend.service.SqlQueryHistoryService;
import com.dataweave.backend.service.WorkspaceProjectService;
import com.dataweave.backend.support.IntegrationTestBase;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ProjectItemFeaturesIntegrationTest extends IntegrationTestBase {

    @Autowired WorkspaceProjectService projects;
    @Autowired DatasetOutputDefinitionService datasets;
    @Autowired ProjectItemService projectItems;
    @Autowired SqlQueryHistoryService queryHistory;

    @Test
    void reorderProjectItemsUpdatesSortOrder() throws Exception {
        var project = projects.create("Reorder", "");
        UUID conn = UUID.randomUUID();
        var def =
                datasets.save(
                        null,
                        project.id(),
                        conn,
                        "public",
                        "t",
                        List.of("id"),
                        "{\"version\":1}");
        ProjectItem a = projectItems.addDatasetReference(project.id(), def.id(), "A");
        var entry = queryHistory.record(conn, "SELECT 1", 1, 5L, java.util.Optional.empty());
        ProjectItem b = projectItems.addQueryReference(project.id(), entry.id(), "B");
        projectItems.reorderItems(project.id(), List.of(b.id(), a.id()));
        var listed = projectItems.listForProject(project.id());
        Assertions.assertEquals(b.id(), listed.get(0).id());
        Assertions.assertEquals(a.id(), listed.get(1).id());
        Assertions.assertEquals(ProjectItem.TYPE_QUERY, listed.get(0).itemType());
    }
}

package com.dataweave.backend;

import com.dataweave.backend.domain.DatasetGraphDefinition;
import com.dataweave.backend.domain.ProjectItem;
import com.dataweave.backend.service.DatasetGraphService;
import com.dataweave.backend.service.DatasetOutputDefinitionService;
import com.dataweave.backend.service.ProjectItemService;
import com.dataweave.backend.service.WorkspaceProjectService;
import com.dataweave.backend.support.IntegrationTestBase;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/** Simulates project link → open definition → graph restored (backend contract). */
class ProjectDatasetNavigationIntegrationTest extends IntegrationTestBase {

    @Autowired WorkspaceProjectService projects;
    @Autowired DatasetOutputDefinitionService datasets;
    @Autowired ProjectItemService projectItems;
    @Autowired DatasetGraphService graphService;

    @Test
    void linkedDatasetDefinitionPreservesGraphJson() throws Exception {
        var project = projects.create("NavTest", "");
        UUID conn = UUID.randomUUID();
        var graph =
                new DatasetGraphDefinition(
                        1,
                        "public",
                        List.of(new DatasetGraphDefinition.DatasetSourceTable("t", "demo_table", 55.0, 66.0)),
                        List.of(),
                        List.of(new DatasetGraphDefinition.DatasetOutputColumn("t.id", "t", "id", "id")),
                        null,
                        null,
                        null,
                        false);
        String json = graphService.serialize(graph);
        var def =
                datasets.save(
                        null,
                        project.id(),
                        conn,
                        "public",
                        "demo_table",
                        List.of("id"),
                        json);
        ProjectItem link = projectItems.addDatasetReference(project.id(), def.id(), "Demo link");
        Assertions.assertEquals(def.id(), link.refId());
        var reloaded = datasets.get(def.id());
        Assertions.assertFalse(reloaded.definitionJson().isBlank());
        var restored = graphService.deserialize(reloaded.definitionJson());
        Assertions.assertEquals(55.0, restored.sourceTables().getFirst().canvasX(), 0.01);
        Assertions.assertThrows(IllegalArgumentException.class, () -> datasets.get(UUID.randomUUID()));
    }
}

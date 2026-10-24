package com.dataweave.backend;

import com.dataweave.backend.domain.ProjectItem;
import com.dataweave.backend.service.DatasetOutputDefinitionService;
import com.dataweave.backend.service.ProjectItemService;
import com.dataweave.backend.service.WorkspaceProjectService;
import com.dataweave.backend.support.IntegrationTestBase;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ProjectDatasetLinkIntegrationTest extends IntegrationTestBase {

    @Autowired WorkspaceProjectService projects;
    @Autowired DatasetOutputDefinitionService datasets;
    @Autowired ProjectItemService projectItems;

    @Test
    void linkDatasetToProjectAndListAfterRestartSimulation() throws Exception {
        var project = projects.create("LinkTest", "");
        UUID conn = UUID.randomUUID();
        var def =
                datasets.save(
                        null,
                        project.id(),
                        conn,
                        "public",
                        "demo",
                        List.of("id"),
                        "{\"version\":1}");
        ProjectItem item = projectItems.addDatasetReference(project.id(), def.id(), "My dataset");
        Assertions.assertEquals(ProjectItem.TYPE_DATASET, item.itemType());
        var listed = projectItems.listForProject(project.id());
        Assertions.assertEquals(1, listed.size());
        Assertions.assertEquals(def.id(), listed.getFirst().refId());
    }
}

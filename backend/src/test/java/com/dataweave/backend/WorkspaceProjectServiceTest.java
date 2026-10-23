package com.dataweave.backend;

import com.dataweave.backend.service.WorkspaceProjectService;
import com.dataweave.backend.support.IntegrationTestBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class WorkspaceProjectServiceTest extends IntegrationTestBase {

    @Autowired WorkspaceProjectService projects;

    @Test
    void createAndListProjects() {
        var created = projects.create("Alpha", "desc");
        var all = projects.list();
        Assertions.assertFalse(all.isEmpty());
        Assertions.assertEquals(created.id(), all.getFirst().id());
    }

    @Test
    void rejectsBlankName() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> projects.create("  ", ""));
    }
}

package com.dataweave.backend;

import com.dataweave.backend.domain.DatasetGraphDefinition;
import com.dataweave.backend.domain.JoinType;
import com.dataweave.backend.service.DatasetGraphService;
import com.dataweave.backend.service.DatasetOutputDefinitionService;
import com.dataweave.backend.service.WorkspaceProjectService;
import com.dataweave.backend.support.IntegrationTestBase;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class DatasetGraphPersistenceIntegrationTest extends IntegrationTestBase {

    @Autowired DatasetGraphService graphService;
    @Autowired DatasetOutputDefinitionService datasets;
    @Autowired WorkspaceProjectService projects;

    @Test
    void roundTripRestoresCanvasPositionsAndJoins() throws Exception {
        var project = projects.create("GraphPersist", "");
        var original =
                new DatasetGraphDefinition(
                        1,
                        "public",
                        List.of(
                                new DatasetGraphDefinition.DatasetSourceTable("o", "orders", 12.5, 88.0),
                                new DatasetGraphDefinition.DatasetSourceTable("c", "customers", 200.0, 40.0)),
                        List.of(
                                new DatasetGraphDefinition.DatasetJoinEdge(
                                        "o", "customer_id", JoinType.RIGHT, "c", "id")),
                        List.of(
                                new DatasetGraphDefinition.DatasetOutputColumn("o.id", "o", "id", "order_id"),
                                new DatasetGraphDefinition.DatasetOutputColumn("c.name", "c", "name", "cust")),
                        "active",
                        "o.status",
                        "o.id",
                        true);
        String json = graphService.serialize(original);
        var def =
                datasets.save(
                        null,
                        project.id(),
                        UUID.randomUUID(),
                        "public",
                        "orders",
                        List.of("order_id", "cust"),
                        json);
        var loaded = datasets.get(def.id());
        DatasetGraphDefinition restored = graphService.deserialize(loaded.definitionJson());
        Assertions.assertEquals(2, restored.sourceTables().size());
        Assertions.assertEquals(88.0, restored.sourceTables().getFirst().canvasY(), 0.01);
        Assertions.assertEquals(JoinType.RIGHT, restored.joins().getFirst().joinType());
        Assertions.assertEquals("active", restored.filterText());
        Assertions.assertTrue(restored.sortDescending());
    }
}

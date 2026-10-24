package com.dataweave.backend;

import com.dataweave.backend.domain.DatasetGraphDefinition;
import com.dataweave.backend.domain.JoinType;
import com.dataweave.backend.service.DatasetGraphValidator;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DatasetGraphValidatorTest {

    private final DatasetGraphValidator validator = new DatasetGraphValidator();

    @Test
    void rejectsDuplicateSourceAliases() {
        var graph =
                new DatasetGraphDefinition(
                        1,
                        "public",
                        List.of(
                                new DatasetGraphDefinition.DatasetSourceTable("a", "t1", 0, 0),
                                new DatasetGraphDefinition.DatasetSourceTable("a", "t2", 1, 1)),
                        List.of(),
                        List.of(),
                        null,
                        null,
                        null,
                        false);
        Assertions.assertTrue(validator.validate(graph).stream().anyMatch(s -> s.contains("Duplicate source alias")));
    }

    @Test
    void rejectsJoinWithUnknownAlias() {
        var graph =
                new DatasetGraphDefinition(
                        1,
                        "public",
                        List.of(new DatasetGraphDefinition.DatasetSourceTable("a", "t1", 0, 0)),
                        List.of(new DatasetGraphDefinition.DatasetJoinEdge("a", "id", JoinType.INNER, "missing", "id")),
                        List.of(),
                        null,
                        null,
                        null,
                        false);
        Assertions.assertTrue(validator.validate(graph).stream().anyMatch(s -> s.contains("unknown")));
    }

    @Test
    void acceptsMinimalValidGraph() {
        var graph =
                new DatasetGraphDefinition(
                        1,
                        "public",
                        List.of(new DatasetGraphDefinition.DatasetSourceTable("a", "orders", 10, 20)),
                        List.of(),
                        List.of(new DatasetGraphDefinition.DatasetOutputColumn("a.id", "a", "id", "order_id")),
                        null,
                        null,
                        null,
                        false);
        Assertions.assertTrue(validator.validate(graph).isEmpty());
    }
}

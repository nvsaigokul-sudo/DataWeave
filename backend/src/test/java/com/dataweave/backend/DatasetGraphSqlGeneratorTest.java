package com.dataweave.backend;

import com.dataweave.backend.domain.DatasetGraphDefinition;
import com.dataweave.backend.domain.JoinType;
import com.dataweave.backend.service.DatasetGraphSqlGenerator;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DatasetGraphSqlGeneratorTest {

    private final DatasetGraphSqlGenerator generator = new DatasetGraphSqlGenerator();

    @Test
    void generatesJoinSelectWithQuotedIdentifiers() {
        var graph =
                new DatasetGraphDefinition(
                        1,
                        "public",
                        List.of(
                                new DatasetGraphDefinition.DatasetSourceTable("a", "orders", 0, 0),
                                new DatasetGraphDefinition.DatasetSourceTable("b", "customers", 200, 0)),
                        List.of(
                                new DatasetGraphDefinition.DatasetJoinEdge(
                                        "a", "customer_id", JoinType.INNER, "b", "id")),
                        List.of(
                                new DatasetGraphDefinition.DatasetOutputColumn(
                                        "a", "a", "id", "order_id")),
                        null,
                        null,
                        null,
                        false);
        String sql = generator.toSelectSql(graph, 100, 0).sql();
        Assertions.assertTrue(sql.contains("INNER JOIN"));
        Assertions.assertTrue(sql.contains("\"orders\""));
        Assertions.assertTrue(sql.contains("\"customer_id\""));
        Assertions.assertTrue(sql.contains("LIMIT 100"));
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.EnumSource(JoinType.class)
    void generatesSqlForEachJoinType(JoinType joinType) {
        var graph =
                new DatasetGraphDefinition(
                        1,
                        "public",
                        List.of(
                                new DatasetGraphDefinition.DatasetSourceTable("a", "orders", 0, 0),
                                new DatasetGraphDefinition.DatasetSourceTable("b", "customers", 0, 0)),
                        List.of(
                                new DatasetGraphDefinition.DatasetJoinEdge(
                                        "a", "customer_id", joinType, "b", "id")),
                        List.of(
                                new DatasetGraphDefinition.DatasetOutputColumn(
                                        "a", "a", "id", "order_id")),
                        null,
                        null,
                        null,
                        false);
        String sql = generator.toSelectSql(graph, 50, 0).sql();
        Assertions.assertTrue(sql.contains(joinType.sqlKeyword()), () -> sql);
    }
}

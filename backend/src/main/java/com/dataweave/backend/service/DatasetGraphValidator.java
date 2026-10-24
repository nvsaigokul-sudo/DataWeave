package com.dataweave.backend.service;

import com.dataweave.backend.domain.DatasetGraphDefinition;
import com.dataweave.backend.domain.DatasetGraphDefinition.DatasetJoinEdge;
import com.dataweave.backend.domain.DatasetGraphDefinition.DatasetOutputColumn;
import com.dataweave.backend.domain.DatasetGraphDefinition.DatasetSourceTable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Validates logical dataset graphs before preview or save. */
public class DatasetGraphValidator {

    public List<String> validate(DatasetGraphDefinition graph) {
        List<String> errors = new ArrayList<>();
        if (graph == null) {
            errors.add("Graph is null");
            return errors;
        }
        if (graph.schema() == null || graph.schema().isBlank()) {
            errors.add("Schema is required");
        }
        List<DatasetSourceTable> sources = graph.sourceTables() == null ? List.of() : graph.sourceTables();
        if (sources.isEmpty()) {
            errors.add("Add at least one source table");
        }
        Set<String> aliases = new HashSet<>();
        for (DatasetSourceTable st : sources) {
            if (st.alias() == null || st.alias().isBlank()) {
                errors.add("Source table alias is required");
            } else if (!aliases.add(st.alias())) {
                errors.add("Duplicate source alias: " + st.alias());
            }
            if (st.tableName() == null || st.tableName().isBlank()) {
                errors.add("Source table name is required for alias " + st.alias());
            }
        }
        Set<String> outputAliases = new HashSet<>();
        for (DatasetOutputColumn col : graph.outputColumns() == null ? List.<DatasetOutputColumn>of() : graph.outputColumns()) {
            String out = col.outputAlias() != null && !col.outputAlias().isBlank() ? col.outputAlias() : col.columnName();
            if (out != null && !outputAliases.add(out)) {
                errors.add("Duplicate output alias: " + out);
            }
            if (col.sourceAlias() == null || !aliases.contains(col.sourceAlias())) {
                errors.add("Output column references unknown table alias: " + col.sourceAlias());
            }
        }
        for (DatasetJoinEdge edge : graph.joins() == null ? List.<DatasetJoinEdge>of() : graph.joins()) {
            if (!aliases.contains(edge.leftAlias())) {
                errors.add("Join left alias unknown: " + edge.leftAlias());
            }
            if (!aliases.contains(edge.rightAlias())) {
                errors.add("Join right alias unknown: " + edge.rightAlias());
            }
            if (edge.leftColumn() == null || edge.leftColumn().isBlank()) {
                errors.add("Join left key is required");
            }
            if (edge.rightColumn() == null || edge.rightColumn().isBlank()) {
                errors.add("Join right key is required");
            }
            if (edge.joinType() == null) {
                errors.add("Join type is required");
            }
        }
        return errors;
    }

    public void requireValid(DatasetGraphDefinition graph) {
        List<String> errors = validate(graph);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join("; ", errors));
        }
    }
}

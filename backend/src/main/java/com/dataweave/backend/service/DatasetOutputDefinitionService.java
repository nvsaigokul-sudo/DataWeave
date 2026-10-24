package com.dataweave.backend.service;

import com.dataweave.backend.domain.DatasetOutputDefinition;
import com.dataweave.backend.persistence.DatasetOutputDefinitionRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class DatasetOutputDefinitionService {

    private final DatasetOutputDefinitionRepository repository;

    public DatasetOutputDefinitionService(DatasetOutputDefinitionRepository repository) {
        this.repository = repository;
    }

    public List<DatasetOutputDefinition> listForProject(UUID projectId) {
        return repository.findByProject(projectId);
    }

    public DatasetOutputDefinition save(
            UUID idOrNull,
            UUID projectId,
            UUID connectionId,
            String schema,
            String table,
            List<String> outputColumns)
            throws Exception {
        return save(idOrNull, projectId, connectionId, schema, table, outputColumns, null);
    }

    public DatasetOutputDefinition save(
            UUID idOrNull,
            UUID projectId,
            UUID connectionId,
            String schema,
            String table,
            List<String> outputColumns,
            String definitionJson)
            throws Exception {
        UUID id = idOrNull == null ? UUID.randomUUID() : idOrNull;
        DatasetOutputDefinition def =
                new DatasetOutputDefinition(
                        id,
                        projectId,
                        connectionId,
                        schema,
                        table,
                        List.copyOf(outputColumns),
                        definitionJson,
                        Instant.now());
        repository.upsert(def);
        return def;
    }

    public void delete(UUID id) {
        repository.delete(id);
    }
}

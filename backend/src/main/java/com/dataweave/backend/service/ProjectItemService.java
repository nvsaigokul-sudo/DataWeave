package com.dataweave.backend.service;

import com.dataweave.backend.domain.ProjectItem;
import com.dataweave.backend.domain.SqlQueryHistoryEntry;
import com.dataweave.backend.persistence.ProjectItemRepository;
import com.dataweave.backend.service.SqlQueryHistoryService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ProjectItemService {

    private final ProjectItemRepository repository;
    private final DatasetOutputDefinitionService datasetService;
    private final SqlQueryHistoryService queryHistoryService;

    public ProjectItemService(
            ProjectItemRepository repository,
            DatasetOutputDefinitionService datasetService,
            SqlQueryHistoryService queryHistoryService) {
        this.repository = repository;
        this.datasetService = datasetService;
        this.queryHistoryService = queryHistoryService;
    }

    public List<ProjectItem> listForProject(UUID projectId) {
        return repository.findByProject(projectId);
    }

    public ProjectItem addDatasetReference(UUID projectId, UUID datasetDefinitionId, String label) {
        var def = datasetService.get(datasetDefinitionId);
        if (!def.projectId().equals(projectId)) {
            throw new IllegalArgumentException("Dataset does not belong to this project");
        }
        int order = repository.findByProject(projectId).size();
        ProjectItem item =
                new ProjectItem(
                        UUID.randomUUID(),
                        projectId,
                        ProjectItem.TYPE_DATASET,
                        datasetDefinitionId,
                        order,
                        label == null || label.isBlank() ? "Dataset" : label.trim(),
                        Instant.now());
        repository.insert(item);
        return item;
    }

    public void removeItem(UUID itemId) {
        repository.delete(itemId);
    }

    public ProjectItem addQueryReference(UUID projectId, UUID historyEntryId, String label) {
        SqlQueryHistoryEntry entry =
                queryHistoryService.listRecent(500).stream()
                        .filter(e -> e.id().equals(historyEntryId))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Query history entry not found: " + historyEntryId));
        int order = repository.findByProject(projectId).size();
        ProjectItem item =
                new ProjectItem(
                        UUID.randomUUID(),
                        projectId,
                        ProjectItem.TYPE_QUERY,
                        entry.id(),
                        order,
                        label == null || label.isBlank() ? "Query" : label.trim(),
                        Instant.now());
        repository.insert(item);
        return item;
    }

    public void reorderItems(UUID projectId, List<UUID> itemIdsInOrder) {
        for (int i = 0; i < itemIdsInOrder.size(); i++) {
            repository.updateSortOrder(itemIdsInOrder.get(i), i);
        }
    }
}

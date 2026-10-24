package com.dataweave.backend.service;

import com.dataweave.backend.domain.ProjectItem;
import com.dataweave.backend.persistence.ProjectItemRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ProjectItemService {

    private final ProjectItemRepository repository;
    private final DatasetOutputDefinitionService datasetService;

    public ProjectItemService(ProjectItemRepository repository, DatasetOutputDefinitionService datasetService) {
        this.repository = repository;
        this.datasetService = datasetService;
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
}

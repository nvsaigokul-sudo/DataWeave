package com.dataweave.backend.service;

import com.dataweave.backend.domain.WorkspaceProject;
import com.dataweave.backend.persistence.WorkspaceProjectRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class WorkspaceProjectService {

    private final WorkspaceProjectRepository repository;

    public WorkspaceProjectService(WorkspaceProjectRepository repository) {
        this.repository = repository;
    }

    public List<WorkspaceProject> list() {
        return repository.findAllOrderByUpdated();
    }

    public WorkspaceProject create(String name, String description) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Project name is required");
        }
        Instant now = Instant.now();
        WorkspaceProject project =
                new WorkspaceProject(UUID.randomUUID(), name.trim(), description == null ? "" : description.trim(), now, now);
        repository.insert(project);
        return project;
    }

    public WorkspaceProject update(UUID id, String name, String description) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Project name is required");
        }
        var existing =
                repository
                        .findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Project not found: " + id));
        Instant now = Instant.now();
        repository.update(id, name.trim(), description == null ? "" : description.trim(), now);
        return new WorkspaceProject(existing.id(), name.trim(), description == null ? "" : description.trim(), existing.createdAt(), now);
    }

    public WorkspaceProject duplicate(UUID id) {
        var existing =
                repository
                        .findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Project not found: " + id));
        String copyName = existing.name() + " (copy)";
        return create(copyName, existing.description());
    }

    public void delete(UUID id) {
        if (repository.findById(id).isEmpty()) {
            throw new IllegalArgumentException("Project not found: " + id);
        }
        repository.delete(id);
    }
}

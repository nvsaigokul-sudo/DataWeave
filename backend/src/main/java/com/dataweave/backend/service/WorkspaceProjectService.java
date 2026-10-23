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
}

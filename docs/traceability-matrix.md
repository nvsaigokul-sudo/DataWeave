# Requirements traceability matrix

| Req ID | Requirement | Design | Implementation | Tests |
|--------|-------------|--------|----------------|-------|
| FR-10-01 | Create project | `ProjectService.create` | `ProjectRepository.insert` | `ProjectRepositoryTest`, `ProjectServiceTest` |
| FR-10-02 | List projects by recency | SQL `ORDER BY updated_at DESC` | `ProjectRepository.list` | `ProjectRepositoryTest` |
| FR-10-03 | Project UI | `ProjectWorkspace` | Compose form + list | Manual GUI (Windows), headless smoke |
| NFR-17-01 | Offline project CRUD | ADR 001 | No network in v0.1 path | Headless smoke |
| NFR-22-01 | Schema migration | Flyway V1 | `V1__init.sql` | Integration tests |
| NFR-32-01 | Automated regression | Gradle test task | `./gradlew test` | CI (future) |

Update this matrix each milestone.

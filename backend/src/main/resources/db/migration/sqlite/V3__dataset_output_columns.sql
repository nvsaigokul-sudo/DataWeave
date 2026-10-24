CREATE TABLE dataset_output_definitions (
    id TEXT PRIMARY KEY,
    project_id TEXT NOT NULL,
    connection_id TEXT NOT NULL,
    source_schema TEXT NOT NULL,
    source_table TEXT NOT NULL,
    columns_json TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    FOREIGN KEY (project_id) REFERENCES workspace_projects(id)
);

CREATE INDEX idx_dataset_output_project ON dataset_output_definitions(project_id);

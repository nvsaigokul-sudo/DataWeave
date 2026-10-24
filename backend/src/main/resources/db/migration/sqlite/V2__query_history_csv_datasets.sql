CREATE TABLE sql_query_history (
    id TEXT PRIMARY KEY,
    connection_id TEXT,
    sql_text TEXT NOT NULL,
    executed_at TEXT NOT NULL,
    row_count INTEGER NOT NULL DEFAULT 0,
    duration_ms INTEGER NOT NULL DEFAULT 0,
    error_message TEXT
);

CREATE INDEX idx_sql_query_history_executed ON sql_query_history(executed_at DESC);

CREATE TABLE csv_sources (
    id TEXT PRIMARY KEY,
    project_id TEXT NOT NULL,
    name TEXT NOT NULL,
    file_path TEXT NOT NULL,
    headers_json TEXT NOT NULL,
    row_estimate INTEGER,
    created_at TEXT NOT NULL,
    FOREIGN KEY (project_id) REFERENCES workspace_projects(id)
);

CREATE INDEX idx_csv_sources_project ON csv_sources(project_id);

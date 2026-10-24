package com.dataweave.backend.domain;

public record TableExportResult(long rowsWritten, boolean truncated, Long rowLimitApplied) {}

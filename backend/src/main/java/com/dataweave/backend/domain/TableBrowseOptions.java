package com.dataweave.backend.domain;

/** Optional server-side sort/filter for paginated table reads. */
public record TableBrowseOptions(
        String sortColumn, boolean sortDescending, String filterText, String filterColumn) {

    public static TableBrowseOptions none() {
        return new TableBrowseOptions(null, false, null, null);
    }

    public boolean hasSort() {
        return sortColumn != null && !sortColumn.isBlank();
    }

    public boolean hasFilter() {
        return filterText != null && !filterText.isBlank();
    }

    public boolean hasColumnFilter() {
        return filterColumn != null && !filterColumn.isBlank();
    }
}

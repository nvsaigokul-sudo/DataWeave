package com.dataweave.backend.service;

import com.dataweave.backend.domain.DataPage;
import com.dataweave.backend.domain.TableRef;
import java.util.UUID;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class TableDataCacheGateway {

    private final TableDataService tableDataService;

    public TableDataCacheGateway(TableDataService tableDataService) {
        this.tableDataService = tableDataService;
    }

    @Cacheable(
            cacheNames = "dataPages",
            key =
                    "#connectionId + ':' + #table.schema + '.' + #table.name + ':' + #offset + ':' + #limit + ':' + #options + ':' + @tableDataCacheGateway.schemaVersion(#connectionId)")
    public DataPage getPage(
            UUID connectionId,
            TableRef table,
            long offset,
            int limit,
            com.dataweave.backend.domain.TableBrowseOptions options)
            throws Exception {
        return tableDataService.fetchPage(connectionId, table, offset, limit, options);
    }

    public long schemaVersion(UUID connectionId) {
        return tableDataService.schemaVersion(connectionId);
    }
}

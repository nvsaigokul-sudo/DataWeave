package com.dataweave.app.ui.datasets

import com.dataweave.backend.domain.DatasetGraphDefinition
import com.dataweave.backend.domain.JoinType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DatasetJoinEdgeLayoutTest {
    @Test
    fun buildsSegmentBetweenTableCenters() {
        val tables =
            listOf(
                DatasetGraphDefinition.DatasetSourceTable("a", "t1", 0.0, 0.0),
                DatasetGraphDefinition.DatasetSourceTable("b", "t2", 200.0, 0.0),
            )
        val joins =
            listOf(
                DatasetGraphDefinition.DatasetJoinEdge("a", "id", JoinType.INNER, "b", "id"),
            )
        val segs = DatasetJoinEdgeLayout.segments(tables, joins, cardWidthPx = 160f, cardHeightPx = 80f)
        assertEquals(1, segs.size)
        assertEquals(80f, segs.first().from.x)
        assertEquals(280f, segs.first().to.x)
        assertEquals("INNER", segs.first().joinLabel)
    }

    @Test
    fun skipsEdgesWithMissingAlias() {
        val tables = listOf(DatasetGraphDefinition.DatasetSourceTable("a", "t1", 0.0, 0.0))
        val joins = listOf(DatasetGraphDefinition.DatasetJoinEdge("a", "id", JoinType.LEFT, "missing", "id"))
        assertTrue(DatasetJoinEdgeLayout.segments(tables, joins, 160f, 80f).isEmpty())
    }
}

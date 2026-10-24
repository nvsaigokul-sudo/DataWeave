package com.dataweave.app.ui.datasets

import com.dataweave.backend.domain.DatasetGraphDefinition

/** Computes canvas line segments for visual join edges (unit-tested). */
object DatasetJoinEdgeLayout {
    data class Point(val x: Float, val y: Float)

    data class Segment(
        val from: Point,
        val to: Point,
        val joinLabel: String,
    )

    fun segments(
        tables: List<DatasetGraphDefinition.DatasetSourceTable>,
        joins: List<DatasetGraphDefinition.DatasetJoinEdge>,
        cardWidthPx: Float,
        cardHeightPx: Float,
    ): List<Segment> {
        if (tables.isEmpty() || joins.isEmpty()) return emptyList()
        val centers =
            tables.associate { st ->
                st.alias() to
                    Point(
                        (st.canvasX() + cardWidthPx / 2f).toFloat(),
                        (st.canvasY() + cardHeightPx / 2f).toFloat(),
                    )
            }
        return joins.mapNotNull { edge ->
            val from = centers[edge.leftAlias()] ?: return@mapNotNull null
            val to = centers[edge.rightAlias()] ?: return@mapNotNull null
            Segment(from, to, edge.joinType().name)
        }
    }
}

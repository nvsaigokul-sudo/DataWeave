package com.dataweave.app.ui.browse

/** Pure selection/copy helpers for spreadsheet grid (unit-tested). */
object GridSelectionLogic {
    fun moveSelection(
        row: Int,
        col: Int,
        rowCount: Int,
        colCount: Int,
        deltaRow: Int,
        deltaCol: Int,
    ): Pair<Int, Int> {
        if (rowCount <= 0 || colCount <= 0) return 0 to 0
        val nr = (row + deltaRow).coerceIn(0, rowCount - 1)
        val nc = (col + deltaCol).coerceIn(0, colCount - 1)
        return nr to nc
    }

    fun formatCell(value: Any?, isNull: Boolean): String = if (isNull) "" else value?.toString() ?: ""

    /** Distinguishes SQL NULL (italic in UI) from empty string (blank, not null). */
    fun isSqlNull(value: Any?): Boolean = value == null

    fun columnIndexAfterHome(current: Int): Int = 0

    fun columnIndexAfterEnd(current: Int, colCount: Int): Int = (colCount - 1).coerceAtLeast(0)

    fun formatRowTsv(row: Map<String, Any?>, columnNames: List<String>): String =
        columnNames.joinToString("\t") { name ->
            val v = row[name]
            if (v == null) "" else v.toString()
        }

    fun formatColumnValues(rows: List<Map<String, Any?>>, columnName: String): String =
        rows.joinToString("\n") { row ->
            val v = row[columnName]
            if (v == null) "" else v.toString()
        }

    fun reorderColumn(order: List<String>, fromIndex: Int, toIndex: Int): List<String> {
        if (fromIndex !in order.indices || toIndex !in order.indices || fromIndex == toIndex) return order
        val m = order.toMutableList()
        val item = m.removeAt(fromIndex)
        m.add(toIndex, item)
        return m
    }
}

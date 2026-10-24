package com.dataweave.app.ui.browse

/** Pure selection/copy helpers for spreadsheet grid (unit-tested). */
object GridSelectionLogic {
    enum class SelectionKind {
        CELL,
        ROW,
        COLUMN,
        RECT,
    }

    data class CellRef(val row: Int, val col: Int)

    data class SelectionState(
        val kind: SelectionKind = SelectionKind.CELL,
        val anchor: CellRef = CellRef(0, 0),
        val focus: CellRef = CellRef(0, 0),
    ) {
        fun normalizedRect(rowCount: Int, colCount: Int): Pair<IntRange, IntRange> {
            val r0 = anchor.row.coerceIn(0, (rowCount - 1).coerceAtLeast(0))
            val r1 = focus.row.coerceIn(0, (rowCount - 1).coerceAtLeast(0))
            val c0 = anchor.col.coerceIn(0, (colCount - 1).coerceAtLeast(0))
            val c1 = focus.col.coerceIn(0, (colCount - 1).coerceAtLeast(0))
            return minOf(r0, r1)..maxOf(r0, r1) to minOf(c0, c1)..maxOf(c0, c1)
        }
    }

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

    fun arrowKey(
        selection: SelectionState,
        deltaRow: Int,
        deltaCol: Int,
        rowCount: Int,
        colCount: Int,
        extend: Boolean,
    ): SelectionState {
        if (rowCount <= 0 || colCount <= 0) return SelectionState()
        val (nr, nc) = moveSelection(selection.focus.row, selection.focus.col, rowCount, colCount, deltaRow, deltaCol)
        return if (extend) {
            selection.copy(kind = SelectionKind.RECT, focus = CellRef(nr, nc))
        } else {
            SelectionState(kind = SelectionKind.CELL, anchor = CellRef(nr, nc), focus = CellRef(nr, nc))
        }
    }

    fun selectRow(row: Int, colCount: Int): SelectionState {
        val c = (colCount - 1).coerceAtLeast(0)
        return SelectionState(kind = SelectionKind.ROW, anchor = CellRef(row, 0), focus = CellRef(row, c))
    }

    fun selectColumn(col: Int, rowCount: Int): SelectionState {
        val r = (rowCount - 1).coerceAtLeast(0)
        return SelectionState(kind = SelectionKind.COLUMN, anchor = CellRef(0, col), focus = CellRef(r, col))
    }

    fun isCellSelected(selection: SelectionState, row: Int, col: Int, rowCount: Int, colCount: Int): Boolean {
        if (rowCount <= 0 || colCount <= 0) return false
        val (rows, cols) = selection.normalizedRect(rowCount, colCount)
        return when (selection.kind) {
            SelectionKind.CELL -> selection.focus.row == row && selection.focus.col == col
            SelectionKind.ROW -> row in rows
            SelectionKind.COLUMN -> col in cols
            SelectionKind.RECT -> row in rows && col in cols
        }
    }

    fun formatCell(value: Any?, isNull: Boolean): String = if (isNull) "" else value?.toString() ?: ""

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

    fun formatSelectionTsv(
        rows: List<Map<String, Any?>>,
        columnNames: List<String>,
        selection: SelectionState,
    ): String {
        if (rows.isEmpty() || columnNames.isEmpty()) return ""
        val (rowRange, colRange) = selection.normalizedRect(rows.size, columnNames.size)
        return buildString {
            for (r in rowRange) {
                val row = rows[r]
                for (c in colRange) {
                    if (c > colRange.first) append('\t')
                    val name = columnNames[c]
                    append(formatCell(row[name], isSqlNull(row[name])))
                }
                if (r < rowRange.last) append('\n')
            }
        }
    }

    fun reorderColumn(order: List<String>, fromIndex: Int, toIndex: Int): List<String> {
        if (fromIndex !in order.indices || toIndex !in order.indices || fromIndex == toIndex) return order
        val m = order.toMutableList()
        val item = m.removeAt(fromIndex)
        m.add(toIndex, item)
        return m
    }
}

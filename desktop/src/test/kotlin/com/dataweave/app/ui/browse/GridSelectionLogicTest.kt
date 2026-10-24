package com.dataweave.app.ui.browse

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GridSelectionLogicTest {
    @Test
    fun moveSelectionClampsToGrid() {
        assertEquals(1 to 2, GridSelectionLogic.moveSelection(0, 0, 5, 4, 1, 2))
        assertEquals(4 to 3, GridSelectionLogic.moveSelection(10, 10, 5, 4, 0, 0))
    }

    @Test
    fun shiftArrowExtendsRect() {
        val start = GridSelectionLogic.SelectionState(
            kind = GridSelectionLogic.SelectionKind.CELL,
            anchor = GridSelectionLogic.CellRef(1, 1),
            focus = GridSelectionLogic.CellRef(1, 1),
        )
        val extended = GridSelectionLogic.arrowKey(start, 0, 2, 5, 4, extend = true)
        assertEquals(GridSelectionLogic.SelectionKind.RECT, extended.kind)
        assertEquals(1, extended.anchor.row)
        assertEquals(3, extended.focus.col)
    }

    @Test
    fun rectSelectionHighlightsInteriorCells() {
        val sel =
            GridSelectionLogic.SelectionState(
                kind = GridSelectionLogic.SelectionKind.RECT,
                anchor = GridSelectionLogic.CellRef(0, 0),
                focus = GridSelectionLogic.CellRef(1, 1),
            )
        assertTrue(GridSelectionLogic.isCellSelected(sel, 0, 0, 3, 3))
        assertTrue(GridSelectionLogic.isCellSelected(sel, 1, 1, 3, 3))
        assertFalse(GridSelectionLogic.isCellSelected(sel, 2, 2, 3, 3))
    }

    @Test
    fun formatSelectionTsvUsesTabsAndNewlines() {
        val rows =
            listOf(
                mapOf("a" to 1, "b" to 2),
                mapOf("a" to 3, "b" to 4),
            )
        val sel =
            GridSelectionLogic.SelectionState(
                kind = GridSelectionLogic.SelectionKind.RECT,
                anchor = GridSelectionLogic.CellRef(0, 0),
                focus = GridSelectionLogic.CellRef(1, 1),
            )
        assertEquals("1\t2\n3\t4", GridSelectionLogic.formatSelectionTsv(rows, listOf("a", "b"), sel))
    }

    @Test
    fun emptyStringIsNotSqlNull() {
        assertEquals(false, GridSelectionLogic.isSqlNull(""))
        assertEquals(true, GridSelectionLogic.isSqlNull(null))
    }

    @Test
    fun reorderColumnMovesItem() {
        assertEquals(listOf("b", "a", "c"), GridSelectionLogic.reorderColumn(listOf("a", "b", "c"), 0, 1))
    }

    @Test
    fun formatSelectionCsvUsesCommas() {
        val rows = listOf(mapOf("a" to 1, "b" to 2))
        val sel =
            GridSelectionLogic.SelectionState(
                anchor = GridSelectionLogic.CellRef(0, 0),
                focus = GridSelectionLogic.CellRef(0, 1),
            )
        assertEquals("1,2", GridSelectionLogic.formatSelectionCsv(rows, listOf("a", "b"), sel))
    }

    @Test
    fun clampSelectionAfterPageResize() {
        val sel =
            GridSelectionLogic.SelectionState(
                kind = GridSelectionLogic.SelectionKind.RECT,
                anchor = GridSelectionLogic.CellRef(5, 3),
                focus = GridSelectionLogic.CellRef(8, 4),
            )
        val clamped = GridSelectionLogic.clampSelection(sel, 3, 2)
        assertEquals(2, clamped.focus.row)
        assertEquals(1, clamped.focus.col)
    }
}

package com.dataweave.app.ui.browse

import kotlin.test.Test
import kotlin.test.assertEquals

class GridSelectionLogicTest {
    @Test
    fun moveSelectionClampsToGrid() {
        assertEquals(1 to 2, GridSelectionLogic.moveSelection(0, 0, 5, 4, 1, 2))
        assertEquals(4 to 3, GridSelectionLogic.moveSelection(10, 10, 5, 4, 0, 0))
    }

    @Test
    fun formatRowTsvUsesTabs() {
        val row = mapOf("a" to 1, "b" to "x")
        assertEquals("1\tx", GridSelectionLogic.formatRowTsv(row, listOf("a", "b")))
    }

    @Test
    fun reorderColumnMovesItem() {
        assertEquals(listOf("b", "a", "c"), GridSelectionLogic.reorderColumn(listOf("a", "b", "c"), 0, 1))
    }

    @Test
    fun emptyStringIsNotSqlNull() {
        assertEquals(false, GridSelectionLogic.isSqlNull(""))
        assertEquals(true, GridSelectionLogic.isSqlNull(null))
    }

    @Test
    fun formatCellNullVsEmpty() {
        assertEquals("", GridSelectionLogic.formatCell(null, true))
        assertEquals("", GridSelectionLogic.formatCell("", false))
        assertEquals("x", GridSelectionLogic.formatCell("x", false))
    }
}

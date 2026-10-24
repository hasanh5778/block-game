package com.example

import com.example.model.BlockPiece
import com.example.model.BlockShapeFactory
import com.example.model.BoardCell
import com.example.model.BoardLogic
import com.example.model.GRID_SIZE
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testEmptyBoardInitialization() {
        val board = BoardLogic.createEmptyBoard()
        assertEquals(GRID_SIZE, board.size)
        assertEquals(GRID_SIZE, board[0].size)
        assertTrue(board.all { row -> row.all { cell -> cell == null } })
    }

    @Test
    fun testCanPlacePieceValidAndInvalid() {
        val board = BoardLogic.createEmptyBoard()
        val piece2x2 = BlockPiece(
            name = "TestSquare",
            matrix = listOf(listOf(true, true), listOf(true, true)),
            colorHex = 0xFFFFFFFF,
            highlightHex = 0xFFFFFFFF
        )

        // Valid inside grid
        assertTrue(BoardLogic.canPlacePiece(board, piece2x2, 0, 0))
        assertTrue(BoardLogic.canPlacePiece(board, piece2x2, 6, 6))

        // Invalid: exceeds board boundary
        assertFalse(BoardLogic.canPlacePiece(board, piece2x2, 7, 7))
        assertFalse(BoardLogic.canPlacePiece(board, piece2x2, -1, 0))

        // Place piece
        val placedBoard = BoardLogic.placePiece(board, piece2x2, 0, 0)
        assertFalse(BoardLogic.canPlacePiece(placedBoard, piece2x2, 0, 0))
        assertFalse(BoardLogic.canPlacePiece(placedBoard, piece2x2, 1, 1))
        assertTrue(BoardLogic.canPlacePiece(placedBoard, piece2x2, 2, 2))
    }

    @Test
    fun testLineClearDetection() {
        var board = BoardLogic.createEmptyBoard()

        // Fill an entire row (row 3)
        val mutable = board.map { it.toMutableList() }.toMutableList()
        for (c in 0 until GRID_SIZE) {
            mutable[3][c] = BoardCell(0xFF38BDF8, 0xFFBAE6FD)
        }
        board = mutable.map { it.toList() }

        val lines = BoardLogic.findFullLines(board)
        assertEquals(1, lines.totalLines)
        assertEquals(listOf(3), lines.fullRows)
        assertTrue(lines.fullCols.isEmpty())

        // Clear row
        val cleared = BoardLogic.clearLines(board, lines.fullRows, lines.fullCols)
        assertTrue(cleared[3].all { it == null })
    }

    @Test
    fun testGameOverCheck() {
        // Create full board except 1 empty cell at (0, 0)
        val mutable = List(GRID_SIZE) { r ->
            MutableList<BoardCell?>(GRID_SIZE) { c ->
                if (r == 0 && c == 0) null else BoardCell(0xFF38BDF8, 0xFFBAE6FD)
            }
        }
        val almostFullBoard = mutable.map { it.toList() }

        val dot1x1 = BlockPiece(
            name = "Dot",
            matrix = listOf(listOf(true)),
            colorHex = 0xFFFFFFFF,
            highlightHex = 0xFFFFFFFF
        )
        val square2x2 = BlockPiece(
            name = "Square",
            matrix = listOf(listOf(true, true), listOf(true, true)),
            colorHex = 0xFFFFFFFF,
            highlightHex = 0xFFFFFFFF
        )

        // Dot should fit
        assertTrue(BoardLogic.canPieceFitAnywhere(almostFullBoard, dot1x1))

        // 2x2 should not fit
        assertFalse(BoardLogic.canPieceFitAnywhere(almostFullBoard, square2x2))

        // If only 2x2 is in tray, canAnyPieceFit should return false (Game Over!)
        assertFalse(BoardLogic.canAnyPieceFit(almostFullBoard, listOf(square2x2)))
    }
}

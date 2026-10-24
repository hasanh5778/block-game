package com.example.model

const val GRID_SIZE = 8

data class BoardCell(
    val colorHex: Long,
    val highlightHex: Long,
    val isClearing: Boolean = false
)

typealias BoardGrid = List<List<BoardCell?>>

data class LineClearResult(
    val fullRows: List<Int>,
    val fullCols: List<Int>
) {
    val totalLines: Int get() = fullRows.size + fullCols.size
    val hasLines: Boolean get() = totalLines > 0
}

object BoardLogic {

    fun createEmptyBoard(): BoardGrid {
        return List(GRID_SIZE) { List(GRID_SIZE) { null } }
    }

    /**
     * Checks if a piece can be placed on the board with its top-left corner at (startRow, startCol).
     */
    fun canPlacePiece(board: BoardGrid, piece: BlockPiece, startRow: Int, startCol: Int): Boolean {
        if (startRow < 0 || startCol < 0) return false
        if (startRow + piece.height > GRID_SIZE || startCol + piece.width > GRID_SIZE) return false

        for (r in 0 until piece.height) {
            for (c in 0 until piece.width) {
                if (piece.isOccupied(r, c)) {
                    val boardR = startRow + r
                    val boardC = startCol + c
                    if (boardR !in 0 until GRID_SIZE || boardC !in 0 until GRID_SIZE) {
                        return false
                    }
                    if (board[boardR][boardC] != null) {
                        return false
                    }
                }
            }
        }
        return true
    }

    /**
     * Checks if a specific piece can fit anywhere on the current board.
     */
    fun canPieceFitAnywhere(board: BoardGrid, piece: BlockPiece): Boolean {
        val maxR = GRID_SIZE - piece.height
        val maxC = GRID_SIZE - piece.width
        if (maxR < 0 || maxC < 0) return false

        for (r in 0..maxR) {
            for (c in 0..maxC) {
                if (canPlacePiece(board, piece, r, c)) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * Checks if any of the given pieces can fit on the board.
     * Returns true if at least one piece can be legally placed.
     */
    fun canAnyPieceFit(board: BoardGrid, pieces: List<BlockPiece>): Boolean {
        if (pieces.isEmpty()) return true
        return pieces.any { piece -> canPieceFitAnywhere(board, piece) }
    }

    /**
     * Places the piece onto the board and returns a new BoardGrid.
     */
    fun placePiece(board: BoardGrid, piece: BlockPiece, startRow: Int, startCol: Int): BoardGrid {
        val mutable = board.map { it.toMutableList() }.toMutableList()
        for (r in 0 until piece.height) {
            for (c in 0 until piece.width) {
                if (piece.isOccupied(r, c)) {
                    val boardR = startRow + r
                    val boardC = startCol + c
                    mutable[boardR][boardC] = BoardCell(
                        colorHex = piece.colorHex,
                        highlightHex = piece.highlightHex
                    )
                }
            }
        }
        return mutable.map { it.toList() }
    }

    /**
     * Finds all completely filled rows and columns on the board.
     */
    fun findFullLines(board: BoardGrid): LineClearResult {
        val fullRows = mutableListOf<Int>()
        val fullCols = mutableListOf<Int>()

        // Check rows
        for (r in 0 until GRID_SIZE) {
            if (board[r].all { it != null }) {
                fullRows.add(r)
            }
        }

        // Check columns
        for (c in 0 until GRID_SIZE) {
            var colFull = true
            for (r in 0 until GRID_SIZE) {
                if (board[r][c] == null) {
                    colFull = false
                    break
                }
            }
            if (colFull) {
                fullCols.add(c)
            }
        }

        return LineClearResult(fullRows, fullCols)
    }

    /**
     * Marks the cells in full rows and columns as isClearing = true for visual flashing.
     */
    fun markLinesClearing(board: BoardGrid, rows: List<Int>, cols: List<Int>): BoardGrid {
        val mutable = board.map { it.toMutableList() }.toMutableList()
        for (r in rows) {
            for (c in 0 until GRID_SIZE) {
                mutable[r][c]?.let {
                    mutable[r][c] = it.copy(isClearing = true)
                }
            }
        }
        for (c in cols) {
            for (r in 0 until GRID_SIZE) {
                mutable[r][c]?.let {
                    mutable[r][c] = it.copy(isClearing = true)
                }
            }
        }
        return mutable.map { it.toList() }
    }

    /**
     * Clears specified rows and columns by setting them to null.
     */
    fun clearLines(board: BoardGrid, rows: List<Int>, cols: List<Int>): BoardGrid {
        val mutable = board.map { it.toMutableList() }.toMutableList()
        for (r in rows) {
            for (c in 0 until GRID_SIZE) {
                mutable[r][c] = null
            }
        }
        for (c in cols) {
            for (r in 0 until GRID_SIZE) {
                mutable[r][c] = null
            }
        }
        return mutable.map { it.toList() }
    }
}

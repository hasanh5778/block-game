package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundManager
import com.example.data.ScorePreferences
import com.example.model.BlockPiece
import com.example.model.BlockShapeFactory
import com.example.model.BoardCell
import com.example.model.BoardGrid
import com.example.model.BoardLogic
import com.example.model.GRID_SIZE
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ComboBannerData(
    val streak: Int,
    val bonusScore: Int,
    val title: String,
    val id: Long = System.currentTimeMillis()
)

data class GameUiState(
    val board: BoardGrid = BoardLogic.createEmptyBoard(),
    val trayPieces: List<BlockPiece> = emptyList(),
    val score: Int = 0,
    val bestScore: Int = 0,
    val comboStreak: Int = 0,
    val maxComboSession: Int = 0,
    val linesClearedSession: Int = 0,
    val isGameOver: Boolean = false,
    val isNewHighScore: Boolean = false,
    val selectedPieceIndex: Int? = null,
    val hoverRow: Int? = null,
    val hoverCol: Int? = null,
    val hoverPieceId: String? = null,
    val isHoverValid: Boolean = false,
    val comboBanner: ComboBannerData? = null,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val isClearingAnimation: Boolean = false
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = ScorePreferences(application)
    private val soundManager = SoundManager(application)

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        startNewGame()
    }

    fun startNewGame() {
        val initialBest = prefs.getBestScore()
        val initialPieces = BlockShapeFactory.generateRoundPieces()
        _uiState.update {
            GameUiState(
                board = BoardLogic.createEmptyBoard(),
                trayPieces = initialPieces,
                score = 0,
                bestScore = initialBest,
                comboStreak = 0,
                maxComboSession = 0,
                linesClearedSession = 0,
                isGameOver = false,
                isNewHighScore = false,
                selectedPieceIndex = null,
                hoverRow = null,
                hoverCol = null,
                hoverPieceId = null,
                isHoverValid = false,
                comboBanner = null,
                soundEnabled = prefs.isSoundEnabled(),
                vibrationEnabled = prefs.isVibrationEnabled()
            )
        }
    }

    fun selectPiece(index: Int) {
        val currentSelected = _uiState.value.selectedPieceIndex
        if (currentSelected == index) {
            _uiState.update { it.copy(selectedPieceIndex = null, hoverRow = null, hoverCol = null, hoverPieceId = null) }
        } else {
            _uiState.update {
                it.copy(
                    selectedPieceIndex = index,
                    hoverRow = null,
                    hoverCol = null,
                    hoverPieceId = null
                )
            }
        }
    }

    fun clearSelection() {
        _uiState.update {
            it.copy(
                selectedPieceIndex = null,
                hoverRow = null,
                hoverCol = null,
                hoverPieceId = null,
                isHoverValid = false
            )
        }
    }

    fun updateHover(piece: BlockPiece?, startRow: Int?, startCol: Int?) {
        if (piece == null || startRow == null || startCol == null) {
            _uiState.update {
                it.copy(hoverRow = null, hoverCol = null, hoverPieceId = null, isHoverValid = false)
            }
            return
        }

        val isValid = BoardLogic.canPlacePiece(_uiState.value.board, piece, startRow, startCol)
        _uiState.update {
            it.copy(
                hoverRow = startRow,
                hoverCol = startCol,
                hoverPieceId = piece.id,
                isHoverValid = isValid
            )
        }
    }

    /**
     * Attempts to place the given piece at (startRow, startCol).
     * Returns true if placement was successful, false otherwise.
     */
    fun tryPlacePiece(piece: BlockPiece, startRow: Int, startCol: Int): Boolean {
        val currentBoard = _uiState.value.board
        if (!BoardLogic.canPlacePiece(currentBoard, piece, startRow, startCol)) {
            return false
        }

        val boardAfterPlacement = BoardLogic.placePiece(currentBoard, piece, startRow, startCol)
        val lineResult = BoardLogic.findFullLines(boardAfterPlacement)

        // Calculate placement points
        val piecePoints = piece.blockCount * 10
        var newScore = _uiState.value.score + piecePoints
        var currentBest = _uiState.value.bestScore

        val soundOn = _uiState.value.soundEnabled
        val vibOn = _uiState.value.vibrationEnabled

        val newTray = _uiState.value.trayPieces.filterNot { it.id == piece.id }

        if (!lineResult.hasLines) {
            // No lines cleared: reset combo streak
            if (newScore > currentBest) {
                currentBest = newScore
                prefs.saveBestScore(newScore)
            }
            soundManager.playPlaceSound(soundOn, vibOn)

            finalizePlacement(
                finalBoard = boardAfterPlacement,
                remainingTray = newTray,
                newScore = newScore,
                newBestScore = currentBest,
                newCombo = 0,
                clearedLinesCount = 0
            )
            return true
        }

        // Lines were cleared!
        val linesCount = lineResult.totalLines
        val newCombo = _uiState.value.comboStreak + 1
        val maxCombo = maxOf(_uiState.value.maxComboSession, newCombo)
        val totalLines = _uiState.value.linesClearedSession + linesCount

        // Points for lines
        val baseLineScore = when (linesCount) {
            1 -> 100
            2 -> 300
            3 -> 600
            4 -> 1000
            else -> 1000 + (linesCount - 4) * 500
        }
        val comboMultiplier = 1.0 + (newCombo * 0.5)
        val lineScore = (baseLineScore * comboMultiplier).toInt()
        newScore += lineScore

        if (newScore > currentBest) {
            currentBest = newScore
            prefs.saveBestScore(newScore)
        }

        // Play sounds and haptics
        if (newCombo >= 2) {
            soundManager.playComboSound(soundOn, vibOn, newCombo)
        } else {
            soundManager.playLineClearSound(soundOn, vibOn, linesCount)
        }

        val comboTitle = when {
            newCombo >= 5 -> "EFSANEVİ! 👑"
            newCombo >= 4 -> "İNANILMAZ! 🌟"
            newCombo >= 3 -> "HARİKA! ⚡"
            newCombo == 2 -> "KOMBO x2! 🔥"
            linesCount >= 3 -> "MEGA TEMİZLİK! ✨"
            linesCount == 2 -> "ÇİFTE SIRA! 🎯"
            else -> "KOMBO! 💥"
        }

        val banner = ComboBannerData(
            streak = newCombo,
            bonusScore = lineScore,
            title = comboTitle
        )

        // Mark clearing animation
        val markedBoard = BoardLogic.markLinesClearing(
            boardAfterPlacement,
            lineResult.fullRows,
            lineResult.fullCols
        )

        _uiState.update {
            it.copy(
                board = markedBoard,
                selectedPieceIndex = null,
                hoverRow = null,
                hoverCol = null,
                hoverPieceId = null,
                isHoverValid = false,
                comboBanner = banner,
                isClearingAnimation = true
            )
        }

        viewModelScope.launch {
            // Flash animation delay
            delay(260)
            val clearedBoard = BoardLogic.clearLines(
                boardAfterPlacement,
                lineResult.fullRows,
                lineResult.fullCols
            )

            finalizePlacement(
                finalBoard = clearedBoard,
                remainingTray = newTray,
                newScore = newScore,
                newBestScore = currentBest,
                newCombo = newCombo,
                clearedLinesCount = linesCount,
                maxCombo = maxCombo,
                totalLines = totalLines
            )
        }

        return true
    }

    /**
     * Tap to place handler: if player taps cell (row, col) with a selected piece.
     */
    fun onCellClicked(row: Int, col: Int) {
        val selectedIdx = _uiState.value.selectedPieceIndex ?: return
        val pieces = _uiState.value.trayPieces
        if (selectedIdx !in pieces.indices) return
        val piece = pieces[selectedIdx]

        // Check if piece fits with clicked cell as top-left
        if (tryPlacePiece(piece, row, col)) {
            clearSelection()
            return
        }

        // Also check if clicked cell could be the center or relative anchor
        val rOffset = minOf(row, GRID_SIZE - piece.height)
        val cOffset = minOf(col, GRID_SIZE - piece.width)
        if (rOffset >= 0 && cOffset >= 0 && tryPlacePiece(piece, rOffset, cOffset)) {
            clearSelection()
        }
    }

    private fun finalizePlacement(
        finalBoard: BoardGrid,
        remainingTray: List<BlockPiece>,
        newScore: Int,
        newBestScore: Int,
        newCombo: Int,
        clearedLinesCount: Int,
        maxCombo: Int = _uiState.value.maxComboSession,
        totalLines: Int = _uiState.value.linesClearedSession + clearedLinesCount
    ) {
        var activeTray = remainingTray
        if (activeTray.isEmpty()) {
            activeTray = BlockShapeFactory.generateRoundPieces()
        }

        val canContinue = BoardLogic.canAnyPieceFit(finalBoard, activeTray)
        val isGameOver = !canContinue

        if (isGameOver) {
            soundManager.playGameOverSound(
                _uiState.value.soundEnabled,
                _uiState.value.vibrationEnabled
            )
        }

        _uiState.update {
            it.copy(
                board = finalBoard,
                trayPieces = activeTray,
                score = newScore,
                bestScore = newBestScore,
                comboStreak = newCombo,
                maxComboSession = maxCombo,
                linesClearedSession = totalLines,
                isGameOver = isGameOver,
                isNewHighScore = newScore > 0 && newScore >= newBestScore,
                selectedPieceIndex = null,
                hoverRow = null,
                hoverCol = null,
                hoverPieceId = null,
                isHoverValid = false,
                isClearingAnimation = false
            )
        }
    }

    fun dismissComboBanner() {
        _uiState.update { it.copy(comboBanner = null) }
    }

    fun toggleSound() {
        val current = _uiState.value.soundEnabled
        val next = !current
        prefs.setSoundEnabled(next)
        _uiState.update { it.copy(soundEnabled = next) }
    }

    fun toggleVibration() {
        val current = _uiState.value.vibrationEnabled
        val next = !current
        prefs.setVibrationEnabled(next)
        _uiState.update { it.copy(vibrationEnabled = next) }
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.release()
    }
}

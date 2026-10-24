package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.BlockPiece
import com.example.ui.components.ComboOverlay
import com.example.ui.components.GameBoard
import com.example.ui.components.GameOverDialog
import com.example.ui.components.PieceTray
import com.example.ui.components.ScoreHeader
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextMuted
import com.example.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                BlockPuzzleApp()
            }
        }
    }
}

@Composable
fun BlockPuzzleApp(
    viewModel: GameViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var boardCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

    // Active hovered or selected piece for ghost preview
    val activeGhostPiece: BlockPiece? = remember(
        uiState.hoverPieceId,
        uiState.selectedPieceIndex,
        uiState.trayPieces
    ) {
        val hoverPiece = uiState.trayPieces.firstOrNull { it.id == uiState.hoverPieceId }
        if (hoverPiece != null) {
            hoverPiece
        } else if (uiState.selectedPieceIndex != null && uiState.selectedPieceIndex!! in uiState.trayPieces.indices) {
            uiState.trayPieces[uiState.selectedPieceIndex!!]
        } else {
            null
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BackgroundDark
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .navigationBarsPadding()
                .background(BackgroundDark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 1. Top Bar & Score Board
                ScoreHeader(
                    score = uiState.score,
                    bestScore = uiState.bestScore,
                    soundEnabled = uiState.soundEnabled,
                    vibrationEnabled = uiState.vibrationEnabled,
                    onToggleSound = { viewModel.toggleSound() },
                    onToggleVibration = { viewModel.toggleVibration() },
                    onRestartGame = { viewModel.startNewGame() }
                )

                // 2. Center: 8x8 Board & Combo Notifications
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    GameBoard(
                        board = uiState.board,
                        hoverPiece = activeGhostPiece,
                        hoverRow = uiState.hoverRow,
                        hoverCol = uiState.hoverCol,
                        isHoverValid = uiState.isHoverValid,
                        onCellClicked = { r, c -> viewModel.onCellClicked(r, c) },
                        onBoardGloballyPositioned = { coords ->
                            boardCoordinates = coords
                        }
                    )

                    // Combo and multi-clear animated overlay
                    ComboOverlay(
                        comboBanner = uiState.comboBanner,
                        onDismiss = { viewModel.dismissComboBanner() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // 3. Bottom: 3 Pieces Tray
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (uiState.selectedPieceIndex != null) "Tahtaya dokunarak yerleştirin" else "Sürükleyin veya seçmek için dokunun",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    PieceTray(
                        pieces = uiState.trayPieces,
                        selectedPieceIndex = uiState.selectedPieceIndex,
                        boardCoordinates = boardCoordinates,
                        onSelectPiece = { idx -> viewModel.selectPiece(idx) },
                        onDragHover = { piece, r, c -> viewModel.updateHover(piece, r, c) },
                        onPiecePlaced = { piece, r, c -> viewModel.tryPlacePiece(piece, r, c) }
                    )
                }
            }

            // 4. Game Over Dialog Modal
            if (uiState.isGameOver) {
                GameOverDialog(
                    score = uiState.score,
                    bestScore = uiState.bestScore,
                    isNewHighScore = uiState.isNewHighScore,
                    totalLinesCleared = uiState.linesClearedSession,
                    maxCombo = uiState.maxComboSession,
                    onPlayAgain = { viewModel.startNewGame() }
                )
            }
        }
    }
}

package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.BlockPiece
import com.example.model.BoardCell
import com.example.model.BoardGrid
import com.example.model.GRID_SIZE
import com.example.ui.theme.BoardBackground
import com.example.ui.theme.CellBorder
import com.example.ui.theme.CellEmpty

@Composable
fun GameBoard(
    board: BoardGrid,
    hoverPiece: BlockPiece?,
    hoverRow: Int?,
    hoverCol: Int?,
    isHoverValid: Boolean,
    onCellClicked: (row: Int, col: Int) -> Unit,
    onBoardGloballyPositioned: (LayoutCoordinates) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .aspectRatio(1f)
            .onGloballyPositioned { coordinates ->
                onBoardGloballyPositioned(coordinates)
            }
            .clip(RoundedCornerShape(18.dp))
            .background(BoardBackground)
            .border(2.dp, CellBorder.copy(alpha = 0.8f), RoundedCornerShape(18.dp))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        val totalSpacing = 5.dp * (GRID_SIZE - 1)
        val cellSize = (maxWidth - totalSpacing) / GRID_SIZE

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            for (r in 0 until GRID_SIZE) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (c in 0 until GRID_SIZE) {
                        val cellData = board[r][c]

                        // Check if this cell is part of the ghost hover preview
                        val isGhost = hoverPiece != null &&
                                hoverRow != null &&
                                hoverCol != null &&
                                r in hoverRow until (hoverRow + hoverPiece.height) &&
                                c in hoverCol until (hoverCol + hoverPiece.width) &&
                                hoverPiece.isOccupied(r - hoverRow, c - hoverCol)

                        BoardCellView(
                            cellData = cellData,
                            isGhost = isGhost,
                            isGhostValid = isHoverValid,
                            ghostColorHex = hoverPiece?.colorHex,
                            size = cellSize,
                            onClick = { onCellClicked(r, c) },
                            testTag = "cell_${r}_$c"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BoardCellView(
    cellData: BoardCell?,
    isGhost: Boolean,
    isGhostValid: Boolean,
    ghostColorHex: Long?,
    size: Dp,
    onClick: () -> Unit,
    testTag: String
) {
    val interactionSource = remember { MutableInteractionSource() }

    // Flash animation for clearing cells
    val scaleAnim = remember { Animatable(1f) }
    val glowAnim = remember { Animatable(0f) }

    LaunchedEffect(cellData?.isClearing) {
        if (cellData?.isClearing == true) {
            scaleAnim.animateTo(1.15f, tween(120))
            scaleAnim.animateTo(0.0f, tween(140))
        } else {
            scaleAnim.snapTo(1f)
        }
    }

    // Gentle pulse for valid ghost preview
    val pulseAnim = remember { Animatable(0.7f) }
    LaunchedEffect(isGhost) {
        if (isGhost) {
            pulseAnim.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(400, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        } else {
            pulseAnim.snapTo(0.7f)
        }
    }

    val cellModifier = Modifier
        .size(size)
        .scale(scaleAnim.value)
        .clip(RoundedCornerShape(7.dp))
        .clickable(
            interactionSource = interactionSource,
            indication = ripple(bounded = true, color = Color.White.copy(alpha = 0.2f)),
            onClick = onClick
        )
        .testTag(testTag)

    when {
        // 1. Occupied Cell
        cellData != null -> {
            val baseColor = Color(cellData.colorHex)
            val highlightColor = Color(cellData.highlightHex)
            val isClearing = cellData.isClearing

            val brush = if (isClearing) {
                Brush.radialGradient(
                    colors = listOf(Color.White, Color(0xFFFFDF70), baseColor)
                )
            } else {
                Brush.linearGradient(
                    colors = listOf(highlightColor, baseColor),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            }

            Box(
                modifier = cellModifier
                    .background(brush)
                    .border(
                        width = 1.dp,
                        color = if (isClearing) Color.White else highlightColor.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(7.dp)
                    )
            ) {
                // Subtle 3D top shine
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp, vertical = 2.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .drawBehind {
                            drawRect(
                                Brush.verticalGradient(
                                    listOf(Color.White.copy(alpha = 0.35f), Color.Transparent)
                                )
                            )
                        }
                )
            }
        }

        // 2. Ghost preview (dragging or selected piece hovering over empty cell)
        isGhost && ghostColorHex != null -> {
            val ghostBase = Color(ghostColorHex)
            val ghostColor = if (isGhostValid) {
                ghostBase.copy(alpha = 0.55f * pulseAnim.value)
            } else {
                Color(0xFFEF4444).copy(alpha = 0.3f)
            }
            val ghostBorder = if (isGhostValid) {
                ghostBase.copy(alpha = 0.9f)
            } else {
                Color(0xFFEF4444).copy(alpha = 0.7f)
            }

            Box(
                modifier = cellModifier
                    .background(ghostColor)
                    .border(1.5.dp, ghostBorder, RoundedCornerShape(7.dp))
            )
        }

        // 3. Normal Empty Cell
        else -> {
            Box(
                modifier = cellModifier
                    .background(CellEmpty)
                    .border(0.7.dp, CellBorder, RoundedCornerShape(7.dp))
            )
        }
    }
}

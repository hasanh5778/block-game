package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BlockPiece
import com.example.model.GRID_SIZE
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.CellBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import kotlin.math.roundToInt

@Composable
fun PieceTray(
    pieces: List<BlockPiece>,
    selectedPieceIndex: Int?,
    boardCoordinates: LayoutCoordinates?,
    onSelectPiece: (Int) -> Unit,
    onDragHover: (piece: BlockPiece?, startRow: Int?, startCol: Int?) -> Unit,
    onPiecePlaced: (piece: BlockPiece, startRow: Int, startCol: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        if (pieces.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Yeni bloklar geliyor...",
                    fontSize = 13.sp,
                    color = TextMuted
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (index in pieces.indices) {
                    val piece = pieces[index]
                    val isSelected = selectedPieceIndex == index

                    PieceTraySlot(
                        piece = piece,
                        slotIndex = index,
                        isSelected = isSelected,
                        boardCoordinates = boardCoordinates,
                        onSelect = { onSelectPiece(index) },
                        onDragHover = onDragHover,
                        onDrop = { row, col -> onPiecePlaced(piece, row, col) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun PieceTraySlot(
    piece: BlockPiece,
    slotIndex: Int,
    isSelected: Boolean,
    boardCoordinates: LayoutCoordinates?,
    onSelect: () -> Unit,
    onDragHover: (piece: BlockPiece?, startRow: Int?, startCol: Int?) -> Unit,
    onDrop: (startRow: Int, startCol: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    var isDragging by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var slotRootPos by remember { mutableStateOf(Offset.Zero) }

    // Touch lift offset so user's thumb does not block view of board while dragging
    val touchLiftPx = with(density) { -80.dp.toPx() }

    // Pulse animation for selected piece
    val pulseAnim = remember { Animatable(1f) }
    LaunchedEffect(isSelected) {
        if (isSelected) {
            pulseAnim.animateTo(
                targetValue = 1.08f,
                animationSpec = infiniteRepeatable(
                    animation = tween(500, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        } else {
            pulseAnim.snapTo(1f)
        }
    }

    Box(
        modifier = modifier
            .padding(4.dp)
            .height(115.dp)
            .onGloballyPositioned { coords ->
                slotRootPos = coords.positionInRoot()
            }
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) AccentCyan.copy(alpha = 0.12f)
                else SurfaceDark.copy(alpha = 0.6f)
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) AccentCyan else CellBorder.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onSelect
            )
            .pointerInput(piece.id, boardCoordinates) {
                detectDragGestures(
                    onDragStart = {
                        isDragging = true
                        dragOffset = Offset.Zero
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragOffset += dragAmount

                        // Calculate current touch finger position in root coordinates
                        val currentFingerRoot = slotRootPos + dragOffset
                        val previewAnchorRoot = Offset(
                            currentFingerRoot.x,
                            currentFingerRoot.y + touchLiftPx
                        )

                        // Check if within board coordinates
                        val boardCoords = boardCoordinates
                        if (boardCoords != null && boardCoords.isAttached) {
                            val boardPos = boardCoords.positionInRoot()
                            val boardWidth = boardCoords.size.width.toFloat()
                            val boardHeight = boardCoords.size.height.toFloat()

                            if (previewAnchorRoot.x in boardPos.x..(boardPos.x + boardWidth) &&
                                previewAnchorRoot.y in boardPos.y..(boardPos.y + boardHeight)
                            ) {
                                val cellPx = boardWidth / GRID_SIZE.toFloat()
                                val localX = previewAnchorRoot.x - boardPos.x
                                val localY = previewAnchorRoot.y - boardPos.y

                                // Align piece center or top-left
                                val col = ((localX / cellPx) - (piece.width / 2f)).roundToInt()
                                    .coerceIn(0, GRID_SIZE - piece.width)
                                val row = ((localY / cellPx) - (piece.height / 2f)).roundToInt()
                                    .coerceIn(0, GRID_SIZE - piece.height)

                                onDragHover(piece, row, col)
                            } else {
                                onDragHover(null, null, null)
                            }
                        }
                    },
                    onDragEnd = {
                        val boardCoords = boardCoordinates
                        val currentFingerRoot = slotRootPos + dragOffset
                        val previewAnchorRoot = Offset(
                            currentFingerRoot.x,
                            currentFingerRoot.y + touchLiftPx
                        )

                        if (boardCoords != null && boardCoords.isAttached) {
                            val boardPos = boardCoords.positionInRoot()
                            val boardWidth = boardCoords.size.width.toFloat()
                            val boardHeight = boardCoords.size.height.toFloat()

                            if (previewAnchorRoot.x in boardPos.x..(boardPos.x + boardWidth) &&
                                previewAnchorRoot.y in boardPos.y..(boardPos.y + boardHeight)
                            ) {
                                val cellPx = boardWidth / GRID_SIZE.toFloat()
                                val localX = previewAnchorRoot.x - boardPos.x
                                val localY = previewAnchorRoot.y - boardPos.y

                                val col = ((localX / cellPx) - (piece.width / 2f)).roundToInt()
                                    .coerceIn(0, GRID_SIZE - piece.width)
                                val row = ((localY / cellPx) - (piece.height / 2f)).roundToInt()
                                    .coerceIn(0, GRID_SIZE - piece.height)

                                onDrop(row, col)
                            }
                        }

                        onDragHover(null, null, null)
                        isDragging = false
                        dragOffset = Offset.Zero
                    },
                    onDragCancel = {
                        onDragHover(null, null, null)
                        isDragging = false
                        dragOffset = Offset.Zero
                    }
                )
            }
            .testTag("piece_slot_$slotIndex"),
        contentAlignment = Alignment.Center
    ) {
        val renderOffset = if (isDragging) {
            IntOffset(
                dragOffset.x.roundToInt(),
                (dragOffset.y + touchLiftPx).roundToInt()
            )
        } else {
            IntOffset.Zero
        }

        Box(
            modifier = Modifier
                .offset { renderOffset }
                .scale(if (isDragging) 1.25f else pulseAnim.value),
            contentAlignment = Alignment.Center
        ) {
            MiniPieceGrid(
                piece = piece,
                cellSize = if (isDragging) 26.dp else 19.dp
            )
        }
    }
}

@Composable
fun MiniPieceGrid(
    piece: BlockPiece,
    cellSize: Dp,
    modifier: Modifier = Modifier
) {
    val baseColor = Color(piece.colorHex)
    val highlightColor = Color(piece.highlightHex)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        for (r in 0 until piece.height) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (c in 0 until piece.width) {
                    if (piece.isOccupied(r, c)) {
                        Box(
                            modifier = Modifier
                                .size(cellSize)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(highlightColor, baseColor),
                                        start = Offset(0f, 0f),
                                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                                    )
                                )
                                .border(0.5.dp, highlightColor.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                        ) {
                            // Top shine
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .drawBehind {
                                        drawRect(
                                            Brush.verticalGradient(
                                                listOf(Color.White.copy(alpha = 0.4f), Color.Transparent)
                                            )
                                        )
                                    }
                            )
                        }
                    } else {
                        // Empty slot in bounding box
                        Spacer(modifier = Modifier.size(cellSize))
                    }
                }
            }
        }
    }
}

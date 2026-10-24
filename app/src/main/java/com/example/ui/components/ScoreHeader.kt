package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.GoldTrophy
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ScoreHeader(
    score: Int,
    bestScore: Int,
    soundEnabled: Boolean,
    vibrationEnabled: Boolean,
    onToggleSound: () -> Unit,
    onToggleVibration: () -> Unit,
    onRestartGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showRestartDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // Top Toolbar: Brand & Mini Settings
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(AccentCyan)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "BLOK BULMACA",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.8.sp,
                    color = AccentCyan
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onToggleSound,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("toggle_sound_button")
                ) {
                    Icon(
                        imageVector = if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                        contentDescription = "Ses Aç/Kapat",
                        tint = if (soundEnabled) TextPrimary else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onToggleVibration,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("toggle_vibration_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Vibration,
                        contentDescription = "Titreşim Aç/Kapat",
                        tint = if (vibrationEnabled) AccentCyan else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = { showRestartDialog = true },
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("restart_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Yeniden Başlat",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Score Boards Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Current Score Card
            ScoreCard(
                label = "SKOR",
                value = score,
                isBest = false,
                modifier = Modifier
                    .weight(1f)
                    .testTag("current_score_card")
            )

            // Best Score Card
            ScoreCard(
                label = "EN YÜKSEK",
                value = bestScore,
                isBest = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("best_score_card")
            )
        }
    }

    if (showRestartDialog) {
        AlertDialog(
            onDismissRequest = { showRestartDialog = false },
            title = {
                Text(
                    text = "Oyunu Yeniden Başlat?",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "Mevcut oyununuz sıfırlanacak ve yeni bir tahta açılacaktır.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRestartDialog = false
                        onRestartGame()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                    modifier = Modifier.testTag("confirm_restart_button")
                ) {
                    Text("Evet, Yeniden Başla", color = Color(0xFF0F1117), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showRestartDialog = false },
                    modifier = Modifier.testTag("cancel_restart_button")
                ) {
                    Text("İptal", color = TextSecondary)
                }
            },
            containerColor = SurfaceDark,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun ScoreCard(
    label: String,
    value: Int,
    isBest: Boolean,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isBest) Color(0xFFEAB308).copy(alpha = 0.4f) else Color(0xFF38BDF8).copy(alpha = 0.3f)
    val bgBrush = if (isBest) {
        Brush.verticalGradient(
            listOf(
                Color(0xFF231E12),
                SurfaceVariantDark
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color(0xFF142436),
                SurfaceVariantDark
            )
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgBrush)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (isBest) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = GoldTrophy,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = if (isBest) GoldTrophy else AccentCyan
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            AnimatedContent(
                targetState = value,
                transitionSpec = {
                    slideInVertically { height -> height } togetherWith
                            slideOutVertically { height -> -height }
                },
                label = "ScoreAnimation"
            ) { targetVal ->
                Text(
                    text = "$targetVal",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )
            }
        }
    }
}

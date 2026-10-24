package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.ComboBannerData
import kotlinx.coroutines.delay

@Composable
fun ComboOverlay(
    comboBanner: ComboBannerData?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = comboBanner != null,
        enter = fadeIn(tween(150)) + scaleIn(tween(250, easing = FastOutSlowInEasing), initialScale = 0.6f),
        exit = fadeOut(tween(200)) + scaleOut(tween(200), targetScale = 0.8f),
        modifier = modifier
    ) {
        if (comboBanner != null) {
            val bounce = remember(comboBanner.id) { Animatable(0.7f) }

            LaunchedEffect(comboBanner.id) {
                bounce.animateTo(1.15f, tween(140, easing = FastOutSlowInEasing))
                bounce.animateTo(1.0f, tween(100, easing = FastOutSlowInEasing))
                delay(1000)
                onDismiss()
            }

            val glowColors = if (comboBanner.streak >= 3) {
                listOf(Color(0xFFFF5722), Color(0xFFFF9800), Color(0xFFFFD54F))
            } else {
                listOf(Color(0xFF0284C7), Color(0xFF38BDF8), Color(0xFFBAE6FD))
            }

            Box(
                modifier = Modifier
                    .scale(bounce.value)
                    .shadow(16.dp, RoundedCornerShape(24.dp), ambientColor = glowColors[0], spotColor = glowColors[1])
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF1E1B4B),
                                Color(0xFF111827),
                                Color(0xFF1E1B4B)
                            )
                        )
                    )
                    .border(
                        2.dp,
                        Brush.horizontalGradient(glowColors),
                        RoundedCornerShape(24.dp)
                    )
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = comboBanner.title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.5.sp
                    )
                    if (comboBanner.bonusScore > 0) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "+${comboBanner.bonusScore} PUAN",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700)
                        )
                    }
                }
            }
        }
    }
}

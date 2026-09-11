package com.example.farmblaster.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PauseDialog(
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onMainMenu: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f))
            .testTag("pause_dialog"),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF1E293B))
                .border(1.5.dp, Color(0xFF38BDF8), RoundedCornerShape(20.dp))
                .shadow(24.dp)
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "GAME PAUSED",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFDE047),
                    letterSpacing = 1.5.sp
                )

                Spacer(modifier = Modifier.height(28.dp))

                MenuButton(
                    label = "RESUME",
                    icon = Icons.Default.PlayArrow,
                    brush = Brush.horizontalGradient(listOf(Color(0xFFF59E0B), Color(0xFFEA580C))),
                    textColor = Color.White,
                    testTag = "resume_button",
                    onClick = onResume
                )

                Spacer(modifier = Modifier.height(12.dp))

                MenuButton(
                    label = "RESTART",
                    icon = Icons.Default.Refresh,
                    brush = Brush.horizontalGradient(listOf(Color(0xFF334155), Color(0xFF475569))),
                    textColor = Color.White,
                    testTag = "restart_button",
                    onClick = onRestart
                )

                Spacer(modifier = Modifier.height(12.dp))

                MenuButton(
                    label = "MAIN MENU",
                    icon = Icons.Default.Home,
                    brush = Brush.horizontalGradient(listOf(Color(0xFF334155), Color(0xFF475569))),
                    textColor = Color.White,
                    testTag = "menu_button",
                    onClick = onMainMenu
                )
            }
        }
    }
}

@Composable
fun LevelCompleteDialog(
    wave: Int,
    bonus: Int,
    kills: Int,
    comboBonus: Int,
    onContinue: () -> Unit
) {
    // Auto-advance to next wave after 2.2 seconds if player doesn't tap Next Wave
    LaunchedEffect(wave) {
        delay(2200L)
        onContinue()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .testTag("level_complete_dialog"),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFF0F172A))
                .border(2.dp, Color(0xFF4ADE80), RoundedCornerShape(22.dp))
                .shadow(24.dp)
                .padding(26.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "🎉 WAVE $wave CLEARED!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF4ADE80),
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(22.dp))

                DialogStatRow(label = "Chickens Smashed", value = "$kills")
                Spacer(modifier = Modifier.height(8.dp))
                DialogStatRow(label = "Wave Bonus", value = "+$bonus")
                Spacer(modifier = Modifier.height(8.dp))
                DialogStatRow(label = "Combo Multiplier Bonus", value = "+$comboBonus")

                Spacer(modifier = Modifier.height(26.dp))

                MenuButton(
                    label = "NEXT WAVE",
                    icon = Icons.Default.PlayArrow,
                    brush = Brush.horizontalGradient(listOf(Color(0xFF22C55E), Color(0xFF15803D))),
                    textColor = Color.White,
                    testTag = "next_wave_button",
                    onClick = onContinue
                )
            }
        }
    }
}

@Composable
fun GameOverDialog(
    score: Int,
    highScore: Int,
    wave: Int,
    kills: Int,
    bestCombo: Int,
    isNewRecord: Boolean,
    onPlayAgain: () -> Unit,
    onMainMenu: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.82f))
            .testTag("game_over_dialog"),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFF1E112A))
                .border(2.dp, Color(0xFFEF4444), RoundedCornerShape(22.dp))
                .shadow(24.dp)
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "GAME OVER",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFEF4444),
                    letterSpacing = 2.sp
                )

                if (isNewRecord) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF59E0B))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "🏆 NEW HIGH SCORE!",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Score card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF2E1A47))
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "FINAL SCORE", fontSize = 12.sp, color = Color(0xFFC084FC), fontWeight = FontWeight.Bold)
                        Text(text = "$score", fontSize = 32.sp, color = Color.White, fontWeight = FontWeight.Black)
                        Text(text = "Best: $highScore", fontSize = 12.sp, color = Color(0xFF94A3B8))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                DialogStatRow(label = "Wave Reached", value = "$wave")
                Spacer(modifier = Modifier.height(6.dp))
                DialogStatRow(label = "Chickens Vaporized", value = "$kills")
                Spacer(modifier = Modifier.height(6.dp))
                DialogStatRow(label = "Best Combo", value = "${bestCombo}x")

                Spacer(modifier = Modifier.height(24.dp))

                MenuButton(
                    label = "PLAY AGAIN",
                    icon = Icons.Default.Refresh,
                    brush = Brush.horizontalGradient(listOf(Color(0xFFF59E0B), Color(0xFFEA580C))),
                    textColor = Color.White,
                    testTag = "play_again_button",
                    onClick = onPlayAgain
                )

                Spacer(modifier = Modifier.height(10.dp))

                MenuButton(
                    label = "MAIN MENU",
                    icon = Icons.Default.Home,
                    brush = Brush.horizontalGradient(listOf(Color(0xFF334155), Color(0xFF475569))),
                    textColor = Color.White,
                    testTag = "main_menu_button",
                    onClick = onMainMenu
                )
            }
        }
    }
}

@Composable
fun DialogStatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 14.sp, color = Color(0xFF94A3B8))
        Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

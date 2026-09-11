package com.example.farmblaster.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.farmblaster.data.GamePreferences

@Composable
fun MainMenuScreen(
    prefs: GamePreferences,
    onPlayClicked: () -> Unit,
    onHowToPlayClicked: () -> Unit,
    onHighScoresClicked: () -> Unit,
    onSettingsClicked: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "menuAnim")
    val buttonGlow by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "buttonGlow"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF070913),
                        Color(0xFF130E26),
                        Color(0xFF1F1238)
                    )
                )
            )
            .testTag("main_menu_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
        ) {
            // High Score Banner
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1E293B).copy(alpha = 0.85f))
                    .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🏆 HIGH SCORE: ",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF59E0B)
                    )
                    Text(
                        text = "${prefs.highScore}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Hero Logo Badge
            Box(
                modifier = Modifier
                    .size(105.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color(0xFF38BDF8), CircleShape)
                    .shadow(16.dp, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.chicken_invaders_icon),
                    contentDescription = "Game Icon",
                    modifier = Modifier.size(105.dp).clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Game Titles
            Text(
                text = "CHICKEN INVADERS",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFFDE047),
                textAlign = TextAlign.Center,
                letterSpacing = 2.sp
            )
            Text(
                text = "FARM BLASTER",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF38BDF8),
                textAlign = TextAlign.Center,
                letterSpacing = 4.sp
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Menu Buttons
            MenuButton(
                label = "PLAY GAME",
                icon = Icons.Default.PlayArrow,
                brush = Brush.horizontalGradient(listOf(Color(0xFFF59E0B), Color(0xFFEA580C))),
                textColor = Color.White,
                modifier = Modifier.scale(buttonGlow),
                testTag = "play_button",
                onClick = onPlayClicked
            )

            Spacer(modifier = Modifier.height(14.dp))

            MenuButton(
                label = "HOW TO PLAY",
                icon = Icons.Default.HelpOutline,
                brush = Brush.horizontalGradient(listOf(Color(0xFF1E293B), Color(0xFF334155))),
                textColor = Color(0xFFE2E8F0),
                testTag = "how_to_play_button",
                onClick = onHowToPlayClicked
            )

            Spacer(modifier = Modifier.height(14.dp))

            MenuButton(
                label = "HIGH SCORES",
                icon = Icons.Default.Leaderboard,
                brush = Brush.horizontalGradient(listOf(Color(0xFF1E293B), Color(0xFF334155))),
                textColor = Color(0xFFE2E8F0),
                testTag = "high_scores_button",
                onClick = onHighScoresClicked
            )

            Spacer(modifier = Modifier.height(14.dp))

            MenuButton(
                label = "SETTINGS",
                icon = Icons.Default.Settings,
                brush = Brush.horizontalGradient(listOf(Color(0xFF1E293B), Color(0xFF334155))),
                textColor = Color(0xFFE2E8F0),
                testTag = "settings_button",
                onClick = onSettingsClicked
            )
        }
    }
}

@Composable
fun MenuButton(
    label: String,
    icon: ImageVector,
    brush: Brush,
    textColor: Color,
    modifier: Modifier = Modifier,
    testTag: String = "",
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(brush)
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                letterSpacing = 1.2.sp
            )
        }
    }
}

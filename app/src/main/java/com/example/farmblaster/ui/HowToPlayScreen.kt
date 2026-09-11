package com.example.farmblaster.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HowToPlayScreen(onBackClicked: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF070913), Color(0xFF130E26), Color(0xFF1F1238))
                )
            )
            .testTag("how_to_play_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = onBackClicked,
                    modifier = Modifier.testTag("back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "HOW TO PLAY",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFDE047),
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                GuideCard(
                    title = "🚀 CONTROLS & STEERING",
                    accentColor = Color(0xFF38BDF8),
                    description = "• Auto-Fire Blasters: Your spaceship continuously fires high-energy blasters from the start!\n• Drag Anywhere: Slide your finger anywhere on screen to smoothly glide and maneuver your Harvester-9 spacecraft.\n• Double-Tap: Quick double-tap anywhere on screen to unleash an emergency screen-clearing bomb!"
                )

                Spacer(modifier = Modifier.height(14.dp))

                GuideCard(
                    title = "🥚 FALLING EGGS & HAZARDS",
                    accentColor = Color(0xFFF59E0B),
                    description = "• Normal Eggs: Standard fall speed.\n• Fast Eggs: Rapid aerodynamic drop.\n• Large Eggs: Heavy brown eggs with wider splash.\n• Explosive Eggs: Glowing pulsating eggs that detonate into fiery shockwaves!\n• Chickens: Don't let them reach the bottom!"
                )

                Spacer(modifier = Modifier.height(14.dp))

                GuideCard(
                    title = "⚡ WEAPONS & POWER-UPS",
                    accentColor = Color(0xFF4ADE80),
                    description = "Destroying chickens spawns valuable power-up capsules:\n• 2X / 3X: Double & Triple Lasers\n• RF: Rapid Fire Vulcan slug cannon\n• SP: 5-Way Plasma Spread\n• SH: Shimmering Energy Shield (deflects eggs!)\n• 💣: Extra Bio-Nuclear Bomb\n• HP / +1: Hull Repair & Extra Life\n• ★: 2X Score Multiplier"
                )

                Spacer(modifier = Modifier.height(14.dp))

                GuideCard(
                    title = "🔥 COMBOS & BOSS WAVES",
                    accentColor = Color(0xFFEF4444),
                    description = "• Chaining kills quickly boosts your Combo Multiplier up to 10x for massive bonus points!\n• Every 5th wave introduces the colossal 'Mother Clucker' boss with 3 evolving rage phases and heavy barrage attacks!"
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun GuideCard(
    title: String,
    accentColor: Color,
    description: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1E293B).copy(alpha = 0.85f))
            .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(18.dp)
    ) {
        Column {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFFCBD5E1),
                lineHeight = 20.sp
            )
        }
    }
}

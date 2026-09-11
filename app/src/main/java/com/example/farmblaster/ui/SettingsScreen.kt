package com.example.farmblaster.ui

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.farmblaster.audio.ArcadeAudioManager
import com.example.farmblaster.data.GamePreferences

@Composable
fun SettingsScreen(
    prefs: GamePreferences,
    audioManager: ArcadeAudioManager,
    onBackClicked: () -> Unit
) {
    var musicOn by remember { mutableStateOf(prefs.isMusicEnabled) }
    var sfxOn by remember { mutableStateOf(prefs.isSfxEnabled) }
    var vibrateOn by remember { mutableStateOf(prefs.isVibrationEnabled) }
    var showResetDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF070913), Color(0xFF130E26), Color(0xFF1F1238))
                )
            )
            .testTag("settings_screen")
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
                    text = "SETTINGS",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFDE047),
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Settings Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1E293B).copy(alpha = 0.85f))
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column {
                    SettingToggleRow(
                        title = "Background Music",
                        subtitle = "Retro procedural chiptune synth soundtrack",
                        checked = musicOn,
                        onCheckedChange = {
                            musicOn = it
                            prefs.isMusicEnabled = it
                            if (it) audioManager.startMusic() else audioManager.stopMusic()
                        }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    SettingToggleRow(
                        title = "Sound Effects",
                        subtitle = "Lasers, clucks, explosions, and power-up SFX",
                        checked = sfxOn,
                        onCheckedChange = {
                            sfxOn = it
                            prefs.isSfxEnabled = it
                        }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    SettingToggleRow(
                        title = "Haptic Vibration",
                        subtitle = "Feel tactile feedback on hits and bomb blast",
                        checked = vibrateOn,
                        onCheckedChange = {
                            vibrateOn = it
                            prefs.isVibrationEnabled = it
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Danger Zone: Reset High Score
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF450A0A).copy(alpha = 0.75f))
                    .border(1.dp, Color(0xFFDC2626).copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                    .clickable { showResetDialog = true }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "RESET SCORES & STATISTICS",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFCA5A5),
                    letterSpacing = 1.sp
                )
            }
        }

        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = { Text("Reset All Progress?") },
                text = { Text("This will clear your high score, best combo, and total kill counts. This action cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            prefs.resetHighScores()
                            showResetDialog = false
                        }
                    ) {
                        Text("Reset", color = Color(0xFFEF4444))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text("Cancel", color = Color.White)
                    }
                },
                containerColor = Color(0xFF1E293B),
                titleContentColor = Color.White,
                textContentColor = Color(0xFFCBD5E1)
            )
        }
    }
}

@Composable
fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF94A3B8)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFFF59E0B),
                uncheckedThumbColor = Color(0xFF94A3B8),
                uncheckedTrackColor = Color(0xFF334155)
            )
        )
    }
}

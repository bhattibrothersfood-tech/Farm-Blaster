package com.example.farmblaster.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.farmblaster.data.GamePreferences
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.PI
import kotlin.math.sin

/**
 * Procedural arcade sound synthesizer and audio manager.
 * Provides original retro-arcade sound effects, energetic music, and haptic feedback.
 */
class ArcadeAudioManager(private val context: Context, private val prefs: GamePreferences) {

    private val sfxExecutor: ExecutorService = Executors.newFixedThreadPool(4)
    private var musicThread: Thread? = null
    @Volatile private var isMusicRunning = false

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        manager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    // Pre-synthesized audio buffers for instant zero-latency playback
    private val sampleRate = 22050
    private var shootPcm: ShortArray = generateSweep(sampleRate, 850f, 220f, 0.08f, 0.8f)
    private var rapidShootPcm: ShortArray = generateSweep(sampleRate, 1100f, 400f, 0.05f, 0.6f)
    private var spreadShootPcm: ShortArray = generateSweep(sampleRate, 600f, 180f, 0.11f, 0.9f)
    private var chickenHitPcm: ShortArray = generateChickenSquawk(sampleRate, 0.07f)
    private var chickenDeathPcm: ShortArray = generateChickenDeath(sampleRate, 0.16f)
    private var eggDropPcm: ShortArray = generateSweep(sampleRate, 300f, 150f, 0.09f, 0.4f)
    private var eggSplatPcm: ShortArray = generateEggSplat(sampleRate, 0.12f)
    private var playerHitPcm: ShortArray = generatePlayerDamage(sampleRate, 0.22f)
    private var shieldPcm: ShortArray = generateShieldChime(sampleRate, 0.18f)
    private var powerUpPcm: ShortArray = generatePowerUpArpeggio(sampleRate)
    private var bombPcm: ShortArray = generateBombExplosion(sampleRate, 0.65f)
    private var explosionPcm: ShortArray = generateExplosion(sampleRate, 0.32f)
    private var bossRoarPcm: ShortArray = generateBossRoar(sampleRate, 0.5f)
    private var clickPcm: ShortArray = generateClick(sampleRate, 0.03f)

    fun playShoot(isRapid: Boolean = false, isSpread: Boolean = false) {
        if (!prefs.isSfxEnabled) return
        val pcm = when {
            isRapid -> rapidShootPcm
            isSpread -> spreadShootPcm
            else -> shootPcm
        }
        playPcm(pcm)
    }

    fun playChickenHit() {
        if (!prefs.isSfxEnabled) return
        playPcm(chickenHitPcm)
        vibrate(15)
    }

    fun playChickenDeath() {
        if (!prefs.isSfxEnabled) return
        playPcm(chickenDeathPcm)
        vibrate(25)
    }

    fun playEggDrop() {
        if (!prefs.isSfxEnabled) return
        playPcm(eggDropPcm)
    }

    fun playEggSplat() {
        if (!prefs.isSfxEnabled) return
        playPcm(eggSplatPcm)
    }

    fun playPlayerHit() {
        if (!prefs.isSfxEnabled) return
        playPcm(playerHitPcm)
        vibrate(70)
    }

    fun playShieldDeflect() {
        if (!prefs.isSfxEnabled) return
        playPcm(shieldPcm)
        vibrate(20)
    }

    fun playPowerUp() {
        if (!prefs.isSfxEnabled) return
        playPcm(powerUpPcm)
        vibrate(40)
    }

    fun playBomb() {
        if (!prefs.isSfxEnabled) return
        playPcm(bombPcm)
        vibrate(250)
    }

    fun playExplosion() {
        if (!prefs.isSfxEnabled) return
        playPcm(explosionPcm)
        vibrate(50)
    }

    fun playBossRoar() {
        if (!prefs.isSfxEnabled) return
        playPcm(bossRoarPcm)
        vibrate(120)
    }

    fun playButtonClick() {
        if (!prefs.isSfxEnabled) return
        playPcm(clickPcm)
        vibrate(10)
    }

    private fun playPcm(pcm: ShortArray) {
        sfxExecutor.execute {
            try {
                val bufferSize = pcm.size * 2
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(pcm, 0, pcm.size)
                track.play()
                // Let static track complete then release
                Thread.sleep((pcm.size * 1000L / sampleRate) + 50)
                track.stop()
                track.release()
            } catch (_: Exception) {
            }
        }
    }

    fun startMusic() {
        if (isMusicRunning || !prefs.isMusicEnabled) return
        isMusicRunning = true
        musicThread = Thread {
            runMusicLoop()
        }.apply {
            isDaemon = true
            name = "ArcadeMusicThread"
            start()
        }
    }

    fun stopMusic() {
        isMusicRunning = false
        musicThread?.interrupt()
        musicThread = null
    }

    fun setMusicEnabled(enabled: Boolean) {
        prefs.isMusicEnabled = enabled
        if (enabled) {
            startMusic()
        } else {
            stopMusic()
        }
    }

    fun vibrate(durationMs: Long) {
        if (!prefs.isVibrationEnabled || vibrator == null || !vibrator.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(durationMs)
            }
        } catch (_: Exception) {
        }
    }

    private fun runMusicLoop() {
        val musicSampleRate = 22050
        val minBufferSize = AudioTrack.getMinBufferSize(
            musicSampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val track = try {
            AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(musicSampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(minBufferSize.coerceAtLeast(4096))
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
        } catch (e: Exception) {
            return
        }

        track.play()

        // Energetic arcade chiptune track notes (Frequencies in Hz)
        // Bassline & Lead arpeggio sequence in A-minor / D-minor heroic theme
        val bassNotes = floatArrayOf(
            110f, 110f, 130.8f, 110f, 146.8f, 130.8f, 110f, 123.5f,
            98f, 98f, 123.5f, 98f, 130.8f, 123.5f, 98f, 110f,
            87.3f, 87.3f, 110f, 87.3f, 130.8f, 110f, 87.3f, 98f,
            110f, 130.8f, 146.8f, 164.8f, 196f, 164.8f, 146.8f, 130.8f
        )
        val leadArp = floatArrayOf(
            440f, 523.25f, 659.25f, 880f, 659.25f, 523.25f, 440f, 523.25f,
            392f, 493.88f, 587.33f, 783.99f, 587.33f, 493.88f, 392f, 493.88f,
            349.23f, 440f, 523.25f, 698.46f, 523.25f, 440f, 349.23f, 440f,
            440f, 587.33f, 659.25f, 880f, 987.77f, 880f, 659.25f, 587.33f
        )

        val noteDuration = (musicSampleRate * 0.135f).toInt() // Tempo ~110 BPM 16th notes
        val buffer = ShortArray(noteDuration)
        var step = 0

        try {
            while (isMusicRunning && !Thread.currentThread().isInterrupted) {
                val bassFreq = bassNotes[step % bassNotes.size]
                val leadFreq = leadArp[step % leadArp.size]
                val isKickBeat = (step % 4 == 0)
                val isSnareBeat = (step % 8 == 4)

                for (i in 0 until noteDuration) {
                    val t = i.toFloat() / musicSampleRate
                    val progress = i.toFloat() / noteDuration
                    val envelope = (1f - progress * 0.7f).coerceIn(0f, 1f)

                    // Square / pulse bass
                    val bassPhase = (t * bassFreq) % 1f
                    val bass = if (bassPhase < 0.45f) 0.35f else -0.35f

                    // Triangle / sine lead arpeggio
                    val leadPhase = (t * leadFreq) * 2 * PI.toFloat()
                    val lead = sin(leadPhase) * 0.28f * envelope

                    // Retro percussion (kick low-frequency drop + snare noise crackle)
                    var drum = 0f
                    if (isKickBeat && progress < 0.35f) {
                        val kickFreq = 140f * (1f - progress / 0.35f) + 40f
                        drum += sin(t * kickFreq * 2 * PI.toFloat()) * 0.45f * (1f - progress / 0.35f)
                    }
                    if (isSnareBeat && progress < 0.25f) {
                        val noise = (Math.random().toFloat() * 2f - 1f)
                        drum += noise * 0.25f * (1f - progress / 0.25f)
                    }

                    val mixed = (bass + lead + drum).coerceIn(-0.95f, 0.95f)
                    buffer[i] = (mixed * 32767 * 0.45f).toInt().toShort()
                }

                track.write(buffer, 0, buffer.size)
                step++
            }
        } catch (_: InterruptedException) {
        } finally {
            try {
                track.stop()
                track.release()
            } catch (_: Exception) {
            }
        }
    }

    fun release() {
        stopMusic()
        sfxExecutor.shutdownNow()
    }

    // --- Audio Synthesis Algorithms ---

    private fun generateSweep(sr: Int, startFreq: Float, endFreq: Float, durationSec: Float, volume: Float): ShortArray {
        val totalSamples = (sr * durationSec).toInt()
        val data = ShortArray(totalSamples)
        var phase = 0.0
        for (i in 0 until totalSamples) {
            val progress = i.toDouble() / totalSamples
            val freq = startFreq + (endFreq - startFreq) * progress
            phase += 2.0 * PI * freq / sr
            val amp = (1.0 - progress) * volume
            data[i] = (sin(phase) * amp * 32767.0).toInt().toShort()
        }
        return data
    }

    private fun generateChickenSquawk(sr: Int, durationSec: Float): ShortArray {
        val totalSamples = (sr * durationSec).toInt()
        val data = ShortArray(totalSamples)
        var phase = 0.0
        for (i in 0 until totalSamples) {
            val p = i.toDouble() / totalSamples
            // Warbling frequency modulation simulating realistic squawk
            val freq = 420.0 + sin(p * 28.0) * 120.0 - p * 150.0
            phase += 2.0 * PI * freq / sr
            val amp = sin(p * PI) * 0.75
            data[i] = (sin(phase) * amp * 32767.0).toInt().toShort()
        }
        return data
    }

    private fun generateChickenDeath(sr: Int, durationSec: Float): ShortArray {
        val totalSamples = (sr * durationSec).toInt()
        val data = ShortArray(totalSamples)
        var phase = 0.0
        for (i in 0 until totalSamples) {
            val p = i.toDouble() / totalSamples
            val freq = 650.0 * (1.0 - p * 0.7) + (Math.random() - 0.5) * 60.0
            phase += 2.0 * PI * freq / sr
            val amp = (1.0 - p) * 0.8
            val noise = (Math.random() - 0.5) * 0.2
            val sample = (sin(phase) + noise) * amp
            data[i] = (sample.coerceIn(-1.0, 1.0) * 32767.0).toInt().toShort()
        }
        return data
    }

    private fun generateEggSplat(sr: Int, durationSec: Float): ShortArray {
        val totalSamples = (sr * durationSec).toInt()
        val data = ShortArray(totalSamples)
        var phase = 0.0
        for (i in 0 until totalSamples) {
            val p = i.toDouble() / totalSamples
            val freq = 280.0 * (1.0 - p * 0.8)
            phase += 2.0 * PI * freq / sr
            val noise = (Math.random() - 0.5) * 0.6
            val amp = (1.0 - p) * 0.7
            val sample = (sin(phase) * 0.4 + noise * 0.6) * amp
            data[i] = (sample.coerceIn(-1.0, 1.0) * 32767.0).toInt().toShort()
        }
        return data
    }

    private fun generatePlayerDamage(sr: Int, durationSec: Float): ShortArray {
        val totalSamples = (sr * durationSec).toInt()
        val data = ShortArray(totalSamples)
        var phase = 0.0
        for (i in 0 until totalSamples) {
            val p = i.toDouble() / totalSamples
            val freq = 180.0 - p * 110.0
            phase += 2.0 * PI * freq / sr
            val noise = (Math.random() - 0.5) * 0.5
            val amp = (1.0 - p) * 0.9
            data[i] = ((sin(phase) * 0.5 + noise * 0.5) * amp * 32767.0).toInt().toShort()
        }
        return data
    }

    private fun generateShieldChime(sr: Int, durationSec: Float): ShortArray {
        val totalSamples = (sr * durationSec).toInt()
        val data = ShortArray(totalSamples)
        var phase1 = 0.0
        var phase2 = 0.0
        for (i in 0 until totalSamples) {
            val p = i.toDouble() / totalSamples
            phase1 += 2.0 * PI * 1174.66 / sr // D6
            phase2 += 2.0 * PI * 1760.0 / sr   // A6
            val amp = (1.0 - p) * 0.6
            val sample = (sin(phase1) * 0.6 + sin(phase2) * 0.4) * amp
            data[i] = (sample * 32767.0).toInt().toShort()
        }
        return data
    }

    private fun generatePowerUpArpeggio(sr: Int): ShortArray {
        val notes = floatArrayOf(523.25f, 659.25f, 783.99f, 1046.5f) // C5, E5, G5, C6
        val noteSamples = (sr * 0.07f).toInt()
        val totalSamples = noteSamples * notes.size
        val data = ShortArray(totalSamples)
        for (n in notes.indices) {
            val freq = notes[n]
            var phase = 0.0
            val offset = n * noteSamples
            for (i in 0 until noteSamples) {
                val p = i.toDouble() / noteSamples
                phase += 2.0 * PI * freq / sr
                val amp = (1.0 - p * 0.4) * 0.7
                data[offset + i] = (sin(phase) * amp * 32767.0).toInt().toShort()
            }
        }
        return data
    }

    private fun generateExplosion(sr: Int, durationSec: Float): ShortArray {
        val totalSamples = (sr * durationSec).toInt()
        val data = ShortArray(totalSamples)
        var lowPass = 0.0
        for (i in 0 until totalSamples) {
            val p = i.toDouble() / totalSamples
            val rawNoise = Math.random() * 2.0 - 1.0
            // Low-pass filter for thunderous explosion sound
            lowPass += (rawNoise - lowPass) * 0.12
            val amp = (1.0 - p) * (1.0 - p) * 0.9
            data[i] = (lowPass * amp * 32767.0).toInt().toShort()
        }
        return data
    }

    private fun generateBombExplosion(sr: Int, durationSec: Float): ShortArray {
        val totalSamples = (sr * durationSec).toInt()
        val data = ShortArray(totalSamples)
        var lowPass = 0.0
        var subPhase = 0.0
        for (i in 0 until totalSamples) {
            val p = i.toDouble() / totalSamples
            val rawNoise = Math.random() * 2.0 - 1.0
            lowPass += (rawNoise - lowPass) * 0.08
            val subFreq = 65.0 * (1.0 - p * 0.6)
            subPhase += 2.0 * PI * subFreq / sr
            val amp = (1.0 - p) * 0.95
            val sample = (lowPass * 0.6 + sin(subPhase) * 0.4) * amp
            data[i] = (sample.coerceIn(-1.0, 1.0) * 32767.0).toInt().toShort()
        }
        return data
    }

    private fun generateBossRoar(sr: Int, durationSec: Float): ShortArray {
        val totalSamples = (sr * durationSec).toInt()
        val data = ShortArray(totalSamples)
        var phase = 0.0
        for (i in 0 until totalSamples) {
            val p = i.toDouble() / totalSamples
            val freq = 160.0 + sin(p * 22.0) * 45.0
            phase += 2.0 * PI * freq / sr
            val noise = (Math.random() - 0.5) * 0.4
            val amp = sin(p * PI) * 0.9
            val sample = (sin(phase) * 0.6 + noise * 0.4) * amp
            data[i] = (sample.coerceIn(-1.0, 1.0) * 32767.0).toInt().toShort()
        }
        return data
    }

    private fun generateClick(sr: Int, durationSec: Float): ShortArray {
        val totalSamples = (sr * durationSec).toInt()
        val data = ShortArray(totalSamples)
        var phase = 0.0
        for (i in 0 until totalSamples) {
            val p = i.toDouble() / totalSamples
            phase += 2.0 * PI * 1800.0 / sr
            val amp = (1.0 - p) * 0.5
            data[i] = (sin(phase) * amp * 32767.0).toInt().toShort()
        }
        return data
    }
}

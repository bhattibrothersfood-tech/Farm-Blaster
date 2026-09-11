package com.example.farmblaster.engine

import android.content.Context
import android.graphics.Canvas
import com.example.farmblaster.audio.ArcadeAudioManager
import com.example.farmblaster.data.GamePreferences
import com.example.farmblaster.model.BossChicken
import com.example.farmblaster.model.Chicken
import com.example.farmblaster.model.Egg
import com.example.farmblaster.model.GameState
import com.example.farmblaster.model.Player
import com.example.farmblaster.model.PlayerProjectile
import com.example.farmblaster.model.PowerUpItem
import com.example.farmblaster.render.BackgroundRenderer
import com.example.farmblaster.render.ChickenRenderer
import com.example.farmblaster.render.EggRenderer
import com.example.farmblaster.render.PlayerRenderer
import com.example.farmblaster.system.CollisionManager
import com.example.farmblaster.system.ParticleManager
import com.example.farmblaster.system.PowerUpManager
import com.example.farmblaster.system.ProjectileManager
import com.example.farmblaster.system.WaveManager

/**
 * Central game engine orchestrating state, game loops, scoring, combos, and rendering.
 */
class GameEngine(
    val context: Context,
    val prefs: GamePreferences,
    val audioManager: ArcadeAudioManager
) {
    var screenWidth = 1080f
    var screenHeight = 1920f

    var gameState: GameState = GameState.PLAYING

    // Entities
    val player = Player()
    val chickens = mutableListOf<Chicken>()
    val boss = BossChicken()
    val playerProjectiles = mutableListOf<PlayerProjectile>()
    val eggs = mutableListOf<Egg>()
    val powerUps = mutableListOf<PowerUpItem>()

    // Systems
    val particleManager = ParticleManager()
    val powerUpManager = PowerUpManager(powerUps)
    val projectileManager = ProjectileManager(playerProjectiles, eggs, audioManager)
    val waveManager = WaveManager(chickens, boss)
    val collisionManager = CollisionManager(audioManager, particleManager)

    // Renderers
    val backgroundRenderer = BackgroundRenderer()
    val chickenRenderer = ChickenRenderer()
    val playerRenderer = PlayerRenderer()
    val eggRenderer = EggRenderer()

    // Scoring & Stats
    var score = 0
    var combo = 1
    var comboTimer = 0f
    val comboMaxDuration = 2.4f
    var enemiesKilledInGame = 0

    // Controls state - continuous firing by default
    var isFiring = true

    // State change callbacks for Compose UI
    var onGameStateChanged: ((GameState) -> Unit)? = null
    var onScoreUpdated: ((Int, Int) -> Unit)? = null // score, combo
    var onStatsUpdated: ((Int, Int, Int) -> Unit)? = null // lives, bombs, wave
    var onWaveComplete: ((wave: Int, bonus: Int, kills: Int, comboBonus: Int) -> Unit)? = null

    init {
        setupCollisionListeners()
    }

    fun initDimensions(width: Float, height: Float) {
        screenWidth = width
        screenHeight = height
        player.reset(screenWidth, screenHeight)
        startNewGame()
    }

    private fun setupCollisionListeners() {
        collisionManager.onScoreAdded = { baseScore ->
            val multipliedScore = baseScore * combo
            score += multipliedScore
            prefs.updateHighScoreIfNeeded(score)
            onScoreUpdated?.invoke(score, combo)
        }

        collisionManager.onChickenKilled = { chicken ->
            powerUpManager.checkSpawnOnKill(chicken)
            enemiesKilledInGame++
            waveManager.waveKills++
            prefs.incrementKills(1)

            // Increment combo
            combo++
            comboTimer = comboMaxDuration
            prefs.updateBestComboIfNeeded(combo)

            if (combo > 2) {
                particleManager.addFloatingText(
                    chicken.x, chicken.y - 30f,
                    "COMBO x$combo!",
                    0xFFFF9800.toInt(),
                    1.2f
                )
            }
            onScoreUpdated?.invoke(score, combo)
        }

        collisionManager.onBossKilled = {
            prefs.incrementBosses()
            waveManager.isWaveTransitioning = true
        }

        collisionManager.onPlayerHit = {
            // Reset combo on player hit
            combo = 1
            comboTimer = 0f
            waveManager.noHitWave = false
            onStatsUpdated?.invoke(player.lives, player.bombs, waveManager.currentWave)
            onScoreUpdated?.invoke(score, combo)

            if (player.lives <= 0) {
                audioManager.playExplosion()
                setEngineState(GameState.GAME_OVER)
            }
        }
    }

    fun startNewGame() {
        score = 0
        combo = 1
        comboTimer = 0f
        enemiesKilledInGame = 0
        player.reset(screenWidth, screenHeight)
        powerUpManager.clear()
        projectileManager.clear()
        particleManager.clear()

        waveManager.startWave(1, screenWidth, screenHeight)
        isFiring = true
        setEngineState(GameState.PLAYING)
        audioManager.startMusic()
        onStatsUpdated?.invoke(player.lives, player.bombs, waveManager.currentWave)
        onScoreUpdated?.invoke(score, combo)
    }

    fun nextWave() {
        val nextW = waveManager.currentWave + 1
        prefs.updateHighestWaveIfNeeded(nextW)
        powerUpManager.clear()
        projectileManager.clear()
        waveManager.startWave(nextW, screenWidth, screenHeight)
        isFiring = true
        setEngineState(GameState.PLAYING)
        onStatsUpdated?.invoke(player.lives, player.bombs, waveManager.currentWave)
    }

    fun triggerBomb() {
        if (player.bombs <= 0 || gameState != GameState.PLAYING) return
        player.bombs--
        collisionManager.triggerScreenClearingBomb(chickens, boss, eggs, screenWidth, screenHeight)
        onStatsUpdated?.invoke(player.lives, player.bombs, waveManager.currentWave)
    }

    fun movePlayer(touchX: Float, touchY: Float? = null) {
        if (gameState != GameState.PLAYING) return
        val halfW = player.width / 2f
        val halfH = player.height / 2f
        player.x = touchX.coerceIn(halfW + 10f, screenWidth - halfW - 10f)
        if (touchY != null) {
            val targetY = touchY - 45f
            player.y = targetY.coerceIn(screenHeight * 0.35f, screenHeight - halfH - 15f)
        }
    }

    fun update(dt: Float) {
        if (gameState != GameState.PLAYING) return

        // 1. Combo timer decay
        if (combo > 1) {
            comboTimer -= dt
            if (comboTimer <= 0f) {
                combo = 1
                onScoreUpdated?.invoke(score, combo)
            }
        }

        // 2. Player update & continuous firing from start
        player.update(dt)
        if (gameState == GameState.PLAYING && !player.isDestroyed) {
            projectileManager.firePlayerWeapon(player)
        }

        // 3. Update Background
        backgroundRenderer.update(dt, screenWidth, screenHeight)

        // 4. Update Wave & Formations
        waveManager.update(dt, screenWidth, screenHeight)

        // 5. Update Projectiles & Eggs
        projectileManager.updateEggsAndAttacks(dt, chickens, boss, screenHeight, waveManager.currentWave)

        // 6. Update Power-ups
        powerUpManager.update(dt, screenHeight)

        // 7. Check Collisions
        collisionManager.checkCollisions(
            player,
            playerProjectiles,
            chickens,
            boss,
            eggs,
            powerUps,
            screenWidth,
            screenHeight
        )

        // 8. Update Particles & Screen Shake
        particleManager.update(dt)

        // 9. Check Wave Completion
        val allWaveEnemiesDead = if (boss.isActive) {
            !boss.isAlive
        } else {
            waveManager.waveTotalEnemies > 0 && chickens.none { it.isAlive }
        }

        if (allWaveEnemiesDead && gameState == GameState.PLAYING) {
            handleWaveCompleted()
        }
    }

    private fun handleWaveCompleted() {
        waveManager.isWaveTransitioning = false
        val waveBonus = waveManager.currentWave * 1000
        val noHitBonus = if (waveManager.noHitWave) 2500 else 0
        val comboBonus = combo * 300
        val totalBonus = waveBonus + noHitBonus + comboBonus

        score += totalBonus
        prefs.updateHighScoreIfNeeded(score)
        audioManager.playPowerUp()

        onWaveComplete?.invoke(
            waveManager.currentWave,
            totalBonus,
            waveManager.waveKills,
            comboBonus
        )
        setEngineState(GameState.LEVEL_COMPLETE)
    }

    fun render(canvas: Canvas) {
        canvas.save()

        // Apply screen shake
        if (particleManager.screenShakeDuration > 0f) {
            canvas.translate(particleManager.shakeOffsetX, particleManager.shakeOffsetY)
        }

        // 1. Background
        backgroundRenderer.render(canvas, screenWidth, screenHeight)

        // 2. Power-ups
        for (pw in powerUps) {
            eggRenderer.renderPowerUp(canvas, pw)
        }

        // 3. Chickens & Boss
        for (c in chickens) {
            chickenRenderer.renderChicken(canvas, c)
        }
        if (boss.isActive) {
            chickenRenderer.renderBoss(canvas, boss, screenWidth)
        }

        // 4. Eggs
        for (e in eggs) {
            eggRenderer.renderEgg(canvas, e)
        }

        // 5. Player Projectiles
        for (p in playerProjectiles) {
            eggRenderer.renderProjectile(canvas, p)
        }

        // 6. Player Ship
        playerRenderer.render(canvas, player)

        // 7. Particles & Floating texts
        particleManager.render(canvas)

        canvas.restore()
    }

    fun pauseGame() {
        if (gameState == GameState.PLAYING) {
            setEngineState(GameState.PAUSED)
            audioManager.stopMusic()
        }
    }

    fun resumeGame() {
        if (gameState == GameState.PAUSED) {
            setEngineState(GameState.PLAYING)
            audioManager.startMusic()
        }
    }

    private fun setEngineState(newState: GameState) {
        gameState = newState
        onGameStateChanged?.invoke(newState)
    }

    fun release() {
        audioManager.release()
    }
}

package com.example.farmblaster.system

import com.example.farmblaster.model.BossChicken
import com.example.farmblaster.model.Chicken
import com.example.farmblaster.model.ChickenBreed
import com.example.farmblaster.model.FormationType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Orchestrates wave progression, diverse formations, and dynamic entrance choreographies.
 * Guarantees chickens stay on screen, loop back seamlessly if diving, and progress cleanly.
 */
class WaveManager(
    private val chickens: MutableList<Chicken>,
    private val boss: BossChicken
) {
    var currentWave = 1
    var currentLevel = 1
    var isWaveTransitioning = false
    var waveTransitionTimer = 0f

    var formationDirection = 1f
    var formationSpeed = 52f
    var formationOscillation = 0f

    var waveKills = 0
    var waveTotalEnemies = 0
    var noHitWave = true

    fun startWave(wave: Int, screenWidth: Float, screenHeight: Float) {
        currentWave = wave
        currentLevel = ((wave - 1) / 5) + 1
        chickens.clear()
        boss.isAlive = false
        boss.isActive = false
        isWaveTransitioning = false
        waveTransitionTimer = 0f
        waveKills = 0
        noHitWave = true
        formationDirection = 1f
        formationOscillation = 0f

        val isBossWave = (wave % 5 == 0)

        if (isBossWave) {
            setupBossWave(wave, screenWidth, screenHeight)
        } else {
            setupFormationWave(wave, screenWidth, screenHeight)
        }
    }

    private fun setupBossWave(wave: Int, screenWidth: Float, screenHeight: Float) {
        val bossHp = 120 + (currentLevel - 1) * 80
        boss.x = screenWidth / 2f
        boss.y = -220f
        boss.targetX = screenWidth / 2f
        boss.targetY = screenHeight * 0.24f
        boss.hp = bossHp
        boss.maxHp = bossHp
        boss.isAlive = true
        boss.isActive = true
        boss.phase = 1
        boss.attackTimer = 0f
        boss.eggBurstTimer = 0f
        boss.moveTimer = 0f
        boss.sweepDirection = 1f

        waveTotalEnemies = 1
    }

    private fun setupFormationWave(wave: Int, screenWidth: Float, screenHeight: Float) {
        val formation = when (wave % 8) {
            1 -> FormationType.HORIZONTAL_LINE
            2 -> FormationType.V_SHAPE
            3 -> FormationType.GRID
            4 -> FormationType.DIAMOND
            5 -> FormationType.CIRCLE
            6 -> FormationType.ZIG_ZAG
            7 -> FormationType.WAVE_SWEEP
            else -> FormationType.SPIRAL_SWARM
        }

        val availableBreeds = mutableListOf(ChickenBreed.WHITE)
        if (wave >= 2) availableBreeds.add(ChickenBreed.BROWN)
        if (wave >= 3) {
            availableBreeds.add(ChickenBreed.BLACK)
            availableBreeds.add(ChickenBreed.FAST)
        }
        if (wave >= 4) {
            availableBreeds.add(ChickenBreed.ARMORED)
            availableBreeds.add(ChickenBreed.EXPLOSIVE)
        }
        if (wave >= 6) {
            availableBreeds.add(ChickenBreed.HEAVY)
            availableBreeds.add(ChickenBreed.ELITE)
        }
        if (wave % 3 == 0) {
            availableBreeds.add(ChickenBreed.GOLDEN)
        }

        val rows = (3 + (wave / 4).coerceAtMost(2))
        val cols = (5 + (wave / 3).coerceAtMost(2))

        val startY = screenHeight * 0.15f
        var chickenId = 0

        when (formation) {
            FormationType.HORIZONTAL_LINE, FormationType.GRID -> {
                val spacingX = screenWidth / (cols + 1f)
                val spacingY = 72f
                for (r in 0 until rows) {
                    for (c in 0 until cols) {
                        val breed = chooseBreed(r, c, availableBreeds)
                        val targetX = spacingX * (c + 1)
                        val targetY = startY + r * spacingY
                        addEnteringChicken(chickenId++, targetX, targetY, breed, screenWidth)
                    }
                }
            }
            FormationType.V_SHAPE -> {
                val count = (rows * cols * 0.75f).toInt().coerceAtLeast(10)
                val midX = screenWidth / 2f
                for (i in 0 until count) {
                    val side = if (i % 2 == 0) 1f else -1f
                    val tier = (i / 2) + 1
                    val targetX = midX + side * (tier * 48f)
                    val targetY = startY + (tier * 42f)
                    val breed = if (tier == 1) ChickenBreed.ELITE else availableBreeds.random()
                    addEnteringChicken(chickenId++, targetX, targetY, breed, screenWidth)
                }
            }
            FormationType.DIAMOND -> {
                val half = rows
                for (r in 0 until half * 2) {
                    val widthInRow = if (r < half) r + 1 else half * 2 - r - 1
                    for (c in 0 until widthInRow) {
                        val targetX = screenWidth / 2f + (c - (widthInRow - 1) / 2f) * 64f
                        val targetY = startY + r * 50f
                        val breed = availableBreeds.random()
                        addEnteringChicken(chickenId++, targetX, targetY, breed, screenWidth)
                    }
                }
            }
            FormationType.CIRCLE -> {
                val radius = screenWidth * 0.28f
                val count = 14 + (wave * 2).coerceAtMost(10)
                for (i in 0 until count) {
                    val angle = (i * 2.0 * PI / count).toFloat()
                    val targetX = screenWidth / 2f + cos(angle) * radius
                    val targetY = startY + radius * 0.8f + sin(angle) * (radius * 0.65f)
                    val breed = if (i % 4 == 0) ChickenBreed.ARMORED else availableBreeds.random()
                    addEnteringChicken(chickenId++, targetX, targetY, breed, screenWidth)
                }
            }
            FormationType.ZIG_ZAG, FormationType.WAVE_SWEEP, FormationType.SPIRAL_SWARM -> {
                val count = 18 + (wave * 2).coerceAtMost(12)
                for (i in 0 until count) {
                    val targetX = (screenWidth * 0.16f) + (i % 6) * (screenWidth * 0.14f)
                    val targetY = startY + (i / 6) * 68f + sin(i.toFloat()) * 16f
                    val breed = availableBreeds.random()
                    addEnteringChicken(chickenId++, targetX, targetY, breed, screenWidth)
                }
            }
        }

        waveTotalEnemies = chickens.size
        formationSpeed = 50f + (wave * 4f).coerceAtMost(90f)
    }

    private fun addEnteringChicken(
        id: Int,
        targetX: Float,
        targetY: Float,
        breed: ChickenBreed,
        screenWidth: Float
    ) {
        val startX = if (id % 2 == 0) -80f else screenWidth + 80f
        val startY = -100f - ((id % 8) * 40f)
        val hp = (breed.baseHp + (currentWave / 3)).coerceAtLeast(1)

        val chicken = Chicken(
            id = id,
            x = startX,
            y = startY,
            targetX = targetX,
            targetY = targetY,
            startX = startX,
            startY = startY,
            breed = breed,
            hp = hp,
            maxHp = hp,
            isAlive = true,
            inFormation = false,
            entranceProgress = 0f,
            oscillationOffset = id * 0.35f,
            diveCooldown = (6f + Math.random().toFloat() * 10f)
        )
        chickens.add(chicken)
    }

    private fun chooseBreed(r: Int, c: Int, available: List<ChickenBreed>): ChickenBreed {
        return when {
            r == 0 && available.contains(ChickenBreed.ELITE) && (c == 2 || c == 3) -> ChickenBreed.ELITE
            r == 0 && available.contains(ChickenBreed.ARMORED) -> ChickenBreed.ARMORED
            r == 1 && available.contains(ChickenBreed.HEAVY) -> ChickenBreed.HEAVY
            else -> available.random()
        }
    }

    fun update(dt: Float, screenWidth: Float, screenHeight: Float) {
        formationOscillation += dt * 3f

        // Update Boss if active
        if (boss.isActive && boss.isAlive) {
            updateBoss(dt, screenWidth, screenHeight)
            return
        }

        // Update Chickens
        val aliveChickens = chickens.filter { it.isAlive }
        if (aliveChickens.isEmpty() && waveTotalEnemies > 0) {
            isWaveTransitioning = true
            return
        }

        // 1. Calculate flock boundaries to reverse sway smoothly without jitter
        val inFormationChickens = aliveChickens.filter { it.inFormation && !it.isDiving }
        if (inFormationChickens.isNotEmpty()) {
            val minX = inFormationChickens.minOf { it.x - it.width / 2f }
            val maxX = inFormationChickens.maxOf { it.x + it.width / 2f }
            val margin = 24f

            if (formationDirection < 0f && minX <= margin) {
                formationDirection = 1f
            } else if (formationDirection > 0f && maxX >= screenWidth - margin) {
                formationDirection = -1f
            }
        }

        val sway = formationSpeed * formationDirection * dt

        for (c in aliveChickens) {
            // Flapping cycle
            c.flapTimer += dt * 10f * c.breed.speedMultiplier
            c.flapFrame = (c.flapTimer.toInt()) % 4

            if (c.hitFlashTimer > 0f) c.hitFlashTimer -= dt

            if (!c.inFormation) {
                // Swoop entrance animation into formation
                c.entranceProgress += dt * 1.5f
                if (c.entranceProgress >= 1f) {
                    c.inFormation = true
                    c.x = c.targetX
                    c.y = c.targetY
                } else {
                    val p = c.entranceProgress
                    val controlX = (c.startX + c.targetX) / 2f + (if (c.id % 2 == 0) 140f else -140f)
                    val controlY = c.targetY - 90f
                    c.x = (1f - p) * (1f - p) * c.startX + 2f * (1f - p) * p * controlX + p * p * c.targetX
                    c.y = (1f - p) * (1f - p) * c.startY + 2f * (1f - p) * p * controlY + p * p * c.targetY
                }
            } else if (c.isDiving) {
                // Dive swoop down towards player
                c.y += (220f + currentWave * 15f) * dt
                c.x += sin(c.flapTimer * 3f) * 75f * dt

                // IF CHICKEN REACHES BOTTOM OF SCREEN: LOOP BACK TO TOP!
                // NEVER gets lost or stuck off screen!
                if (c.y > screenHeight + 50f) {
                    c.y = -60f
                    c.x = c.targetX.coerceIn(50f, screenWidth - 50f)
                    c.isDiving = false
                }
            } else {
                // Normal formation flight
                c.x += sway
                c.targetX += sway
                // Harmonic bobbing hover in upper combat sector
                c.y = c.targetY + sin(formationOscillation + c.oscillationOffset) * 8f

                // Periodic dive attack trigger
                c.diveCooldown -= dt
                if (c.diveCooldown <= 0f) {
                    c.diveCooldown = 7f + (Math.random().toFloat() * 10f)
                    // Maximum 2 chickens diving at a time
                    if (Math.random() < 0.25 && aliveChickens.count { it.isDiving } < 2) {
                        c.isDiving = true
                    }
                }

                // Safety guard: if chicken somehow wanders below screen, immediately return from top
                if (c.y > screenHeight + 40f) {
                    c.y = -60f
                    c.x = c.targetX.coerceIn(50f, screenWidth - 50f)
                    c.targetY = c.targetY.coerceIn(screenHeight * 0.12f, screenHeight * 0.38f)
                    c.isDiving = false
                }
            }
        }
    }

    private fun updateBoss(dt: Float, screenWidth: Float, screenHeight: Float) {
        boss.moveTimer += dt
        boss.flapTimer += dt * 8f
        boss.flapFrame = (boss.flapTimer.toInt()) % 4

        if (boss.hitFlashTimer > 0f) boss.hitFlashTimer -= dt

        // Entrance slide
        if (boss.y < boss.targetY) {
            boss.y += 180f * dt
            return
        }

        // Boss movement pattern based on phase
        val moveSpeed = when (boss.phase) {
            1 -> 110f
            2 -> 160f
            else -> 220f
        }

        boss.x += boss.sweepDirection * moveSpeed * dt
        val margin = boss.width / 2f + 30f
        if (boss.x < margin && boss.sweepDirection < 0f) {
            boss.x = margin
            boss.sweepDirection = 1f
        } else if (boss.x > screenWidth - margin && boss.sweepDirection > 0f) {
            boss.x = screenWidth - margin
            boss.sweepDirection = -1f
        }

        // Figure-8 hover
        boss.y = boss.targetY + sin(boss.moveTimer * 2f) * 35f
    }
}

package com.example.farmblaster.system

import com.example.farmblaster.audio.ArcadeAudioManager
import com.example.farmblaster.model.BossChicken
import com.example.farmblaster.model.Chicken
import com.example.farmblaster.model.Egg
import com.example.farmblaster.model.Player
import com.example.farmblaster.model.PlayerProjectile
import com.example.farmblaster.model.PowerUpItem
import com.example.farmblaster.model.PowerUpType
import kotlin.math.abs
import kotlin.math.hypot

/**
 * High-performance spatial collision detector and event dispatcher.
 */
class CollisionManager(
    private val audioManager: ArcadeAudioManager,
    private val particleManager: ParticleManager
) {

    var onScoreAdded: ((Int) -> Unit)? = null
    var onChickenKilled: ((Chicken) -> Unit)? = null
    var onBossKilled: (() -> Unit)? = null
    var onPlayerHit: (() -> Unit)? = null

    fun checkCollisions(
        player: Player,
        projectiles: MutableList<PlayerProjectile>,
        chickens: MutableList<Chicken>,
        boss: BossChicken,
        eggs: MutableList<Egg>,
        powerUps: MutableList<PowerUpItem>,
        screenWidth: Float,
        screenHeight: Float
    ) {
        // 1. Projectiles vs Chickens
        for (p in projectiles) {
            if (!p.isAlive) continue

            for (c in chickens) {
                if (!c.isAlive) continue

                val dx = abs(p.x - c.x)
                val dy = abs(p.y - c.y)
                val hitDistX = (p.width + c.width) * 0.44f
                val hitDistY = (p.height + c.height) * 0.44f

                if (dx < hitDistX && dy < hitDistY) {
                    val killed = c.takeDamage(p.damage)
                    if (!p.piercesEnemies) {
                        p.isAlive = false
                    }

                    // Sparks
                    particleManager.spawnSparks(p.x, p.y, c.breed.secondaryColor.toInt(), 7)

                    if (killed) {
                        audioManager.playChickenDeath()
                        particleManager.spawnFeatherExplosion(
                            c.x, c.y,
                            c.breed.primaryColor.toInt(),
                            c.breed.secondaryColor.toInt(),
                            16
                        )
                        val scoreGain = c.breed.scoreValue * (if (player.isScore2xActive) 2 else 1)
                        onScoreAdded?.invoke(scoreGain)
                        particleManager.addFloatingText(c.x, c.y, "+$scoreGain", 0xFFFFD700.toInt())
                        onChickenKilled?.invoke(c)

                        // Special: Explosive chicken damages nearby flock
                        if (c.breed.name == "EXPLOSIVE") {
                            triggerAreaDamage(c.x, c.y, 160f, chickens)
                            particleManager.spawnShockwaveRing(c.x, c.y, 0xFFFF4500.toInt(), 40f)
                            audioManager.playExplosion()
                        }
                    } else {
                        audioManager.playChickenHit()
                    }

                    if (!p.piercesEnemies) break
                }
            }

            // Projectiles vs Boss
            if (p.isAlive && boss.isActive && boss.isAlive) {
                val dx = abs(p.x - boss.x)
                val dy = abs(p.y - boss.y)
                if (dx < boss.width * 0.42f && dy < boss.height * 0.42f) {
                    val killed = boss.takeDamage(p.damage)
                    if (!p.piercesEnemies) p.isAlive = false
                    particleManager.spawnSparks(p.x, p.y, 0xFFFFD700.toInt(), 8)

                    if (killed) {
                        audioManager.playExplosion()
                        particleManager.spawnBombDetonation(boss.x, boss.y, screenWidth, screenHeight)
                        val bossScore = 15000 * (if (player.isScore2xActive) 2 else 1)
                        onScoreAdded?.invoke(bossScore)
                        particleManager.addFloatingText(boss.x, boss.y, "BOSS DEFEATED! +$bossScore", 0xFFFFD700.toInt(), 1.4f)
                        onBossKilled?.invoke()
                    } else {
                        audioManager.playChickenHit()
                    }
                }
            }
        }

        // 2. Eggs vs Player
        for (e in eggs) {
            if (!e.isAlive) continue

            val dist = hypot(e.x - player.x, e.y - player.y)
            val playerRadius = if (player.isShieldActive) 58f else 38f

            if (dist < playerRadius + e.eggType.radius) {
                e.isAlive = false
                audioManager.playEggSplat()
                particleManager.spawnEggImpact(e.x, e.y, e.eggType.isExplosive)

                if (player.isShieldActive) {
                    audioManager.playShieldDeflect()
                    particleManager.spawnSparks(e.x, e.y, 0xFF38BDF8.toInt(), 12)
                } else if (!player.isInvulnerable) {
                    player.lives--
                    player.invulnerableTimer = 2.2f
                    player.hitFlashTimer = 0.2f
                    audioManager.playPlayerHit()
                    particleManager.triggerShake(18f, 0.4f)
                    onPlayerHit?.invoke()
                    if (player.lives <= 0) {
                        player.isDestroyed = true
                        particleManager.spawnBombDetonation(player.x, player.y, screenWidth, screenHeight)
                    }
                }
            }
        }

        // 3. PowerUps vs Player
        for (pw in powerUps) {
            if (!pw.isAlive) continue

            val dist = hypot(pw.x - player.x, pw.y - player.y)
            if (dist < 58f) {
                pw.isAlive = false
                player.activatePowerUp(pw.type)
                audioManager.playPowerUp()
                particleManager.spawnSparks(pw.x, pw.y, pw.type.color.toInt(), 18)
                particleManager.addFloatingText(pw.x, pw.y - 15f, pw.type.displayName, pw.type.color.toInt(), 1.15f)
                val bonus = 500 * (if (player.isScore2xActive) 2 else 1)
                onScoreAdded?.invoke(bonus)
            }
        }

        // 4. Chickens reaching bottom or crashing into player
        for (c in chickens) {
            if (!c.isAlive) continue
            // Check bottom screen breach
            if (c.y + c.height / 2f >= player.y - player.height / 2f) {
                // If player is not invulnerable and ship is touched
                val dist = hypot(c.x - player.x, c.y - player.y)
                if (dist < 60f && !player.isInvulnerable) {
                    player.lives--
                    player.invulnerableTimer = 2.2f
                    player.hitFlashTimer = 0.2f
                    audioManager.playPlayerHit()
                    particleManager.triggerShake(20f, 0.45f)
                    onPlayerHit?.invoke()
                    if (player.lives <= 0) {
                        player.isDestroyed = true
                    }
                }
            }
        }
    }

    private fun triggerAreaDamage(cx: Float, cy: Float, radius: Float, chickens: MutableList<Chicken>) {
        for (other in chickens) {
            if (!other.isAlive) continue
            val d = hypot(other.x - cx, other.y - cy)
            if (d < radius) {
                val dead = other.takeDamage(3)
                if (dead) {
                    val sc = other.breed.scoreValue
                    onScoreAdded?.invoke(sc)
                    particleManager.spawnFeatherExplosion(
                        other.x, other.y,
                        other.breed.primaryColor.toInt(),
                        other.breed.secondaryColor.toInt(),
                        12
                    )
                    onChickenKilled?.invoke(other)
                }
            }
        }
    }

    fun triggerScreenClearingBomb(
        chickens: MutableList<Chicken>,
        boss: BossChicken,
        eggs: MutableList<Egg>,
        screenWidth: Float,
        screenHeight: Float
    ) {
        audioManager.playBomb()
        particleManager.spawnBombDetonation(screenWidth / 2f, screenHeight / 2f, screenWidth, screenHeight)

        // Clear all eggs
        for (e in eggs) {
            if (e.isAlive) {
                e.isAlive = false
                particleManager.spawnEggImpact(e.x, e.y, e.eggType.isExplosive)
            }
        }

        // Heavy damage or destroy all normal chickens
        for (c in chickens) {
            if (c.isAlive) {
                val dead = c.takeDamage(10)
                if (dead) {
                    val gain = c.breed.scoreValue
                    onScoreAdded?.invoke(gain)
                    particleManager.spawnFeatherExplosion(
                        c.x, c.y,
                        c.breed.primaryColor.toInt(),
                        c.breed.secondaryColor.toInt(),
                        14
                    )
                    onChickenKilled?.invoke(c)
                }
            }
        }

        // Heavy damage to Boss
        if (boss.isActive && boss.isAlive) {
            val killed = boss.takeDamage(25)
            if (killed) {
                audioManager.playExplosion()
                val bossScore = 15000
                onScoreAdded?.invoke(bossScore)
                onBossKilled?.invoke()
            }
        }
    }
}

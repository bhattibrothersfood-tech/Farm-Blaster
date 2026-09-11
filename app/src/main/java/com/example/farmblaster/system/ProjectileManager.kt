package com.example.farmblaster.system

import com.example.farmblaster.audio.ArcadeAudioManager
import com.example.farmblaster.model.BossChicken
import com.example.farmblaster.model.Chicken
import com.example.farmblaster.model.Egg
import com.example.farmblaster.model.EggType
import com.example.farmblaster.model.Player
import com.example.farmblaster.model.PlayerProjectile
import com.example.farmblaster.model.WeaponType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Handles projectile physics, weapon firing modes, and enemy egg attacks.
 */
class ProjectileManager(
    val playerProjectiles: MutableList<PlayerProjectile>,
    val eggs: MutableList<Egg>,
    private val audioManager: ArcadeAudioManager
) {
    var shootCooldownTimer = 0f

    fun firePlayerWeapon(player: Player) {
        if (player.isDestroyed || shootCooldownTimer > 0f) return

        val weapon = player.currentWeapon
        val px = player.x
        val py = player.y - player.height / 2f + 10f

        when (weapon) {
            WeaponType.SINGLE -> {
                playerProjectiles.add(
                    PlayerProjectile(
                        x = px,
                        y = py,
                        vx = 0f,
                        vy = -1100f,
                        damage = 1,
                        weaponType = WeaponType.SINGLE
                    )
                )
                audioManager.playShoot(isRapid = false, isSpread = false)
            }
            WeaponType.DOUBLE -> {
                playerProjectiles.add(
                    PlayerProjectile(
                        x = px - 26f,
                        y = py,
                        vx = 0f,
                        vy = -1150f,
                        damage = 1,
                        weaponType = WeaponType.DOUBLE
                    )
                )
                playerProjectiles.add(
                    PlayerProjectile(
                        x = px + 26f,
                        y = py,
                        vx = 0f,
                        vy = -1150f,
                        damage = 1,
                        weaponType = WeaponType.DOUBLE
                    )
                )
                audioManager.playShoot(isRapid = false, isSpread = false)
            }
            WeaponType.TRIPLE -> {
                // Center
                playerProjectiles.add(
                    PlayerProjectile(
                        x = px,
                        y = py - 4f,
                        vx = 0f,
                        vy = -1200f,
                        damage = 1,
                        weaponType = WeaponType.TRIPLE
                    )
                )
                // Left angle
                playerProjectiles.add(
                    PlayerProjectile(
                        x = px - 22f,
                        y = py,
                        vx = -180f,
                        vy = -1180f,
                        damage = 1,
                        weaponType = WeaponType.TRIPLE
                    )
                )
                // Right angle
                playerProjectiles.add(
                    PlayerProjectile(
                        x = px + 22f,
                        y = py,
                        vx = 180f,
                        vy = -1180f,
                        damage = 1,
                        weaponType = WeaponType.TRIPLE
                    )
                )
                audioManager.playShoot(isRapid = false, isSpread = false)
            }
            WeaponType.SPREAD -> {
                val angles = floatArrayOf(-260f, -130f, 0f, 130f, 260f)
                for (vx in angles) {
                    playerProjectiles.add(
                        PlayerProjectile(
                            x = px,
                            y = py,
                            vx = vx,
                            vy = -1050f,
                            damage = 1,
                            weaponType = WeaponType.SPREAD
                        )
                    )
                }
                audioManager.playShoot(isRapid = false, isSpread = true)
            }
            WeaponType.RAPID -> {
                playerProjectiles.add(
                    PlayerProjectile(
                        x = px,
                        y = py,
                        vx = 0f,
                        vy = -1350f,
                        damage = 1,
                        weaponType = WeaponType.RAPID
                    )
                )
                audioManager.playShoot(isRapid = true, isSpread = false)
            }
            WeaponType.CHARGED_WAVE -> {
                playerProjectiles.add(
                    PlayerProjectile(
                        x = px,
                        y = py,
                        vx = 0f,
                        vy = -950f,
                        damage = 3,
                        width = 60f,
                        height = 24f,
                        weaponType = WeaponType.CHARGED_WAVE,
                        piercesEnemies = true
                    )
                )
                audioManager.playShoot(isRapid = false, isSpread = true)
            }
        }

        player.muzzleFlashTimer = 0.06f
        shootCooldownTimer = weapon.fireCooldownFrames * 0.016f
    }

    fun updateEggsAndAttacks(
        dt: Float,
        chickens: List<Chicken>,
        boss: BossChicken,
        screenHeight: Float,
        wave: Int
    ) {
        // Cooldown
        if (shootCooldownTimer > 0f) shootCooldownTimer -= dt

        // 1. Normal Chicken Egg Attacks
        val alive = chickens.filter { it.isAlive && it.inFormation }
        if (alive.isNotEmpty()) {
            val baseRate = (0.0035f + wave * 0.0008f).coerceAtMost(0.025f)
            for (c in alive) {
                if (Math.random() < (c.breed.eggDropChance + baseRate)) {
                    dropChickenEgg(c)
                }
            }
        }

        // 2. Boss Egg Attacks
        if (boss.isActive && boss.isAlive) {
            updateBossEggAttacks(dt, boss, wave)
        }

        // 3. Update Player Projectiles Physics
        val pIt = playerProjectiles.iterator()
        while (pIt.hasNext()) {
            val p = pIt.next()
            if (!p.isAlive) {
                pIt.remove()
                continue
            }
            p.x += p.vx * dt
            p.y += p.vy * dt
            if (p.y < -50f || p.x < -80f || p.x > 2000f) {
                pIt.remove()
            }
        }

        // 4. Update Egg Physics & Explosive timers
        val eIt = eggs.iterator()
        while (eIt.hasNext()) {
            val e = eIt.next()
            if (!e.isAlive) {
                eIt.remove()
                continue
            }
            e.x += e.vx * dt
            e.y += e.vy * dt
            e.rotation += e.rotationSpeed * dt * 45f

            if (e.eggType.isExplosive) {
                e.fuseTimer += dt
                // Detonate after traveling distance
                if (e.fuseTimer > 1.8f) {
                    e.isAlive = false
                }
            }

            if (e.y > screenHeight + 60f) {
                eIt.remove()
            }
        }
    }

    private fun dropChickenEgg(c: Chicken) {
        val eggType = when (c.breed.name) {
            "FAST" -> EggType.FAST
            "HEAVY" -> EggType.LARGE
            "EXPLOSIVE" -> EggType.EXPLOSIVE
            "ELITE" -> if (Math.random() < 0.5) EggType.FAST else EggType.EXPLOSIVE
            else -> EggType.NORMAL
        }

        eggs.add(
            Egg(
                x = c.x,
                y = c.y + c.height / 2f,
                vx = (-25f + Math.random().toFloat() * 50f),
                vy = eggType.speed * 42f,
                eggType = eggType,
                rotation = (Math.random() * 360f).toFloat(),
                rotationSpeed = (-4f + Math.random().toFloat() * 8f)
            )
        )
        audioManager.playEggDrop()
    }

    private fun updateBossEggAttacks(dt: Float, boss: BossChicken, wave: Int) {
        boss.attackTimer += dt
        boss.eggBurstTimer += dt

        // Attack pattern based on phase
        val burstInterval = when (boss.phase) {
            1 -> 1.4f
            2 -> 0.95f
            else -> 0.60f // Frenzy
        }

        if (boss.eggBurstTimer >= burstInterval) {
            boss.eggBurstTimer = 0f
            when (boss.phase) {
                1 -> {
                    // Twin heavy eggs
                    eggs.add(Egg(x = boss.x - 40f, y = boss.y + 40f, vy = 380f, eggType = EggType.LARGE))
                    eggs.add(Egg(x = boss.x + 40f, y = boss.y + 40f, vy = 380f, eggType = EggType.LARGE))
                }
                2 -> {
                    // 5-Way Egg Spread
                    for (i in -2..2) {
                        val vx = i * 95f
                        eggs.add(
                            Egg(
                                x = boss.x,
                                y = boss.y + 50f,
                                vx = vx,
                                vy = 420f,
                                eggType = EggType.BOSS_SPREAD
                            )
                        )
                    }
                }
                3 -> {
                    // Phase 3: Triple volley + explosive cluster
                    for (i in -3..3) {
                        val vx = i * 110f
                        val type = if (i == 0) EggType.EXPLOSIVE else EggType.FAST
                        eggs.add(
                            Egg(
                                x = boss.x,
                                y = boss.y + 50f,
                                vx = vx,
                                vy = 460f,
                                eggType = type
                            )
                        )
                    }
                }
            }
            audioManager.playEggDrop()
        }
    }

    fun clear() {
        playerProjectiles.clear()
        eggs.clear()
        shootCooldownTimer = 0f
    }
}

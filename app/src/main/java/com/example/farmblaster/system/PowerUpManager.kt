package com.example.farmblaster.system

import com.example.farmblaster.model.Chicken
import com.example.farmblaster.model.ChickenBreed
import com.example.farmblaster.model.PowerUpItem
import com.example.farmblaster.model.PowerUpType

/**
 * Manages power-up drop chances, lifetimes, and item physics.
 */
class PowerUpManager(private val powerUps: MutableList<PowerUpItem>) {

    fun checkSpawnOnKill(chicken: Chicken) {
        val dropChance = when (chicken.breed) {
            ChickenBreed.GOLDEN -> 1.0f // Golden chicken always drops power-up
            ChickenBreed.ELITE -> 0.45f
            ChickenBreed.ARMORED -> 0.30f
            ChickenBreed.HEAVY -> 0.25f
            else -> 0.08f // Balanced drop rate for standard chickens
        }

        if (Math.random() < dropChance) {
            val type = choosePowerUpType(chicken.breed)
            powerUps.add(
                PowerUpItem(
                    x = chicken.x,
                    y = chicken.y,
                    vy = 180f,
                    type = type,
                    bobTimer = (Math.random() * 5.0).toFloat(),
                    isAlive = true
                )
            )
        }
    }

    private fun choosePowerUpType(breed: ChickenBreed): PowerUpType {
        if (breed == ChickenBreed.GOLDEN) {
            return listOf(PowerUpType.SCORE_2X, PowerUpType.EXTRA_LIFE, PowerUpType.SPREAD).random()
        }
        val roll = Math.random()
        return when {
            roll < 0.22 -> PowerUpType.DOUBLE
            roll < 0.40 -> PowerUpType.TRIPLE
            roll < 0.54 -> PowerUpType.RAPID_FIRE
            roll < 0.68 -> PowerUpType.SPREAD
            roll < 0.80 -> PowerUpType.SHIELD
            roll < 0.90 -> PowerUpType.BOMB
            roll < 0.96 -> PowerUpType.HEALTH
            else -> PowerUpType.EXTRA_LIFE
        }
    }

    fun update(dt: Float, screenHeight: Float) {
        val it = powerUps.iterator()
        while (it.hasNext()) {
            val pw = it.next()
            if (!pw.isAlive) {
                it.remove()
                continue
            }
            pw.y += pw.vy * dt
            pw.bobTimer += dt

            if (pw.y > screenHeight + 50f) {
                it.remove()
            }
        }
    }

    fun clear() {
        powerUps.clear()
    }
}

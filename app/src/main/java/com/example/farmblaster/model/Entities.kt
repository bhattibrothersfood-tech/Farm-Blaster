package com.example.farmblaster.model

data class Player(
    var x: Float = 0f,
    var y: Float = 0f,
    val width: Float = 90f,
    val height: Float = 95f,
    var speed: Float = 14f,
    var lives: Int = 3,
    var bombs: Int = 2,
    var currentWeapon: WeaponType = WeaponType.SINGLE,
    var weaponTimer: Float = 0f,
    var shieldTimer: Float = 0f,
    var scoreMultiplierTimer: Float = 0f,
    var invulnerableTimer: Float = 0f,
    var hitFlashTimer: Float = 0f,
    var muzzleFlashTimer: Float = 0f,
    var thrusterFlamePhase: Float = 0f,
    var isDestroyed: Boolean = false
) {
    val isShieldActive: Boolean get() = shieldTimer > 0f
    val isScore2xActive: Boolean get() = scoreMultiplierTimer > 0f
    val isInvulnerable: Boolean get() = invulnerableTimer > 0f || isShieldActive

    fun reset(screenWidth: Float, screenHeight: Float) {
        x = screenWidth / 2f
        y = screenHeight - 170f
        lives = 3
        bombs = 2
        currentWeapon = WeaponType.SINGLE
        weaponTimer = 0f
        shieldTimer = 0f
        scoreMultiplierTimer = 0f
        invulnerableTimer = 2.0f
        hitFlashTimer = 0f
        muzzleFlashTimer = 0f
        thrusterFlamePhase = 0f
        isDestroyed = false
    }

    fun activatePowerUp(type: PowerUpType) {
        when (type) {
            PowerUpType.DOUBLE -> {
                currentWeapon = WeaponType.DOUBLE
                weaponTimer = type.durationSeconds
            }
            PowerUpType.TRIPLE -> {
                currentWeapon = WeaponType.TRIPLE
                weaponTimer = type.durationSeconds
            }
            PowerUpType.SPREAD -> {
                currentWeapon = WeaponType.SPREAD
                weaponTimer = type.durationSeconds
            }
            PowerUpType.RAPID_FIRE -> {
                currentWeapon = WeaponType.RAPID
                weaponTimer = type.durationSeconds
            }
            PowerUpType.SHIELD -> {
                shieldTimer = type.durationSeconds
            }
            PowerUpType.BOMB -> {
                bombs = (bombs + 1).coerceAtMost(5)
            }
            PowerUpType.HEALTH -> {
                // Restore life if under 3
                if (lives < 3) lives++
            }
            PowerUpType.EXTRA_LIFE -> {
                lives = (lives + 1).coerceAtMost(5)
            }
            PowerUpType.SCORE_2X -> {
                scoreMultiplierTimer = type.durationSeconds
            }
        }
    }

    fun update(dt: Float) {
        thrusterFlamePhase += dt * 14f
        if (weaponTimer > 0f) {
            weaponTimer -= dt
            if (weaponTimer <= 0f) {
                currentWeapon = WeaponType.SINGLE
            }
        }
        if (shieldTimer > 0f) shieldTimer -= dt
        if (scoreMultiplierTimer > 0f) scoreMultiplierTimer -= dt
        if (invulnerableTimer > 0f) invulnerableTimer -= dt
        if (hitFlashTimer > 0f) hitFlashTimer -= dt
        if (muzzleFlashTimer > 0f) muzzleFlashTimer -= dt
    }
}

data class Chicken(
    var id: Int = 0,
    var x: Float = 0f,
    var y: Float = 0f,
    var targetX: Float = 0f,
    var targetY: Float = 0f,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var breed: ChickenBreed = ChickenBreed.WHITE,
    var hp: Int = 1,
    var maxHp: Int = 1,
    var width: Float = 72f,
    var height: Float = 66f,
    var isAlive: Boolean = true,
    var inFormation: Boolean = false,
    var entranceProgress: Float = 0f,
    var entrancePathIndex: Int = 0,
    var startX: Float = 0f,
    var startY: Float = 0f,
    var flapTimer: Float = 0f,
    var flapFrame: Int = 0,
    var hitFlashTimer: Float = 0f,
    var oscillationOffset: Float = 0f,
    var diveCooldown: Float = 0f,
    var isDiving: Boolean = false
) {
    fun takeDamage(dmg: Int): Boolean {
        hp -= dmg
        hitFlashTimer = 0.08f
        if (hp <= 0) {
            isAlive = false
            return true
        }
        return false
    }
}

data class BossChicken(
    var x: Float = 0f,
    var y: Float = 0f,
    var targetX: Float = 0f,
    var targetY: Float = 0f,
    var hp: Int = 100,
    var maxHp: Int = 100,
    var width: Float = 220f,
    var height: Float = 210f,
    var isAlive: Boolean = false,
    var isActive: Boolean = false,
    var phase: Int = 1,
    var attackTimer: Float = 0f,
    var eggBurstTimer: Float = 0f,
    var flapTimer: Float = 0f,
    var flapFrame: Int = 0,
    var hitFlashTimer: Float = 0f,
    var moveTimer: Float = 0f,
    var sweepDirection: Float = 1f,
    var specialAttackActive: Boolean = false,
    var entranceY: Float = -250f
) {
    fun takeDamage(dmg: Int): Boolean {
        hp -= dmg
        hitFlashTimer = 0.08f
        // Phase transition checks
        val hpPct = hp.toFloat() / maxHp
        phase = when {
            hpPct > 0.65f -> 1
            hpPct > 0.30f -> 2
            else -> 3
        }
        if (hp <= 0) {
            isAlive = false
            isActive = false
            return true
        }
        return false
    }
}

data class Egg(
    var x: Float = 0f,
    var y: Float = 0f,
    var vx: Float = 0f,
    var vy: Float = 10f,
    var eggType: EggType = EggType.NORMAL,
    var rotation: Float = 0f,
    var rotationSpeed: Float = 5f,
    var fuseTimer: Float = 0f,
    var isAlive: Boolean = true
)

data class PlayerProjectile(
    var x: Float = 0f,
    var y: Float = 0f,
    var vx: Float = 0f,
    var vy: Float = -24f,
    var damage: Int = 1,
    var width: Float = 12f,
    var height: Float = 28f,
    var weaponType: WeaponType = WeaponType.SINGLE,
    var piercesEnemies: Boolean = false,
    var isAlive: Boolean = true
)

data class PowerUpItem(
    var x: Float = 0f,
    var y: Float = 0f,
    var vy: Float = 4.2f,
    var type: PowerUpType = PowerUpType.DOUBLE,
    var bobTimer: Float = 0f,
    var isAlive: Boolean = true
)

enum class ParticleShape {
    FEATHER, SPARK, SMOKE, EGG_SHARD, YOLK, RING, FLASH
}

data class Particle(
    var x: Float = 0f,
    var y: Float = 0f,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var color: Int = 0,
    var size: Float = 8f,
    var alpha: Float = 1f,
    var decay: Float = 1.5f,
    var rotation: Float = 0f,
    var rotationSpeed: Float = 0f,
    var shape: ParticleShape = ParticleShape.SPARK,
    var isAlive: Boolean = false
)

data class FloatingText(
    var x: Float = 0f,
    var y: Float = 0f,
    var text: String = "",
    var color: Int = 0xFFFFD700.toInt(),
    var alpha: Float = 1f,
    var vy: Float = -45f,
    var scale: Float = 1f,
    var isAlive: Boolean = false
)

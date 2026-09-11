package com.example.farmblaster.model

enum class ChickenBreed(
    val displayName: String,
    val baseHp: Int,
    val scoreValue: Int,
    val eggDropChance: Float,
    val primaryColor: Long,
    val secondaryColor: Long,
    val speedMultiplier: Float = 1.0f
) {
    WHITE("Leghorn Scout", 1, 100, 0.0035f, 0xFFF8F6F0, 0xFFE2DDD0, 1.0f),
    BROWN("Rhode Island Striker", 2, 200, 0.0050f, 0xFF8B3A1C, 0xFFB85D32, 1.15f),
    BLACK("Australorp Raider", 3, 350, 0.0065f, 0xFF1E1E26, 0xFF353C48, 1.0f),
    GOLDEN("Golden Orpington", 1, 1000, 0.0020f, 0xFFF59E0B, 0xFFFDE68A, 1.4f),
    ARMORED("Titanium Cyber-Hen", 6, 600, 0.0080f, 0xFF64748B, 0xFF94A3B8, 0.8f),
    FAST("Falcon Hen", 2, 300, 0.0090f, 0xFF0284C7, 0xFF38BDF8, 1.55f),
    HEAVY("Mother Berta", 8, 800, 0.0070f, 0xFF78350F, 0xFF92400E, 0.65f),
    EXPLOSIVE("Combustion Pullet", 3, 500, 0.0060f, 0xFFDC2626, 0xFFEA580C, 1.1f),
    ELITE("Elite Squadron Hen", 5, 750, 0.0110f, 0xFF7C3AED, 0xFFA855F7, 1.25f),
    BOSS("Mother Clucker", 100, 10000, 0.0400f, 0xFF991B1B, 0xFFF59E0B, 0.9f)
}

enum class EggType(
    val speed: Float,
    val damage: Int,
    val isExplosive: Boolean = false,
    val radius: Float = 10f
) {
    NORMAL(11f, 1, false, 10f),
    FAST(16f, 1, false, 9f),
    LARGE(8f, 2, false, 18f),
    EXPLOSIVE(10f, 1, true, 13f),
    BOSS_SPREAD(12f, 1, false, 14f)
}

enum class WeaponType(
    val displayName: String,
    val fireCooldownFrames: Int,
    val projectileSpeed: Float,
    val damage: Int
) {
    SINGLE("Plasma Bolt", 11, 24f, 1),
    DOUBLE("Twin Blasters", 10, 25f, 1),
    TRIPLE("Tri-Cannon", 10, 26f, 1),
    SPREAD("5-Way Fan Blast", 12, 23f, 1),
    RAPID("Vulcan Gatling", 6, 28f, 1),
    CHARGED_WAVE("Proton Wave", 14, 20f, 3)
}

enum class PowerUpType(
    val displayName: String,
    val durationSeconds: Float = 12f,
    val color: Long
) {
    DOUBLE("DOUBLE SHOT", 14f, 0xFF38BDF8),
    TRIPLE("TRIPLE SHOT", 12f, 0xFF4ADE80),
    SPREAD("SPREAD SHOT", 10f, 0xFFF472B6),
    RAPID_FIRE("RAPID FIRE", 10f, 0xFFFBBF24),
    SHIELD("ENERGY SHIELD", 15f, 0xFF60A5FA),
    BOMB("+1 BOMB", 0f, 0xFFEF4444),
    HEALTH("REPAIR HULL", 0f, 0xFF10B981),
    EXTRA_LIFE("+1 LIFE", 0f, 0xFFA855F7),
    SCORE_2X("2X SCORE", 15f, 0xFFF59E0B)
}

enum class FormationType {
    HORIZONTAL_LINE,
    V_SHAPE,
    DIAMOND,
    CIRCLE,
    GRID,
    ZIG_ZAG,
    WAVE_SWEEP,
    SPIRAL_SWARM
}

enum class GameState {
    SPLASH,
    MAIN_MENU,
    PLAYING,
    PAUSED,
    LEVEL_COMPLETE,
    GAME_OVER,
    HOW_TO_PLAY,
    SETTINGS,
    HIGH_SCORES
}

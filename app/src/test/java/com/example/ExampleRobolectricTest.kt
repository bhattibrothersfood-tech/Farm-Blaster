package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.farmblaster.audio.ArcadeAudioManager
import com.example.farmblaster.data.GamePreferences
import com.example.farmblaster.engine.GameEngine
import com.example.farmblaster.model.BossChicken
import com.example.farmblaster.model.Chicken
import com.example.farmblaster.model.ChickenBreed
import com.example.farmblaster.model.Player
import com.example.farmblaster.model.PowerUpType
import com.example.farmblaster.model.WeaponType
import com.example.farmblaster.system.WaveManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Farm Blaster", appName)
    }

    @Test
    fun `chicken takes damage and dies`() {
        val chicken = Chicken(
            id = 1,
            x = 100f,
            y = 100f,
            breed = ChickenBreed.WHITE,
            hp = 2,
            maxHp = 2
        )

        // 1 damage: should survive
        val killed1 = chicken.takeDamage(1)
        assertFalse(killed1)
        assertEquals(1, chicken.hp)
        assertTrue(chicken.isAlive)

        // 1 damage: should die
        val killed2 = chicken.takeDamage(1)
        assertTrue(killed2)
        assertEquals(0, chicken.hp)
        assertFalse(chicken.isAlive)
    }

    @Test
    fun `player activates shield power up`() {
        val player = Player()
        player.reset(1080f, 1920f)

        assertFalse(player.isShieldActive)

        player.activatePowerUp(PowerUpType.SHIELD)
        assertTrue(player.isShieldActive)
        assertTrue(player.isInvulnerable)
    }

    @Test
    fun `player activates triple weapon`() {
        val player = Player()
        player.reset(1080f, 1920f)

        assertEquals(WeaponType.SINGLE, player.currentWeapon)

        player.activatePowerUp(PowerUpType.TRIPLE)
        assertEquals(WeaponType.TRIPLE, player.currentWeapon)
        assertTrue(player.weaponTimer > 0f)
    }

    @Test
    fun `preferences stores and updates high score`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = GamePreferences(context)
        prefs.resetHighScores()

        assertEquals(0, prefs.highScore)
        val updated = prefs.updateHighScoreIfNeeded(1500)
        assertTrue(updated)
        assertEquals(1500, prefs.highScore)

        val lowerScoreUpdated = prefs.updateHighScoreIfNeeded(800)
        assertFalse(lowerScoreUpdated)
        assertEquals(1500, prefs.highScore)
    }

    @Test
    fun `continuous shooting is enabled from start`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = GamePreferences(context)
        val audio = ArcadeAudioManager(context, prefs)
        val engine = GameEngine(context, prefs, audio)
        engine.initDimensions(1080f, 1920f)

        assertTrue(engine.isFiring)
    }

    @Test
    fun `diving chicken loops back to top when reaching bottom of screen`() {
        val chickens = mutableListOf<Chicken>()
        val boss = BossChicken()
        val waveManager = WaveManager(chickens, boss)
        val screenWidth = 1080f
        val screenHeight = 1920f

        val chicken = Chicken(
            id = 1,
            x = 500f,
            y = screenHeight + 55f, // Below the screen!
            targetX = 500f,
            targetY = 300f,
            breed = ChickenBreed.WHITE,
            hp = 1,
            maxHp = 1,
            isAlive = true,
            inFormation = true,
            isDiving = true
        )
        chickens.add(chicken)

        waveManager.update(0.016f, screenWidth, screenHeight)

        // Chicken should have looped back above the screen and stopped diving
        assertTrue(chicken.y < 0f)
        assertFalse(chicken.isDiving)
        assertTrue(chicken.isAlive)
    }
}

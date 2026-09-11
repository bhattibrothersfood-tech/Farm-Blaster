package com.example.farmblaster.render

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import com.example.farmblaster.model.Player
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Renders the futuristic farm-defense spacecraft ("Harvester-9 Alpha")
 * with animated plasma thrusters, muzzle flashes, and shimmering energy shield dome.
 */
class PlayerRenderer {

    private val shipPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shieldPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shieldBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3.5f
    }
    private val flamePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val muzzlePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val hullPath = Path()
    private val wingLeftPath = Path()
    private val wingRightPath = Path()
    private val cockpitPath = Path()
    private val flamePath = Path()

    fun render(canvas: Canvas, player: Player) {
        if (player.isDestroyed) return

        // Invulnerability blinking
        if (player.invulnerableTimer > 0f && (player.invulnerableTimer * 12).toInt() % 2 == 0) {
            return
        }

        val cx = player.x
        val cy = player.y

        canvas.save()

        // Hit flash filter
        if (player.hitFlashTimer > 0f) {
            shipPaint.colorFilter = PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        } else {
            shipPaint.colorFilter = null
        }

        // 1. Dual Animated Plasma Thruster Flames
        val flameFlicker = (sin(player.thrusterFlamePhase) * 6f).coerceIn(-4f, 8f)
        val flameLength = 28f + flameFlicker
        drawPlasmaFlame(canvas, cx - 22f, cy + 32f, flameLength)
        drawPlasmaFlame(canvas, cx + 22f, cy + 32f, flameLength)

        // 2. Agricultural Defense Wings (Swept forward harvester blades with solar conduits)
        // Left Wing
        wingLeftPath.reset()
        wingLeftPath.moveTo(cx - 10f, cy + 10f)
        wingLeftPath.lineTo(cx - 44f, cy + 24f)
        wingLeftPath.lineTo(cx - 42f, cy - 12f)
        wingLeftPath.lineTo(cx - 28f, cy - 8f)
        wingLeftPath.lineTo(cx - 14f, cy - 2f)
        wingLeftPath.close()

        shipPaint.style = Paint.Style.FILL
        shipPaint.shader = LinearGradient(
            cx - 44f, cy, cx - 10f, cy,
            Color.rgb(220, 170, 40), Color.rgb(70, 85, 105), Shader.TileMode.CLAMP
        )
        canvas.drawPath(wingLeftPath, shipPaint)

        // Right Wing
        wingRightPath.reset()
        wingRightPath.moveTo(cx + 10f, cy + 10f)
        wingRightPath.lineTo(cx + 44f, cy + 24f)
        wingRightPath.lineTo(cx + 42f, cy - 12f)
        wingRightPath.lineTo(cx + 28f, cy - 8f)
        wingRightPath.lineTo(cx + 14f, cy - 2f)
        wingRightPath.close()

        shipPaint.shader = LinearGradient(
            cx + 10f, cy, cx + 44f, cy,
            Color.rgb(70, 85, 105), Color.rgb(220, 170, 40), Shader.TileMode.CLAMP
        )
        canvas.drawPath(wingRightPath, shipPaint)

        // Wingtip Blaster Pods
        shipPaint.shader = null
        shipPaint.color = Color.rgb(40, 50, 65)
        canvas.drawRoundRect(RectF(cx - 45f, cy - 22f, cx - 39f, cy + 8f), 3f, 3f, shipPaint)
        canvas.drawRoundRect(RectF(cx + 39f, cy - 22f, cx + 45f, cy + 8f), 3f, 3f, shipPaint)

        // 3. Central Hull (Sleek aerodynamic titanium chassis)
        hullPath.reset()
        hullPath.moveTo(cx, cy - 42f) // Nose cone
        hullPath.cubicTo(cx + 14f, cy - 20f, cx + 24f, cy + 10f, cx + 22f, cy + 34f)
        hullPath.lineTo(cx + 10f, cy + 36f)
        hullPath.lineTo(cx, cy + 28f)
        hullPath.lineTo(cx - 10f, cy + 36f)
        hullPath.cubicTo(cx - 24f, cy + 10f, cx - 14f, cy - 20f, cx, cy - 42f)
        hullPath.close()

        shipPaint.shader = LinearGradient(
            cx - 24f, cy, cx + 24f, cy,
            Color.rgb(245, 248, 255), Color.rgb(180, 195, 215), Shader.TileMode.CLAMP
        )
        canvas.drawPath(hullPath, shipPaint)

        // Harvest Golden Racing Accent Stripes
        val stripePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(245, 158, 11)
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawLine(cx - 8f, cy - 14f, cx - 12f, cy + 26f, stripePaint)
        canvas.drawLine(cx + 8f, cy - 14f, cx + 12f, cy + 26f, stripePaint)

        // 4. Cockpit Canopy (Glowing cyan aerodynamic bubble)
        cockpitPath.reset()
        cockpitPath.moveTo(cx, cy - 28f)
        cockpitPath.cubicTo(cx + 9f, cy - 16f, cx + 8f, cy + 4f, cx, cy + 10f)
        cockpitPath.cubicTo(cx - 8f, cy + 4f, cx - 9f, cy - 16f, cx, cy - 28f)
        cockpitPath.close()

        shipPaint.shader = RadialGradient(
            cx, cy - 10f, 18f,
            intArrayOf(Color.rgb(220, 255, 255), Color.rgb(0, 180, 240), Color.rgb(0, 70, 140)),
            floatArrayOf(0f, 0.6f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawPath(cockpitPath, shipPaint)

        // 5. Muzzle Flash Bursts
        if (player.muzzleFlashTimer > 0f) {
            muzzlePaint.style = Paint.Style.FILL
            muzzlePaint.shader = RadialGradient(
                cx - 42f, cy - 24f, 14f,
                Color.rgb(255, 255, 200), Color.TRANSPARENT, Shader.TileMode.CLAMP
            )
            canvas.drawCircle(cx - 42f, cy - 24f, 14f, muzzlePaint)

            muzzlePaint.shader = RadialGradient(
                cx + 42f, cy - 24f, 14f,
                Color.rgb(255, 255, 200), Color.TRANSPARENT, Shader.TileMode.CLAMP
            )
            canvas.drawCircle(cx + 42f, cy - 24f, 14f, muzzlePaint)
        }

        // 6. Shimmering Energy Shield Dome
        if (player.isShieldActive) {
            drawShieldDome(canvas, cx, cy, 64f, player.thrusterFlamePhase)
        }

        canvas.restore()
    }

    private fun drawPlasmaFlame(canvas: Canvas, fx: Float, fy: Float, length: Float) {
        flamePath.reset()
        flamePath.moveTo(fx - 7f, fy)
        flamePath.lineTo(fx, fy + length)
        flamePath.lineTo(fx + 7f, fy)
        flamePath.close()

        // Outer blue plasma
        flamePaint.style = Paint.Style.FILL
        flamePaint.shader = LinearGradient(
            fx, fy, fx, fy + length,
            Color.rgb(0, 230, 255), Color.TRANSPARENT, Shader.TileMode.CLAMP
        )
        canvas.drawPath(flamePath, flamePaint)

        // Inner white-hot core
        val innerPath = Path()
        innerPath.moveTo(fx - 3f, fy)
        innerPath.lineTo(fx, fy + length * 0.6f)
        innerPath.lineTo(fx + 3f, fy)
        innerPath.close()
        flamePaint.shader = LinearGradient(
            fx, fy, fx, fy + length * 0.6f,
            Color.WHITE, Color.rgb(100, 240, 255), Shader.TileMode.CLAMP
        )
        canvas.drawPath(innerPath, flamePaint)
    }

    private fun drawShieldDome(canvas: Canvas, cx: Float, cy: Float, radius: Float, phase: Float) {
        // Translucent blue forcefield
        shieldPaint.style = Paint.Style.FILL
        shieldPaint.shader = RadialGradient(
            cx, cy, radius,
            intArrayOf(Color.argb(35, 56, 189, 248), Color.argb(90, 14, 165, 233), Color.argb(160, 2, 132, 199)),
            floatArrayOf(0f, 0.7f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(cx, cy, radius, shieldPaint)

        // Shimmering outer border with rotational energy nodes
        shieldBorderPaint.color = Color.rgb(125, 211, 252)
        canvas.drawCircle(cx, cy, radius, shieldBorderPaint)

        // Hexagonal energy node ripples
        for (i in 0 until 6) {
            val angle = phase * 1.5f + (i * PI.toFloat() / 3f)
            val nx = cx + cos(angle) * (radius - 2f)
            val ny = cy + sin(angle) * (radius - 2f)
            shieldPaint.shader = null
            shieldPaint.color = Color.rgb(224, 242, 254)
            canvas.drawCircle(nx, ny, 4f, shieldPaint)
        }
    }
}

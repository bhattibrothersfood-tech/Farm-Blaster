package com.example.farmblaster.render

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import com.example.farmblaster.model.Egg
import com.example.farmblaster.model.EggType
import com.example.farmblaster.model.PlayerProjectile
import com.example.farmblaster.model.PowerUpItem
import com.example.farmblaster.model.PowerUpType
import com.example.farmblaster.model.WeaponType
import kotlin.math.sin

/**
 * Renders realistic falling eggs with 3D spherical lighting,
 * high-energy player laser projectiles, and floating power-up capsules.
 */
class EggRenderer {

    private val eggPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val eggHighlightPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val projPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val powerUpBgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val powerUpBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }
    private val powerUpTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
        textSize = 17f
    }

    private val eggPath = Path()
    private val eggRect = RectF()

    fun renderEgg(canvas: Canvas, egg: Egg) {
        if (!egg.isAlive) return

        canvas.save()
        canvas.translate(egg.x, egg.y)
        canvas.rotate(egg.rotation)

        val r = egg.eggType.radius
        val w = r * 1.55f
        val h = r * 2.1f

        // Realistic asymmetrical egg shape (narrower top, wider bottom)
        eggPath.reset()
        eggPath.moveTo(0f, -h / 2f)
        eggPath.cubicTo(w * 0.45f, -h / 2f, w / 2f, -h * 0.1f, w / 2f, h * 0.2f)
        eggPath.cubicTo(w / 2f, h / 2f, -w / 2f, h / 2f, -w / 2f, h * 0.2f)
        eggPath.cubicTo(-w / 2f, -h * 0.1f, -w * 0.45f, -h / 2f, 0f, -h / 2f)
        eggPath.close()

        when (egg.eggType) {
            EggType.NORMAL -> {
                eggPaint.shader = RadialGradient(
                    -w * 0.2f, -h * 0.2f, h * 0.7f,
                    intArrayOf(Color.rgb(255, 255, 250), Color.rgb(240, 235, 220), Color.rgb(205, 195, 175)),
                    floatArrayOf(0f, 0.6f, 1f),
                    Shader.TileMode.CLAMP
                )
            }
            EggType.FAST -> {
                eggPaint.shader = RadialGradient(
                    -w * 0.2f, -h * 0.2f, h * 0.7f,
                    intArrayOf(Color.rgb(255, 245, 180), Color.rgb(245, 215, 80), Color.rgb(215, 170, 20)),
                    floatArrayOf(0f, 0.6f, 1f),
                    Shader.TileMode.CLAMP
                )
            }
            EggType.LARGE -> {
                // Heavy brown speckled egg
                eggPaint.shader = RadialGradient(
                    -w * 0.2f, -h * 0.2f, h * 0.7f,
                    intArrayOf(Color.rgb(230, 190, 150), Color.rgb(180, 130, 90), Color.rgb(120, 75, 40)),
                    floatArrayOf(0f, 0.65f, 1f),
                    Shader.TileMode.CLAMP
                )
            }
            EggType.EXPLOSIVE -> {
                // Volatile glowing core
                eggPaint.shader = RadialGradient(
                    0f, 0f, h * 0.6f,
                    intArrayOf(Color.rgb(255, 240, 120), Color.rgb(255, 70, 20), Color.rgb(180, 20, 10)),
                    floatArrayOf(0f, 0.5f, 1f),
                    Shader.TileMode.CLAMP
                )
            }
            EggType.BOSS_SPREAD -> {
                eggPaint.shader = RadialGradient(
                    -w * 0.2f, -h * 0.2f, h * 0.7f,
                    intArrayOf(Color.rgb(255, 250, 200), Color.rgb(234, 88, 12), Color.rgb(154, 52, 18)),
                    floatArrayOf(0f, 0.5f, 1f),
                    Shader.TileMode.CLAMP
                )
            }
        }

        canvas.drawPath(eggPath, eggPaint)

        // Specular 3D light reflection glint
        eggHighlightPaint.color = Color.argb(190, 255, 255, 255)
        canvas.drawCircle(-w * 0.22f, -h * 0.22f, r * 0.28f, eggHighlightPaint)

        canvas.restore()
    }

    fun renderProjectile(canvas: Canvas, proj: PlayerProjectile) {
        if (!proj.isAlive) return
        val cx = proj.x
        val cy = proj.y
        val pw = proj.width
        val ph = proj.height

        when (proj.weaponType) {
            WeaponType.SINGLE, WeaponType.DOUBLE -> {
                // Glowing cyan laser bolt
                projPaint.shader = LinearGradient(
                    cx, cy - ph / 2f, cx, cy + ph / 2f,
                    Color.WHITE, Color.rgb(0, 210, 255), Shader.TileMode.CLAMP
                )
                eggRect.set(cx - pw / 2f, cy - ph / 2f, cx + pw / 2f, cy + ph / 2f)
                canvas.drawRoundRect(eggRect, pw / 2f, pw / 2f, projPaint)
            }
            WeaponType.TRIPLE -> {
                // Vibrant Emerald Tri-blast
                projPaint.shader = LinearGradient(
                    cx, cy - ph / 2f, cx, cy + ph / 2f,
                    Color.WHITE, Color.rgb(74, 222, 128), Shader.TileMode.CLAMP
                )
                eggRect.set(cx - pw / 2f, cy - ph / 2f, cx + pw / 2f, cy + ph / 2f)
                canvas.drawRoundRect(eggRect, pw / 2f, pw / 2f, projPaint)
            }
            WeaponType.SPREAD -> {
                // Neon Pink/Purple Plasma bolt
                projPaint.shader = LinearGradient(
                    cx, cy - ph / 2f, cx, cy + ph / 2f,
                    Color.WHITE, Color.rgb(244, 114, 182), Shader.TileMode.CLAMP
                )
                eggRect.set(cx - pw / 2f, cy - ph / 2f, cx + pw / 2f, cy + ph / 2f)
                canvas.drawRoundRect(eggRect, pw / 2f, pw / 2f, projPaint)
            }
            WeaponType.RAPID -> {
                // Golden High-Speed Vulcan slug
                projPaint.shader = LinearGradient(
                    cx, cy - ph / 2f, cx, cy + ph / 2f,
                    Color.WHITE, Color.rgb(251, 191, 36), Shader.TileMode.CLAMP
                )
                eggRect.set(cx - pw / 2f, cy - ph / 2f, cx + pw / 2f, cy + ph / 2f)
                canvas.drawRoundRect(eggRect, pw / 2f, pw / 2f, projPaint)
            }
            WeaponType.CHARGED_WAVE -> {
                // Wide Piercing Wave Arc
                projPaint.shader = RadialGradient(
                    cx, cy, 32f,
                    Color.WHITE, Color.rgb(245, 158, 11), Shader.TileMode.CLAMP
                )
                eggRect.set(cx - 38f, cy - 14f, cx + 38f, cy + 14f)
                canvas.drawRoundRect(eggRect, 14f, 14f, projPaint)
            }
        }
    }

    fun renderPowerUp(canvas: Canvas, item: PowerUpItem) {
        if (!item.isAlive) return
        val cx = item.x
        val cy = item.y + sin(item.bobTimer * 6f) * 6f
        val radius = 26f
        val itemColor = item.type.color.toInt()

        // Outer pulsating glow halo
        val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = itemColor
            alpha = 80
        }
        canvas.drawCircle(cx, cy, radius + 8f, glowPaint)

        // Pill Capsule Background
        powerUpBgPaint.style = Paint.Style.FILL
        powerUpBgPaint.shader = RadialGradient(
            cx - 6f, cy - 6f, radius,
            intArrayOf(Color.WHITE, itemColor, Color.rgb(15, 23, 42)),
            floatArrayOf(0f, 0.7f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(cx, cy, radius, powerUpBgPaint)

        // Sharp Neon Border
        powerUpBorderPaint.color = Color.WHITE
        canvas.drawCircle(cx, cy, radius, powerUpBorderPaint)

        // Type Monogram
        val label = when (item.type) {
            PowerUpType.DOUBLE -> "2X"
            PowerUpType.TRIPLE -> "3X"
            PowerUpType.SPREAD -> "SP"
            PowerUpType.RAPID_FIRE -> "RF"
            PowerUpType.SHIELD -> "SH"
            PowerUpType.BOMB -> "💣"
            PowerUpType.HEALTH -> "HP"
            PowerUpType.EXTRA_LIFE -> "+1"
            PowerUpType.SCORE_2X -> "★"
        }
        powerUpTextPaint.color = Color.WHITE
        canvas.drawText(label, cx, cy + 6f, powerUpTextPaint)
    }
}

package com.example.farmblaster.render

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import kotlin.math.cos
import kotlin.math.sin

/**
 * Renders an atmospheric scrolling futuristic farm at night.
 * Multi-layer parallax includes stars, luminous moon, nebulae, silhouettes of barns, silos, and rotating windmill.
 */
class BackgroundRenderer {

    private data class Star(var x: Float, var y: Float, val size: Float, val speed: Float, val alpha: Float)
    private data class Cloud(var x: Float, var y: Float, val width: Float, val height: Float, val speed: Float)

    private val stars = mutableListOf<Star>()
    private val clouds = mutableListOf<Cloud>()

    private val skyPaint = Paint()
    private val starPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val moonPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val farmPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val fieldPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val windowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bladePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var initialized = false
    private var windmillAngle = 0f
    private var scrollOffset = 0f

    private fun initElements(width: Float, height: Float) {
        stars.clear()
        // Distant slow stars
        for (i in 0 until 90) {
            stars.add(
                Star(
                    x = (Math.random() * width).toFloat(),
                    y = (Math.random() * height).toFloat(),
                    size = (1.2f + Math.random().toFloat() * 1.8f),
                    speed = (12f + Math.random().toFloat() * 18f),
                    alpha = (0.35f + Math.random().toFloat() * 0.65f)
                )
            )
        }
        // Near bright warp stars
        for (i in 0 until 35) {
            stars.add(
                Star(
                    x = (Math.random() * width).toFloat(),
                    y = (Math.random() * height).toFloat(),
                    size = (2.2f + Math.random().toFloat() * 2.2f),
                    speed = (35f + Math.random().toFloat() * 35f),
                    alpha = (0.75f + Math.random().toFloat() * 0.25f)
                )
            )
        }

        clouds.clear()
        for (i in 0 until 5) {
            clouds.add(
                Cloud(
                    x = (Math.random() * width).toFloat(),
                    y = (Math.random() * height * 0.45f).toFloat(),
                    width = (140f + Math.random().toFloat() * 180f),
                    height = (45f + Math.random().toFloat() * 55f),
                    speed = (8f + Math.random().toFloat() * 12f)
                )
            )
        }

        initialized = true
    }

    fun update(dt: Float, width: Float, height: Float) {
        if (!initialized || width <= 0f || height <= 0f) {
            initElements(width.coerceAtLeast(1080f), height.coerceAtLeast(1920f))
        }

        windmillAngle += dt * 75f
        scrollOffset = (scrollOffset + dt * 18f) % height

        // Move stars
        for (s in stars) {
            s.y += s.speed * dt
            if (s.y > height) {
                s.y = 0f
                s.x = (Math.random() * width).toFloat()
            }
        }

        // Move clouds
        for (c in clouds) {
            c.x += c.speed * dt
            if (c.x - c.width > width) {
                c.x = -c.width
                c.y = (Math.random() * height * 0.45f).toFloat()
            }
        }
    }

    fun render(canvas: Canvas, width: Float, height: Float) {
        if (!initialized) initElements(width, height)

        // 1. Deep Space Night Sky Gradient
        skyPaint.shader = LinearGradient(
            0f, 0f, 0f, height,
            Color.rgb(6, 8, 20), Color.rgb(20, 14, 38), Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, width, height, skyPaint)

        // 2. Glowing Lunar Moon (Atmospheric cosmic moon)
        val moonX = width * 0.82f
        val moonY = height * 0.14f
        val moonRadius = 46f

        // Moon halo
        moonPaint.style = Paint.Style.FILL
        moonPaint.shader = RadialGradient(
            moonX, moonY, moonRadius * 2.2f,
            Color.argb(55, 250, 240, 200), Color.TRANSPARENT, Shader.TileMode.CLAMP
        )
        canvas.drawCircle(moonX, moonY, moonRadius * 2.2f, moonPaint)

        // Moon body
        moonPaint.shader = RadialGradient(
            moonX - 12f, moonY - 12f, moonRadius,
            intArrayOf(Color.rgb(255, 255, 240), Color.rgb(235, 230, 205), Color.rgb(190, 185, 160)),
            floatArrayOf(0f, 0.6f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(moonX, moonY, moonRadius, moonPaint)

        // Moon craters
        moonPaint.shader = null
        moonPaint.color = Color.argb(40, 140, 135, 115)
        canvas.drawCircle(moonX - 14f, moonY + 8f, 9f, moonPaint)
        canvas.drawCircle(moonX + 16f, moonY - 10f, 6f, moonPaint)
        canvas.drawCircle(moonX + 8f, moonY + 14f, 12f, moonPaint)

        // 3. Stars
        for (s in stars) {
            starPaint.color = Color.WHITE
            starPaint.alpha = (s.alpha * 255).toInt()
            canvas.drawCircle(s.x, s.y, s.size, starPaint)
        }

        // 4. Soft Night Clouds
        val cloudPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(35, 160, 175, 220)
            style = Paint.Style.FILL
        }
        for (c in clouds) {
            val rect = RectF(c.x, c.y, c.x + c.width, c.y + c.height)
            canvas.drawRoundRect(rect, c.height / 2f, c.height / 2f, cloudPaint)
        }

        // 5. Far Mountain Silhouettes
        val mtnPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(12, 16, 32)
            style = Paint.Style.FILL
        }
        val mtnPath = Path()
        val mtnBaseY = height * 0.72f
        mtnPath.moveTo(0f, height)
        mtnPath.lineTo(0f, mtnBaseY)
        mtnPath.lineTo(width * 0.22f, mtnBaseY - 90f)
        mtnPath.lineTo(width * 0.48f, mtnBaseY - 40f)
        mtnPath.lineTo(width * 0.74f, mtnBaseY - 110f)
        mtnPath.lineTo(width, mtnBaseY - 30f)
        mtnPath.lineTo(width, height)
        mtnPath.close()
        canvas.drawPath(mtnPath, mtnPaint)

        // 6. Rolling Farmland Fields
        fieldPaint.shader = LinearGradient(
            0f, height * 0.75f, 0f, height,
            Color.rgb(18, 26, 44), Color.rgb(8, 12, 22), Shader.TileMode.CLAMP
        )
        val fieldPath = Path()
        fieldPath.moveTo(0f, height)
        fieldPath.lineTo(0f, height * 0.80f)
        fieldPath.cubicTo(width * 0.35f, height * 0.76f, width * 0.65f, height * 0.84f, width, height * 0.79f)
        fieldPath.lineTo(width, height)
        fieldPath.close()
        canvas.drawPath(fieldPath, fieldPaint)

        // Glowing crop rows (holographic agricultural lines)
        val cropLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(45, 74, 222, 128)
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }
        for (i in 0 until 7) {
            val startY = height * 0.82f + i * 26f
            canvas.drawLine(0f, startY, width, startY + 12f, cropLinePaint)
        }

        // 7. Silhouetted Futuristic Barn, Silo, & Rotating Windmill
        renderFarmstead(canvas, width, height)
    }

    private fun renderFarmstead(canvas: Canvas, width: Float, height: Float) {
        farmPaint.color = Color.rgb(10, 14, 24)
        farmPaint.style = Paint.Style.FILL

        val baseY = height * 0.83f

        // High-Tech Barn
        val barnX = width * 0.12f
        val barnW = 120f
        val barnH = 80f

        val barnPath = Path()
        barnPath.moveTo(barnX, baseY)
        barnPath.lineTo(barnX, baseY - barnH + 28f)
        barnPath.lineTo(barnX + barnW / 2f, baseY - barnH)
        barnPath.lineTo(barnX + barnW, baseY - barnH + 28f)
        barnPath.lineTo(barnX + barnW, baseY)
        barnPath.close()
        canvas.drawPath(barnPath, farmPaint)

        // Illuminated warm barn windows
        windowPaint.color = Color.argb(190, 255, 215, 80)
        windowPaint.style = Paint.Style.FILL
        canvas.drawRoundRect(RectF(barnX + 22f, baseY - 45f, barnX + 46f, baseY - 22f), 4f, 4f, windowPaint)
        canvas.drawRoundRect(RectF(barnX + 74f, baseY - 45f, barnX + 98f, baseY - 22f), 4f, 4f, windowPaint)

        // Futuristic Silo
        val siloX = barnX + barnW + 12f
        val siloW = 44f
        val siloH = 105f
        val siloRect = RectF(siloX, baseY - siloH, siloX + siloW, baseY)
        canvas.drawRoundRect(siloRect, 22f, 22f, farmPaint)

        // Neon power ring around silo
        val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(56, 189, 248)
            strokeWidth = 2.5f
            style = Paint.Style.STROKE
        }
        canvas.drawOval(RectF(siloX + 2f, baseY - siloH + 26f, siloX + siloW - 2f, baseY - siloH + 34f), ringPaint)

        // High-Tech Wind Turbine / Windmill
        val millX = width * 0.84f
        val millBase = baseY - 8f
        val towerH = 135f

        // Tower
        val towerPath = Path()
        towerPath.moveTo(millX - 10f, millBase)
        towerPath.lineTo(millX - 4f, millBase - towerH)
        towerPath.lineTo(millX + 4f, millBase - towerH)
        towerPath.lineTo(millX + 10f, millBase)
        towerPath.close()
        canvas.drawPath(towerPath, farmPaint)

        // Rotating Windmill Blades
        val hubX = millX
        val hubY = millBase - towerH
        val bladeLength = 52f

        bladePaint.color = Color.rgb(20, 28, 45)
        bladePaint.style = Paint.Style.STROKE
        bladePaint.strokeWidth = 5.5f

        canvas.save()
        canvas.translate(hubX, hubY)
        canvas.rotate(windmillAngle)

        for (i in 0 until 3) {
            val angleRad = (i * 2.0 * Math.PI / 3.0).toFloat()
            val bx = cos(angleRad) * bladeLength
            val by = sin(angleRad) * bladeLength
            canvas.drawLine(0f, 0f, bx, by, bladePaint)
        }
        canvas.restore()

        // Windmill center hub LED
        val hubPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(239, 68, 68)
            style = Paint.Style.FILL
        }
        canvas.drawCircle(hubX, hubY, 4.5f, hubPaint)
    }
}

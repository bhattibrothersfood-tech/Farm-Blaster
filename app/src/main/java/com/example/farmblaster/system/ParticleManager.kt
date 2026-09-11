package com.example.farmblaster.system

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.example.farmblaster.model.FloatingText
import com.example.farmblaster.model.Particle
import com.example.farmblaster.model.ParticleShape
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * High-performance object-pooled particle system and screen shake manager.
 * Pre-allocates memory to guarantee zero GC during gameplay.
 */
class ParticleManager(maxParticles: Int = 320, maxTexts: Int = 24) {

    val particles = Array(maxParticles) { Particle() }
    val floatingTexts = Array(maxTexts) { FloatingText() }

    var screenShakeIntensity = 0f
    var screenShakeDuration = 0f
    var shakeOffsetX = 0f
    var shakeOffsetY = 0f

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 34f
        isFakeBoldText = true
    }
    private val featherPath = Path()
    private val rectF = RectF()

    fun triggerShake(intensity: Float, duration: Float) {
        screenShakeIntensity = intensity.coerceAtLeast(screenShakeIntensity)
        screenShakeDuration = duration.coerceAtLeast(screenShakeDuration)
    }

    fun spawnFeatherExplosion(x: Float, y: Float, primaryColor: Int, secondaryColor: Int, count: Int = 14) {
        for (i in 0 until count) {
            val p = getAvailableParticle() ?: break
            val angle = (Math.random() * 2.0 * PI).toFloat()
            val speed = (40f + Math.random().toFloat() * 160f)
            p.x = x
            p.y = y
            p.vx = cos(angle) * speed
            p.vy = sin(angle) * speed - 30f // Slight upward flutter
            p.color = if (i % 2 == 0) primaryColor else secondaryColor
            p.size = (12f + Math.random().toFloat() * 14f)
            p.alpha = 1f
            p.decay = (0.7f + Math.random().toFloat() * 0.7f)
            p.rotation = (Math.random() * 360f).toFloat()
            p.rotationSpeed = (-180f + Math.random().toFloat() * 360f)
            p.shape = ParticleShape.FEATHER
            p.isAlive = true
        }
    }

    fun spawnSparks(x: Float, y: Float, color: Int, count: Int = 8) {
        for (i in 0 until count) {
            val p = getAvailableParticle() ?: break
            val angle = (Math.random() * 2.0 * PI).toFloat()
            val speed = (80f + Math.random().toFloat() * 200f)
            p.x = x
            p.y = y
            p.vx = cos(angle) * speed
            p.vy = sin(angle) * speed
            p.color = color
            p.size = (4f + Math.random().toFloat() * 6f)
            p.alpha = 1f
            p.decay = (2.5f + Math.random().toFloat() * 1.5f)
            p.rotation = 0f
            p.rotationSpeed = 0f
            p.shape = ParticleShape.SPARK
            p.isAlive = true
        }
    }

    fun spawnEggImpact(x: Float, y: Float, isExplosive: Boolean = false) {
        // Yolk splatters (gold/yellow)
        val yolkColor = Color.rgb(255, 190, 20)
        val shellColor = Color.rgb(245, 240, 235)
        val count = if (isExplosive) 24 else 12

        for (i in 0 until count) {
            val p = getAvailableParticle() ?: break
            val angle = (Math.random() * 2.0 * PI).toFloat()
            val speed = (60f + Math.random().toFloat() * 180f)
            p.x = x
            p.y = y
            p.vx = cos(angle) * speed
            p.vy = sin(angle) * speed
            p.color = if (i % 2 == 0) yolkColor else shellColor
            p.size = (6f + Math.random().toFloat() * 9f)
            p.alpha = 1f
            p.decay = (1.2f + Math.random().toFloat() * 1.0f)
            p.rotation = (Math.random() * 360f).toFloat()
            p.rotationSpeed = (-120f + Math.random().toFloat() * 240f)
            p.shape = if (i % 2 == 0) ParticleShape.YOLK else ParticleShape.EGG_SHARD
            p.isAlive = true
        }

        if (isExplosive) {
            spawnShockwaveRing(x, y, Color.rgb(255, 100, 20), 45f)
            triggerShake(14f, 0.25f)
        }
    }

    fun spawnShockwaveRing(x: Float, y: Float, color: Int, initialSize: Float = 20f) {
        val p = getAvailableParticle() ?: return
        p.x = x
        p.y = y
        p.vx = 0f
        p.vy = 0f
        p.color = color
        p.size = initialSize
        p.alpha = 1f
        p.decay = 2.0f
        p.rotation = 0f
        p.rotationSpeed = 0f
        p.shape = ParticleShape.RING
        p.isAlive = true
    }

    fun spawnBombDetonation(x: Float, y: Float, screenWidth: Float, screenHeight: Float) {
        // Massive screen shake
        triggerShake(28f, 0.6f)

        // Multiple giant expanding rings
        spawnShockwaveRing(x, y, Color.rgb(255, 230, 150), 30f)
        spawnShockwaveRing(x, y, Color.rgb(255, 80, 20), 50f)

        // Shower of smoke, fiery sparks, and feathers
        for (i in 0 until 40) {
            val p = getAvailableParticle() ?: break
            val angle = (Math.random() * 2.0 * PI).toFloat()
            val speed = (120f + Math.random().toFloat() * 320f)
            p.x = x
            p.y = y
            p.vx = cos(angle) * speed
            p.vy = sin(angle) * speed
            p.color = when (i % 3) {
                0 -> Color.rgb(255, 220, 50)
                1 -> Color.rgb(255, 70, 20)
                else -> Color.rgb(240, 240, 240)
            }
            p.size = (10f + Math.random().toFloat() * 16f)
            p.alpha = 1f
            p.decay = (0.9f + Math.random().toFloat() * 0.8f)
            p.rotation = (Math.random() * 360f).toFloat()
            p.rotationSpeed = (-200f + Math.random().toFloat() * 400f)
            p.shape = if (i % 2 == 0) ParticleShape.SMOKE else ParticleShape.FEATHER
            p.isAlive = true
        }
    }

    fun addFloatingText(x: Float, y: Float, text: String, color: Int, scale: Float = 1f) {
        for (ft in floatingTexts) {
            if (!ft.isAlive) {
                ft.x = x
                ft.y = y
                ft.text = text
                ft.color = color
                ft.alpha = 1f
                ft.vy = -55f
                ft.scale = scale
                ft.isAlive = true
                break
            }
        }
    }

    fun update(dt: Float) {
        // Update screen shake
        if (screenShakeDuration > 0f) {
            screenShakeDuration -= dt
            val currentIntensity = screenShakeIntensity * (screenShakeDuration / 0.5f).coerceIn(0f, 1f)
            shakeOffsetX = ((Math.random() * 2.0 - 1.0) * currentIntensity).toFloat()
            shakeOffsetY = ((Math.random() * 2.0 - 1.0) * currentIntensity).toFloat()
        } else {
            screenShakeIntensity = 0f
            shakeOffsetX = 0f
            shakeOffsetY = 0f
        }

        // Update particles
        for (p in particles) {
            if (!p.isAlive) continue
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.rotation += p.rotationSpeed * dt

            // Air drag / gravity depending on shape
            when (p.shape) {
                ParticleShape.FEATHER -> {
                    p.vx *= 0.96f
                    p.vy += 45f * dt // gentle gravity
                }
                ParticleShape.SPARK -> {
                    p.vx *= 0.94f
                    p.vy *= 0.94f
                }
                ParticleShape.RING -> {
                    p.size += 260f * dt // rapidly expands
                }
                ParticleShape.SMOKE -> {
                    p.size += 18f * dt
                    p.vy -= 20f * dt // rises
                }
                ParticleShape.EGG_SHARD, ParticleShape.YOLK -> {
                    p.vy += 120f * dt // drops fast
                }
                else -> {}
            }

            p.alpha -= p.decay * dt
            if (p.alpha <= 0f) {
                p.isAlive = false
            }
        }

        // Update floating texts
        for (ft in floatingTexts) {
            if (!ft.isAlive) continue
            ft.y += ft.vy * dt
            ft.alpha -= 0.9f * dt
            if (ft.alpha <= 0f) {
                ft.isAlive = false
            }
        }
    }

    fun render(canvas: Canvas) {
        for (p in particles) {
            if (!p.isAlive) continue
            val alphaInt = (p.alpha.coerceIn(0f, 1f) * 255).toInt()
            if (alphaInt <= 0) continue

            when (p.shape) {
                ParticleShape.FEATHER -> {
                    fillPaint.color = p.color
                    fillPaint.alpha = alphaInt
                    canvas.save()
                    canvas.translate(p.x, p.y)
                    canvas.rotate(p.rotation)
                    // Draw teardrop quill feather
                    featherPath.reset()
                    featherPath.moveTo(0f, -p.size)
                    featherPath.cubicTo(p.size * 0.45f, -p.size * 0.4f, p.size * 0.35f, p.size * 0.6f, 0f, p.size)
                    featherPath.cubicTo(-p.size * 0.35f, p.size * 0.6f, -p.size * 0.45f, -p.size * 0.4f, 0f, -p.size)
                    canvas.drawPath(featherPath, fillPaint)
                    canvas.restore()
                }
                ParticleShape.SPARK -> {
                    fillPaint.color = p.color
                    fillPaint.alpha = alphaInt
                    canvas.drawCircle(p.x, p.y, p.size, fillPaint)
                }
                ParticleShape.RING -> {
                    strokePaint.color = p.color
                    strokePaint.alpha = alphaInt
                    strokePaint.strokeWidth = 6f * p.alpha
                    canvas.drawCircle(p.x, p.y, p.size, strokePaint)
                }
                ParticleShape.SMOKE -> {
                    fillPaint.color = p.color
                    fillPaint.alpha = (alphaInt * 0.5f).toInt()
                    canvas.drawCircle(p.x, p.y, p.size, fillPaint)
                }
                ParticleShape.EGG_SHARD -> {
                    fillPaint.color = p.color
                    fillPaint.alpha = alphaInt
                    canvas.save()
                    canvas.translate(p.x, p.y)
                    canvas.rotate(p.rotation)
                    rectF.set(-p.size / 2f, -p.size / 3f, p.size / 2f, p.size / 3f)
                    canvas.drawRoundRect(rectF, 2f, 2f, fillPaint)
                    canvas.restore()
                }
                ParticleShape.YOLK -> {
                    fillPaint.color = p.color
                    fillPaint.alpha = alphaInt
                    canvas.drawCircle(p.x, p.y, p.size, fillPaint)
                }
                else -> {
                    fillPaint.color = p.color
                    fillPaint.alpha = alphaInt
                    canvas.drawCircle(p.x, p.y, p.size, fillPaint)
                }
            }
        }

        // Render floating texts
        for (ft in floatingTexts) {
            if (!ft.isAlive) continue
            val alphaInt = (ft.alpha.coerceIn(0f, 1f) * 255).toInt()
            if (alphaInt <= 0) continue
            textPaint.color = ft.color
            textPaint.alpha = alphaInt
            textPaint.textSize = 32f * ft.scale
            canvas.drawText(ft.text, ft.x, ft.y, textPaint)
        }
    }

    private fun getAvailableParticle(): Particle? {
        for (p in particles) {
            if (!p.isAlive) return p
        }
        return null
    }

    fun clear() {
        for (p in particles) p.isAlive = false
        for (ft in floatingTexts) ft.isAlive = false
        screenShakeDuration = 0f
        screenShakeIntensity = 0f
        shakeOffsetX = 0f
        shakeOffsetY = 0f
    }
}

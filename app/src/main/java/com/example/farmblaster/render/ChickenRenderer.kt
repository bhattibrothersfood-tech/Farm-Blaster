package com.example.farmblaster.render

import android.graphics.Bitmap
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
import com.example.farmblaster.model.BossChicken
import com.example.farmblaster.model.Chicken
import com.example.farmblaster.model.ChickenBreed
import kotlin.math.PI
import kotlin.math.sin

/**
 * Renders realistic, detailed chickens and colossal bosses.
 * Uses pre-rendered high-definition sprite frame buffers for 60 FPS silky performance.
 */
class ChickenRenderer {

    private val spriteWidth = 140
    private val spriteHeight = 130
    private val frameCount = 4

    // Breed -> Array of animation frames (0: up, 1: mid-up, 2: glide, 3: down)
    private val normalSprites = mutableMapOf<ChickenBreed, Array<Bitmap>>()
    private val hitFlashSprites = mutableMapOf<ChickenBreed, Array<Bitmap>>()

    private val drawPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val bossHpBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(180, 20, 20, 30)
        style = Paint.Style.FILL
    }
    private val bossHpBarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(239, 68, 68)
        style = Paint.Style.FILL
    }
    private val bossHpBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(255, 215, 0)
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }
    private val bossTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(255, 230, 100)
        textSize = 28f
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
    }

    init {
        initSprites()
    }

    private fun initSprites() {
        for (breed in ChickenBreed.values()) {
            if (breed == ChickenBreed.BOSS) continue // Boss is rendered dynamically at colossal scale
            val frames = Array(frameCount) { f ->
                createChickenFrameBitmap(breed, f, isHitFlash = false)
            }
            val flashFrames = Array(frameCount) { f ->
                createChickenFrameBitmap(breed, f, isHitFlash = true)
            }
            normalSprites[breed] = frames
            hitFlashSprites[breed] = flashFrames
        }
    }

    private fun createChickenFrameBitmap(breed: ChickenBreed, frame: Int, isHitFlash: Boolean): Bitmap {
        val bitmap = Bitmap.createBitmap(spriteWidth, spriteHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val p = Paint(Paint.ANTI_ALIAS_FLAG)

        if (isHitFlash) {
            drawRealisticChicken(canvas, breed, frame, p)
            // Apply bright white/red hit flash overlay
            val flashPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                colorFilter = PorterDuffColorFilter(Color.argb(230, 255, 255, 255), PorterDuff.Mode.SRC_IN)
            }
            val flashBmp = Bitmap.createBitmap(spriteWidth, spriteHeight, Bitmap.Config.ARGB_8888)
            val flashCanvas = Canvas(flashBmp)
            flashCanvas.drawBitmap(bitmap, 0f, 0f, flashPaint)
            bitmap.recycle()
            return flashBmp
        }

        drawRealisticChicken(canvas, breed, frame, p)
        return bitmap
    }

    /**
     * Draws an anatomically detailed, realistic chicken with feathered body, wings, head, comb, wattles, eyes, and beak.
     */
    private fun drawRealisticChicken(canvas: Canvas, breed: ChickenBreed, frame: Int, paint: Paint) {
        val cx = spriteWidth / 2f
        val cy = spriteHeight / 2f + 6f

        val baseCol = breed.primaryColor.toInt()
        val secCol = breed.secondaryColor.toInt()

        // 1. Tucked legs & talons
        paint.shader = null
        paint.color = Color.rgb(220, 160, 40)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        canvas.drawLine(cx - 14f, cy + 28f, cx - 14f, cy + 42f, paint)
        canvas.drawLine(cx + 14f, cy + 28f, cx + 14f, cy + 42f, paint)
        // Claws
        canvas.drawLine(cx - 14f, cy + 42f, cx - 22f, cy + 48f, paint)
        canvas.drawLine(cx - 14f, cy + 42f, cx - 14f, cy + 50f, paint)
        canvas.drawLine(cx - 14f, cy + 42f, cx - 6f, cy + 48f, paint)
        canvas.drawLine(cx + 14f, cy + 42f, cx + 6f, cy + 48f, paint)
        canvas.drawLine(cx + 14f, cy + 42f, cx + 14f, cy + 50f, paint)
        canvas.drawLine(cx + 14f, cy + 42f, cx + 22f, cy + 48f, paint)

        // 2. Wings (Flapping animation depending on frame)
        // frame 0: up, 1: mid-up, 2: glide, 3: down
        val wingAngleDeg = when (frame) {
            0 -> -28f
            1 -> -10f
            2 -> 8f
            else -> 24f
        }
        drawRealisticWing(canvas, cx - 22f, cy + 4f, -1f, wingAngleDeg, baseCol, secCol, breed)
        drawRealisticWing(canvas, cx + 22f, cy + 4f, 1f, wingAngleDeg, baseCol, secCol, breed)

        // 3. Main Plumage Body (Realistic layered feather egg shape)
        paint.style = Paint.Style.FILL
        paint.shader = RadialGradient(
            cx, cy - 8f, 38f,
            intArrayOf(secCol, baseCol, darken(baseCol, 0.75f)),
            floatArrayOf(0f, 0.65f, 1f),
            Shader.TileMode.CLAMP
        )
        val bodyRect = RectF(cx - 30f, cy - 20f, cx + 30f, cy + 34f)
        canvas.drawOval(bodyRect, paint)

        // Layered feather scallops along breast
        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.2f
        paint.color = darken(secCol, 0.85f)
        val scallopY = floatArrayOf(cy - 4f, cy + 6f, cy + 16f, cy + 24f)
        for (sy in scallopY) {
            val scPath = Path()
            scPath.moveTo(cx - 18f, sy)
            scPath.quadTo(cx - 9f, sy + 6f, cx, sy)
            scPath.quadTo(cx + 9f, sy + 6f, cx + 18f, sy)
            canvas.drawPath(scPath, paint)
        }

        // 4. Armored Plates for Armored Chicken breed
        if (breed == ChickenBreed.ARMORED) {
            paint.style = Paint.Style.FILL
            paint.shader = LinearGradient(
                cx - 24f, cy, cx + 24f, cy + 20f,
                Color.rgb(180, 195, 210), Color.rgb(100, 115, 130), Shader.TileMode.CLAMP
            )
            val armorRect = RectF(cx - 22f, cy - 8f, cx + 22f, cy + 22f)
            canvas.drawRoundRect(armorRect, 8f, 8f, paint)
            // Steel rivets
            paint.shader = null
            paint.color = Color.rgb(240, 245, 250)
            canvas.drawCircle(cx - 16f, cy - 2f, 2.5f, paint)
            canvas.drawCircle(cx + 16f, cy - 2f, 2.5f, paint)
            canvas.drawCircle(cx - 16f, cy + 16f, 2.5f, paint)
            canvas.drawCircle(cx + 16f, cy + 16f, 2.5f, paint)
        }

        // 5. Head and Neck Ruffle (Hackles)
        paint.style = Paint.Style.FILL
        paint.shader = RadialGradient(
            cx, cy - 26f, 26f,
            intArrayOf(secCol, baseCol, darken(baseCol, 0.8f)),
            floatArrayOf(0f, 0.7f, 1f),
            Shader.TileMode.CLAMP
        )
        val headRect = RectF(cx - 22f, cy - 42f, cx + 22f, cy - 10f)
        canvas.drawOval(headRect, paint)

        // Pointed neck hackle feathers
        paint.shader = null
        paint.style = Paint.Style.FILL
        paint.color = darken(baseCol, 0.9f)
        val hacklePath = Path()
        hacklePath.moveTo(cx - 20f, cy - 16f)
        hacklePath.lineTo(cx - 12f, cy - 8f)
        hacklePath.lineTo(cx - 6f, cy - 14f)
        hacklePath.lineTo(cx, cy - 6f)
        hacklePath.lineTo(cx + 6f, cy - 14f)
        hacklePath.lineTo(cx + 12f, cy - 8f)
        hacklePath.lineTo(cx + 20f, cy - 16f)
        canvas.drawPath(hacklePath, paint)

        // 6. Fleshy Comb (Red serrated crown with natural lobes)
        paint.shader = LinearGradient(
            cx, cy - 62f, cx, cy - 38f,
            Color.rgb(245, 40, 40), Color.rgb(180, 15, 15), Shader.TileMode.CLAMP
        )
        val combPath = Path()
        combPath.moveTo(cx - 16f, cy - 38f)
        combPath.cubicTo(cx - 16f, cy - 48f, cx - 11f, cy - 54f, cx - 9f, cy - 42f)
        combPath.cubicTo(cx - 7f, cy - 58f, cx - 2f, cy - 62f, cx, cy - 44f)
        combPath.cubicTo(cx + 2f, cy - 58f, cx + 7f, cy - 62f, cx + 9f, cy - 42f)
        combPath.cubicTo(cx + 11f, cy - 54f, cx + 16f, cy - 48f, cx + 16f, cy - 38f)
        combPath.close()
        canvas.drawPath(combPath, paint)

        // 7. Wattles (Dual hanging fleshy dewlaps)
        paint.shader = LinearGradient(
            cx, cy - 16f, cx, cy - 2f,
            Color.rgb(230, 30, 30), Color.rgb(160, 10, 10), Shader.TileMode.CLAMP
        )
        val wattleLeft = RectF(cx - 9f, cy - 18f, cx - 2f, cy - 2f)
        val wattleRight = RectF(cx + 2f, cy - 18f, cx + 9f, cy - 2f)
        canvas.drawOval(wattleLeft, paint)
        canvas.drawOval(wattleRight, paint)

        // 8. Curved Beak (Sharp horn color with nostril)
        paint.shader = LinearGradient(
            cx - 8f, cy - 26f, cx + 8f, cy - 16f,
            Color.rgb(255, 185, 30), Color.rgb(215, 120, 10), Shader.TileMode.CLAMP
        )
        val beakPath = Path()
        beakPath.moveTo(cx - 9f, cy - 26f)
        beakPath.lineTo(cx + 9f, cy - 26f)
        beakPath.lineTo(cx, cy - 14f)
        beakPath.close()
        canvas.drawPath(beakPath, paint)
        // Nostrils
        paint.shader = null
        paint.color = Color.rgb(140, 70, 0)
        canvas.drawCircle(cx - 3f, cy - 23f, 1.2f, paint)
        canvas.drawCircle(cx + 3f, cy - 23f, 1.2f, paint)

        // 9. Realistic Bird Eyes (Amber ring + black pupil + specular gleam)
        for (side in floatArrayOf(-1f, 1f)) {
            val ex = cx + side * 11f
            val ey = cy - 30f
            // Eye socket ring
            paint.color = Color.rgb(210, 80, 20)
            canvas.drawCircle(ex, ey, 6.5f, paint)
            // Golden Iris
            paint.color = Color.rgb(255, 210, 40)
            canvas.drawCircle(ex, ey, 5.2f, paint)
            // Black Pupil
            paint.color = Color.BLACK
            canvas.drawCircle(ex, ey, 3.2f, paint)
            // White Specular Highlight
            paint.color = Color.WHITE
            canvas.drawCircle(ex - 1.2f, ey - 1.5f, 1.5f, paint)
        }

        // Special: Elite Chicken Cyber Visor
        if (breed == ChickenBreed.ELITE) {
            paint.color = Color.argb(200, 0, 230, 255)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 3f
            canvas.drawLine(cx - 18f, cy - 30f, cx + 18f, cy - 30f, paint)
        }
    }

    private fun drawRealisticWing(
        canvas: Canvas,
        pivotX: Float,
        pivotY: Float,
        side: Float,
        angleDeg: Float,
        baseCol: Int,
        secCol: Int,
        breed: ChickenBreed
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        canvas.save()
        canvas.translate(pivotX, pivotY)
        canvas.rotate(side * angleDeg)

        // 3 Layered primary & secondary flight feathers
        for (f in 0..2) {
            val featherPath = Path()
            val fx = side * (10f + f * 10f)
            val fy = f * 6f
            val length = 32f - f * 4f

            featherPath.moveTo(0f, 0f)
            featherPath.cubicTo(
                side * 8f, fy - 6f,
                fx, fy + 4f,
                fx + side * 12f, fy + length
            )
            featherPath.cubicTo(
                fx, fy + length + 8f,
                side * 4f, fy + 12f,
                0f, 0f
            )

            paint.style = Paint.Style.FILL
            paint.shader = LinearGradient(
                0f, 0f, fx, fy + length,
                secCol, darken(baseCol, 0.85f), Shader.TileMode.CLAMP
            )
            canvas.drawPath(featherPath, paint)

            paint.shader = null
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1.4f
            paint.color = darken(baseCol, 0.7f)
            canvas.drawPath(featherPath, paint)
        }

        canvas.restore()
    }

    fun renderChicken(canvas: Canvas, chicken: Chicken) {
        if (!chicken.isAlive) return
        val sprites = if (chicken.hitFlashTimer > 0f) hitFlashSprites[chicken.breed] else normalSprites[chicken.breed]
        val frameBmp = sprites?.getOrNull(chicken.flapFrame % frameCount)

        if (frameBmp != null) {
            val left = chicken.x - chicken.width / 2f
            val top = chicken.y - chicken.height / 2f
            val destRect = RectF(left, top, left + chicken.width, top + chicken.height)
            canvas.drawBitmap(frameBmp, null, destRect, drawPaint)

            // Health bar for tough enemies
            if (chicken.maxHp > 1 && chicken.hp < chicken.maxHp) {
                val barW = chicken.width * 0.75f
                val barH = 5f
                val bx = chicken.x - barW / 2f
                val by = top - 8f

                bossHpBgPaint.color = Color.DKGRAY
                canvas.drawRect(bx, by, bx + barW, by + barH, bossHpBgPaint)

                val hpPct = (chicken.hp.toFloat() / chicken.maxHp).coerceIn(0f, 1f)
                bossHpBarPaint.color = Color.rgb(50, 220, 80)
                canvas.drawRect(bx, by, bx + barW * hpPct, by + barH, bossHpBarPaint)
            }
        }
    }

    fun renderBoss(canvas: Canvas, boss: BossChicken, screenWidth: Float) {
        if (!boss.isAlive) return
        val cx = boss.x
        val cy = boss.y
        val p = Paint(Paint.ANTI_ALIAS_FLAG)

        canvas.save()

        // Hit flash tint
        if (boss.hitFlashTimer > 0f) {
            p.colorFilter = PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        }

        // Colossal Boss Wings
        val wingFlap = sin(boss.moveTimer * 7f) * 25f
        drawBossWing(canvas, cx - 60f, cy + 10f, -1f, wingFlap, p)
        drawBossWing(canvas, cx + 60f, cy + 10f, 1f, wingFlap, p)

        // Boss Plumage Body
        p.style = Paint.Style.FILL
        val phaseColor = when (boss.phase) {
            1 -> Color.rgb(180, 20, 20)
            2 -> Color.rgb(220, 30, 10)
            else -> Color.rgb(255, 60, 0) // Rage fiery red
        }
        p.shader = RadialGradient(
            cx, cy, 95f,
            intArrayOf(Color.rgb(255, 140, 30), phaseColor, Color.rgb(90, 5, 5)),
            floatArrayOf(0f, 0.65f, 1f),
            Shader.TileMode.CLAMP
        )
        val bossBody = RectF(cx - 80f, cy - 55f, cx + 80f, cy + 85f)
        canvas.drawOval(bossBody, p)

        // Heavy cyber armor plating
        p.shader = LinearGradient(
            cx - 50f, cy - 20f, cx + 50f, cy + 45f,
            Color.rgb(200, 210, 225), Color.rgb(110, 125, 145), Shader.TileMode.CLAMP
        )
        val breastPlate = RectF(cx - 50f, cy - 15f, cx + 50f, cy + 50f)
        canvas.drawRoundRect(breastPlate, 16f, 16f, p)

        // Boss Head
        p.shader = RadialGradient(
            cx, cy - 70f, 65f,
            intArrayOf(Color.rgb(255, 160, 40), phaseColor, Color.rgb(80, 0, 0)),
            floatArrayOf(0f, 0.7f, 1f),
            Shader.TileMode.CLAMP
        )
        val bossHead = RectF(cx - 55f, cy - 115f, cx + 55f, cy - 35f)
        canvas.drawOval(bossHead, p)

        // Imperial Golden Crown Comb
        p.shader = LinearGradient(
            cx, cy - 150f, cx, cy - 100f,
            Color.rgb(255, 220, 60), Color.rgb(200, 140, 10), Shader.TileMode.CLAMP
        )
        val crownPath = Path()
        crownPath.moveTo(cx - 40f, cy - 105f)
        crownPath.lineTo(cx - 30f, cy - 155f)
        crownPath.lineTo(cx - 15f, cy - 125f)
        crownPath.lineTo(cx, cy - 165f)
        crownPath.lineTo(cx + 15f, cy - 125f)
        crownPath.lineTo(cx + 30f, cy - 155f)
        crownPath.lineTo(cx + 40f, cy - 105f)
        crownPath.close()
        canvas.drawPath(crownPath, p)

        // Giant Beak
        p.shader = LinearGradient(
            cx, cy - 75f, cx, cy - 40f,
            Color.rgb(255, 200, 30), Color.rgb(220, 100, 0), Shader.TileMode.CLAMP
        )
        val beakPath = Path()
        beakPath.moveTo(cx - 24f, cy - 75f)
        beakPath.lineTo(cx + 24f, cy - 75f)
        beakPath.lineTo(cx, cy - 38f)
        beakPath.close()
        canvas.drawPath(beakPath, p)

        // Massive Red Wattles
        p.shader = null
        p.color = Color.rgb(220, 20, 20)
        canvas.drawOval(RectF(cx - 22f, cy - 45f, cx - 6f, cy - 10f), p)
        canvas.drawOval(RectF(cx + 6f, cy - 45f, cx + 22f, cy - 10f), p)

        // Glowing Boss Eyes (Left: cyber optic, Right: enraged eagle eye)
        // Left cyber eye
        p.color = Color.CYAN
        canvas.drawCircle(cx - 25f, cy - 80f, 12f, p)
        p.color = Color.WHITE
        canvas.drawCircle(cx - 25f, cy - 80f, 5f, p)
        // Right eye
        p.color = Color.rgb(255, 60, 0)
        canvas.drawCircle(cx + 25f, cy - 80f, 12f, p)
        p.color = Color.BLACK
        canvas.drawCircle(cx + 25f, cy - 80f, 6f, p)
        p.color = Color.WHITE
        canvas.drawCircle(cx + 22f, cy - 83f, 3f, p)

        canvas.restore()

        // Boss Health Bar at top of screen
        renderBossHealthBar(canvas, boss, screenWidth)
    }

    private fun drawBossWing(canvas: Canvas, px: Float, py: Float, side: Float, angle: Float, paint: Paint) {
        canvas.save()
        canvas.translate(px, py)
        canvas.rotate(side * angle)

        val wingPath = Path()
        wingPath.moveTo(0f, 0f)
        wingPath.cubicTo(side * 50f, -30f, side * 110f, 10f, side * 140f, 70f)
        wingPath.cubicTo(side * 110f, 100f, side * 40f, 80f, 0f, 0f)

        paint.shader = LinearGradient(
            0f, 0f, side * 140f, 70f,
            Color.rgb(255, 120, 30), Color.rgb(120, 10, 10), Shader.TileMode.CLAMP
        )
        paint.style = Paint.Style.FILL
        canvas.drawPath(wingPath, paint)

        // Wing armor blades
        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        paint.color = Color.rgb(220, 230, 245)
        canvas.drawPath(wingPath, paint)

        canvas.restore()
    }

    private fun renderBossHealthBar(canvas: Canvas, boss: BossChicken, screenWidth: Float) {
        val barMargin = 40f
        val barTop = 90f
        val barHeight = 22f
        val barWidth = screenWidth - barMargin * 2f

        // Background
        val bgRect = RectF(barMargin, barTop, barMargin + barWidth, barTop + barHeight)
        canvas.drawRoundRect(bgRect, 8f, 8f, bossHpBgPaint)

        // Fill
        val hpPct = (boss.hp.toFloat() / boss.maxHp).coerceIn(0f, 1f)
        val fillRect = RectF(barMargin + 2f, barTop + 2f, barMargin + 2f + (barWidth - 4f) * hpPct, barTop + barHeight - 2f)
        bossHpBarPaint.color = when (boss.phase) {
            1 -> Color.rgb(239, 68, 68)
            2 -> Color.rgb(249, 115, 22)
            else -> Color.rgb(220, 38, 38)
        }
        canvas.drawRoundRect(fillRect, 6f, 6f, bossHpBarPaint)

        // Border
        canvas.drawRoundRect(bgRect, 8f, 8f, bossHpBorderPaint)

        // Text
        val phaseLabel = "MOTHER CLUCKER - PHASE ${boss.phase}"
        canvas.drawText(phaseLabel, screenWidth / 2f, barTop - 10f, bossTextPaint)
    }

    private fun darken(color: Int, factor: Float): Int {
        val a = Color.alpha(color)
        val r = (Color.red(color) * factor).toInt().coerceIn(0, 255)
        val g = (Color.green(color) * factor).toInt().coerceIn(0, 255)
        val b = (Color.blue(color) * factor).toInt().coerceIn(0, 255)
        return Color.argb(a, r, g, b)
    }
}

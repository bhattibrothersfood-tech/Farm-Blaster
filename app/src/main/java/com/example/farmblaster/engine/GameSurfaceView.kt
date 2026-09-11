package com.example.farmblaster.engine

import android.content.Context
import android.graphics.PixelFormat
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView

/**
 * Dedicated high-speed native SurfaceView running an optimized 60 FPS game loop thread.
 * Handles multi-touch input and hardware double-buffered Canvas rendering.
 */
class GameSurfaceView(
    context: Context,
    val engine: GameEngine
) : SurfaceView(context), SurfaceHolder.Callback, Runnable {

    private var gameThread: Thread? = null
    @Volatile private var isRunning = false

    private val targetFps = 60
    private val targetFrameTimeMs = 1000L / targetFps

    // Active touch tracking
    private var steerPointerId = -1
    private var lastTapTime = 0L

    init {
        holder.addCallback(this)
        holder.setFormat(PixelFormat.RGBA_8888)
        isFocusable = true
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        engine.initDimensions(width.toFloat(), height.toFloat())
        startLoop()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        engine.screenWidth = width.toFloat()
        engine.screenHeight = height.toFloat()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        stopLoop()
    }

    fun startLoop() {
        if (isRunning) return
        isRunning = true
        gameThread = Thread(this, "GameLoopThread").apply {
            priority = Thread.MAX_PRIORITY
            start()
        }
    }

    fun stopLoop() {
        isRunning = false
        try {
            gameThread?.join(500)
            gameThread = null
        } catch (_: InterruptedException) {
        }
    }

    override fun run() {
        var lastTime = System.nanoTime()

        while (isRunning) {
            val now = System.nanoTime()
            val elapsedSec = (now - lastTime) / 1_000_000_000f
            lastTime = now

            // Cap delta time to prevent physics tunneling
            val dt = elapsedSec.coerceIn(0.001f, 0.035f)

            // Update simulation
            engine.update(dt)

            // Render frame
            var canvas = holder.lockCanvas()
            if (canvas != null) {
                try {
                    engine.render(canvas)
                } finally {
                    holder.unlockCanvasAndPost(canvas)
                }
            }

            // Regulate frame rate to 60 FPS
            val frameDurationMs = (System.nanoTime() - now) / 1_000_000L
            val sleepMs = targetFrameTimeMs - frameDurationMs
            if (sleepMs > 0) {
                try {
                    Thread.sleep(sleepMs)
                } catch (_: InterruptedException) {
                    break
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.actionMasked
        val actionIndex = event.actionIndex

        when (action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                val pointerId = event.getPointerId(actionIndex)
                val x = event.getX(actionIndex)
                val y = event.getY(actionIndex)

                // Double tap anywhere triggers emergency bomb if player has bombs
                val now = System.currentTimeMillis()
                if (now - lastTapTime < 320L) {
                    engine.triggerBomb()
                }
                lastTapTime = now

                // Direct or drag steering for player ship
                if (steerPointerId == -1) {
                    steerPointerId = pointerId
                    engine.movePlayer(x, y)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                for (i in 0 until event.pointerCount) {
                    if (event.getPointerId(i) == steerPointerId) {
                        engine.movePlayer(event.getX(i), event.getY(i))
                        break
                    }
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                val pointerId = event.getPointerId(actionIndex)
                if (pointerId == steerPointerId) {
                    steerPointerId = -1
                }
            }

            MotionEvent.ACTION_CANCEL -> {
                steerPointerId = -1
            }
        }

        return true
    }
}

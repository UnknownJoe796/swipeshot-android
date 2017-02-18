package com.ivieleague.swipeshot.game

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import com.ivieleague.swipeshot.clear
import com.ivieleague.swipeshot.math.PolygonF
import com.lightningkite.kotlin.runAll
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

class GameWorld(var myPlayerId: String) {

    companion object {
        val commonPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = .1f
        }
    }

    @Transient val stepQueue = ConcurrentLinkedQueue<GameWorld.(Float) -> Unit>()
    @Transient val stepListeners = ConcurrentLinkedQueue<GameWorld.(Float) -> Unit>()
    @Transient val controlOverlayListeners = ConcurrentLinkedQueue<GameWorld.(Canvas) -> Unit>()

    var cameraWorldUnitsToShow = 20f
    val cameraPosition = PointF(0f, 0f)

    val players = HashMap<String, Player>()
    val environment = ArrayList<Wall>()

    //test setup
    init {
        testSetup()
    }

    fun testSetup() {
        players[myPlayerId] = Player()
        environment += Wall(PolygonF(mutableListOf(
                PointF(5f, 5f),
                PointF(5f, 10f),
                PointF(10f, 10f),
                PointF(10f, 5f)
        )))
        environment += Wall(PolygonF(mutableListOf(
                PointF(-5f, -5f),
                PointF(-5f, -10f),
                PointF(-10f, -5f)
        )))
    }

    fun step(timePassed: Float) {
        stepQueue.clear { it.invoke(this, timePassed) }
        stepListeners.runAll(this, timePassed)
        players.forEach { it.value.step(this, timePassed) }

        cameraPosition.set(players[myPlayerId]?.position ?: PointF(0f, 0f))
    }

    fun render(canvas: Canvas) {
        canvas.drawColor(Color.WHITE)
        canvas.save()
        canvas.translate(canvas.width / 2f, canvas.height / 2f)
        val minSide = canvas.width.coerceAtMost(canvas.height)
        val scale = cameraWorldUnitsToShow / minSide
        canvas.scale(1 / scale, 1 / scale)
        canvas.translate(-cameraPosition.x, -cameraPosition.y)

        players.forEach { it.value.render(canvas) }
        environment.forEach { it.render(canvas) }

        canvas.restore()

        controlOverlayListeners.runAll(this, canvas)
    }
}
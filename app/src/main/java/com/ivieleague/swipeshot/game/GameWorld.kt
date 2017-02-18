package com.ivieleague.swipeshot.game

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import com.ivieleague.swipeshot.clear
import com.ivieleague.swipeshot.math.PolygonF
import com.lightningkite.kotlin.collection.random
import com.lightningkite.kotlin.runAll
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

class GameWorld(val player: Player?, val networking: NetInterface<Player?>) {

    companion object {
        val millisecondsBetweenMessages = 30L
        val kickOutMilliseconds = 3000L
        val commonPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = .1f
        }
        val textPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.FILL
            textSize = .75f * 100f
            textAlign = Paint.Align.CENTER
        }
    }

    @Transient val stepQueue = ConcurrentLinkedQueue<GameWorld.(Float) -> Unit>()
    @Transient val stepListeners = ConcurrentLinkedQueue<GameWorld.(Float) -> Unit>()
    @Transient val controlOverlayListeners = ConcurrentLinkedQueue<GameWorld.(Canvas) -> Unit>()

    var cameraWorldUnitsToShow = 20f
    val cameraPosition = PointF(0f, 0f)

    val spawnPoints = ArrayList<PointF>()
    val players = HashMap<String, Player>()
    val environment = ArrayList<Wall>()

    //test setup
    init {
        testLevel()

        if (player != null) {
            players[player.id] = player
            player.position.set(selectSpawnPoint())
        }
    }

    fun selectSpawnPoint(): PointF = spawnPoints.random()

    fun step(timePassed: Float) {

        //grab the updates from the other users
        networking.receiveQueue.removeAll {
            if (it != null && it.id != player?.id) {
                players[it.id] = it
            }
            true
        }

        //kick out timed out players
        val toRemove = HashSet<String>()
        for ((id, player) in players) {
            if (player != this.player && player.lastMessage + kickOutMilliseconds < System.currentTimeMillis()) {
                toRemove.add(id)
            }
        }
        for (id in toRemove) {
            players.remove(id)
        }

        //step the world forwards
        stepQueue.clear { it.invoke(this, timePassed) }
        stepListeners.runAll(this, timePassed)
        players.forEach { it.value.step(this, timePassed) }

        if (player != null) {
            //if it's time, send a message
//            println("${player.lastMessage + millisecondsBetweenMessages} VS ${System.currentTimeMillis()}")
            if (player.lastMessage + millisecondsBetweenMessages < System.currentTimeMillis()) {
                player.lastMessage = System.currentTimeMillis()

//                println("SENDINGP")
                networking.broadcastQueue.add(player)
            }

            //update the camera position
            cameraPosition.set(player.position)
        }

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


    fun testLevel() {
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

        //outer wall top
        environment += Wall(PolygonF(mutableListOf(
                PointF(-16f, -16f),
                PointF(15f, -16f),
                PointF(15f, -15f),
                PointF(-16f, -15f)
        )))
        //outer wall right
        environment += Wall(PolygonF(mutableListOf(
                PointF(15f, -16f),
                PointF(16f, -16f),
                PointF(16f, 15f),
                PointF(15f, 15f)
        )))
        //outer wall bottom
        environment += Wall(PolygonF(mutableListOf(
                PointF(16f, 15f),
                PointF(16f, 16f),
                PointF(-15f, 16f),
                PointF(-15f, 15f)
        )))
        //outer wall left
        environment += Wall(PolygonF(mutableListOf(
                PointF(-15f, 16f),
                PointF(-16f, 16f),
                PointF(-16f, -15f),
                PointF(-15f, -15f)
        )))


        spawnPoints += PointF(0f, 0f)
        spawnPoints += PointF(13f, 13f)
        spawnPoints += PointF(-13f, -13f)
        spawnPoints += PointF(13f, -13f)
        spawnPoints += PointF(-13f, 13f)
    }
}
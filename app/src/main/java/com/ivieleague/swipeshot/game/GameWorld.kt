package com.ivieleague.swipeshot.game

import android.graphics.*
import com.ivieleague.swipeshot.clear
import com.ivieleague.swipeshot.math.*
import com.lightningkite.kotlin.collection.random
import com.lightningkite.kotlin.runAll
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

class GameWorld(val player: Player?, val world: String = "default", val networking: NetInterface<State?>) {

    companion object {
        val millisecondsBetweenMessages = 30L
        val kickOutMilliseconds = 3000L
        val commonPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = .15f
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

    var cameraWorldUnitsToShow = 30f
    val cameraPosition = PointF(0f, 0f)

    val spawnPoints = ArrayList<PointF>()
    val players = HashMap<String, Player>()
    val environment = ArrayList<Wall>()

    //test setup
    init {
        generateLevel3(world)

        if (player != null) {
            players[player.id] = player
            val spawn = selectSpawnPoint()
            player.position.set(spawn)
            println("SPAWN $spawn")
        }
    }

    fun selectSpawnPoint(): PointF = spawnPoints.random()

    fun handleDeath(player: Player) {
        if (player == this.player)
            selectSpawnPoint()
        else
            player.position.set(2000f, 2000f)
    }

    fun step(timePassed: Float) {

        //grab the updates from the other users
        networking.receiveQueue.removeAll {
            if (it != null && it.player.id != player?.id && it.world == world) {
                players[it.player.id] = it.player.apply {
                    lastMessage = System.currentTimeMillis()
                }
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
                networking.broadcastQueue.add(State(world, player))
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

        GameWorld.commonPaint.alpha = 64
        spawnPoints.forEach { canvas.drawCircle(it.x, it.y, .1f, GameWorld.commonPaint) }
        GameWorld.commonPaint.alpha = 255
        players.forEach { it.value.render(canvas) }
        environment.forEach { it.render(canvas) }

        canvas.restore()

        controlOverlayListeners.runAll(this, canvas)
    }

    fun generateLevel(seed: String, cellWidth: Int = 8, cellHeight: Int = 8, cellSize: Float = 10f) {
        val random = Random(seed.hashCode().toLong() or seed.reversed().hashCode().toLong().shl(32))

        val width = cellWidth * cellSize
        val height = cellHeight * cellSize

        //outer wall top
        environment += Wall(PolygonF(mutableListOf(
                PointF(-1f, -1f),
                PointF(width, -1f),
                PointF(width, 0f),
                PointF(-1f, 0f)
        )))
        //outer wall right
        environment += Wall(PolygonF(mutableListOf(
                PointF(width, -1f),
                PointF(width.plus(1), -1f),
                PointF(width.plus(1), height),
                PointF(width, height)
        )))
        //outer wall bottom
        environment += Wall(PolygonF(mutableListOf(
                PointF(width.plus(1), height),
                PointF(width.plus(1), height.plus(1)),
                PointF(0f, height.plus(1)),
                PointF(0f, height)
        )))
        //outer wall left
        environment += Wall(PolygonF(mutableListOf(
                PointF(0f, height.plus(1)),
                PointF(-1f, height.plus(1)),
                PointF(-1f, 0f),
                PointF(0f, 0f)
        )))

        fun generateCell(cellX: Int, cellY: Int, points: MutableList<PointF>) {
            points.forEach {
                it.x = cellX * cellSize + it.x * cellSize
                it.y = cellY * cellSize + it.y * cellSize
            }
            environment += Wall(PolygonF(points))
        }

        val possibilities: List<(Int, Int) -> Unit> = listOf(
                { x, y -> },
                //                {x, y -> generateCell(x, y, mutableListOf(
//                        PointF(0f, .25f),
//                        PointF(1f, .25f),
//                        PointF(1f, .75f),
//                        PointF(0f, .75f)
//                ))},
//                {x, y -> generateCell(x, y, mutableListOf(
//                        PointF(.25f, 0f),
//                        PointF(.25f, 1f),
//                        PointF(.75f, 1f),
//                        PointF(.75f, 0f)
//                ))},
//                {x, y -> generateCell(x, y, mutableListOf(
//                        PointF(.25f, .25f),
//                        PointF(.75f, .25f),
//                        PointF(.75f, .75f),
//                        PointF(.25f, .75f)
//                ))},
                { x, y ->
                    generateCell(x, y, mutableListOf(
                            PointF(.25f, .5f),
                            PointF(.5f, .25f),
                            PointF(.75f, .5f),
                            PointF(.5f, .75f)
                    ))
                },
                { x, y -> spawnPoints += PointF((x + .5f) * cellSize, (y + .5f) * cellSize) }
        )

        val remixedPossibilities = possibilities.asSequence().flatMap { p ->
            val copies = random.nextInt(5)
            (0..copies).asSequence().map { p }
        }.toList()

//        val baggedPossibilities = generateSequence {
//            possibilities.asSequence().flatMap{ p ->
//                val copies = random.nextInt(5)
//                (0 .. copies).asSequence().map { p }
//            }.toMutableList().shuffle(random)
//        }.flatten()

        for (cellX in 1..cellWidth - 2) {
            for (cellY in 1..cellHeight - 2) {
                remixedPossibilities.let {
                    it[random.nextInt(it.size)]
                }.invoke(cellX, cellY)
            }
        }

        for (cellX in 0..cellWidth - 1) {
            spawnPoints += PointF(cellX * cellSize + cellSize / 2, cellSize / 2)
            spawnPoints += PointF(cellX * cellSize + cellSize / 2, (cellHeight - 1) * cellSize + cellSize / 2)
        }
        for (cellY in 1..cellHeight - 2) {
            spawnPoints += PointF(cellSize / 2, cellY * cellSize + cellSize / 2)
            spawnPoints += PointF((cellWidth - 1) * cellSize + cellSize / 2, cellY * cellSize + cellSize / 2)
        }
    }

    fun generateLevel2(seed: String, size: Int = 3, unitSize: Float = 8f) {
        val random = Random(seed.hashCode().toLong() or seed.reversed().hashCode().toLong().shl(32))

        val radius = (size * 2 + 1.5f) * unitSize / 2f

        //outer wall
        for (i in 0..5) {
            val angle = i * Math.PI * 2.0 / 6.0
            val nextAngle = (i + 1) * Math.PI * 2.0 / 6.0
            environment += Wall(PolygonF(mutableListOf(
                    PointF_polar(angle, radius),
                    PointF_polar(angle, radius + 2f),
                    PointF_polar(nextAngle, radius + 2f),
                    PointF_polar(nextAngle, radius)
            )))
        }

        fun generateCell(position: PointF) {
            environment += Wall(PolygonF(
                    (0..5)
                            .map { it * Math.PI * 2.0 / 6.0 + Math.PI / 6.0 }
                            .map { position + PointF_polar(it, unitSize / 4f) }
                            .toMutableList()
            ))
        }

        //cell center
//        generateCell(PointF(0f, 0f))

        for (i in 0..5) {
            val angle = i * Math.PI * 2.0 / 6.0
            for (j in 1..size) {
                val startPoint = PointF_polar(angle, j * unitSize)
                for (k in 0..j - 1) {
                    generateCell(startPoint + PointF_polar(angle + Math.PI * 2.0 / 3.0, k * unitSize))
                }
            }
        }

        spawnPoints += PointF(0f, 0f)
    }

    fun generateLevel3(
            seed: String,
            radius: Float = 70f,
            itemSizeMin: Float = 2f,
            itemSizeMax: Float = 8f,
            itemSpacing: Float = 2f,
            goalItems: Int = 30,
            goalSpawns: Int = 20,
            maxIterations: Int = 10000
    ) {
        val random = Random(seed.hashCode().toLong() or seed.reversed().hashCode().toLong().shl(32))

        //outer wall
        for (i in 0..5) {
            val angle = i * Math.PI * 2.0 / 6.0
            val nextAngle = (i + 1) * Math.PI * 2.0 / 6.0
            environment += Wall(PolygonF(mutableListOf(
                    PointF_polar(angle, radius),
                    PointF_polar(angle, radius + 2f),
                    PointF_polar(nextAngle, radius + 2f),
                    PointF_polar(nextAngle, radius)
            )))
        }

        val innerRadius = radius * .75f

        fun generateItem(position: PointF): PolygonF {
            val count = random.nextInt(10) + 5
            val distances = (0..count).map {
                random.nextFloat() * (itemSizeMax - itemSizeMin) + itemSizeMin
            }
            val offset = random.nextInt(count)
            val rotation = random.nextDouble() * Math.PI * 2.0 / count
            return PolygonF(
                    (0..count).map {
                        position + PointF_polar(
                                rotation + it * Math.PI * 2.0 / count,
                                distances[(it + offset) % count]
                        )
                    }.toMutableList()
            )
        }

        fun okPosition(polygonF: PolygonF): Boolean {
            val calc = PolygonPolygonResult()
            val newBounds = RectF()
            polygonF.getBounds(newBounds)
            newBounds.left -= itemSpacing
            newBounds.right += itemSpacing
            newBounds.top -= itemSpacing
            newBounds.bottom += itemSpacing
            val existingBounds = RectF()
            return environment.none {
                it.polygon.getBounds(existingBounds)
                if (existingBounds intersects newBounds) {
                    calc.first = polygonF
                    calc.second = it.polygon
                    calc.calculate()
//                println(calc.best.bestDistanceSquared)
                    calc.best.bestDistanceSquared < itemSpacing.sqr()
                } else false
            }
        }

        fun okPosition(point: PointF): Boolean {
            val calc = PointPolygonResult()
            val newBounds = RectF(point.x, point.y, point.x, point.y)
            newBounds.left -= itemSpacing
            newBounds.right += itemSpacing
            newBounds.top -= itemSpacing
            newBounds.bottom += itemSpacing
            val existingBounds = RectF()
            calc.point = point
            return environment.none {
                it.polygon.getBounds(existingBounds)
                if (existingBounds intersects newBounds) {
                    calc.polygon = it.polygon
                    calc.calculate()
//                println(calc.best.bestDistanceSquared)
                    calc.bestDistanceSquared < itemSpacing.sqr()
                } else false
            }
        }

        //cell center
//        generateCell(PointF(0f, 0f))

        var successes = 0
        for (i in 0..maxIterations) {
            val new = generateItem(PointF_polar(
                    angle = random.nextDouble() * Math.PI * 2,
                    length = Math.sqrt(random.nextDouble()).toFloat() * innerRadius
            ))
            if (okPosition(new)) {
                environment += Wall(new)
                successes++
                if (successes > goalItems) {
                    break
                }
            }
        }

        successes = 0
        for (i in 0..maxIterations) {
            val pos = PointF_polar(
                    angle = random.nextDouble() * Math.PI * 2,
                    length = Math.sqrt(random.nextDouble()).toFloat() * innerRadius
            )
            if (okPosition(pos)) {
                spawnPoints += pos
                successes++
                if (successes > goalSpawns) {
                    break
                }
            }
        }
    }
}
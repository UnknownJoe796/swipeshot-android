package com.ivieleague.swipeshot

import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.net.wifi.WifiManager
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.View
import com.ivieleague.swipeshot.game.GameWorld
import com.ivieleague.swipeshot.game.NetInterface
import com.ivieleague.swipeshot.game.Player
import com.ivieleague.swipeshot.game.UDPBroadcastNetInterface
import com.ivieleague.swipeshot.math.length
import com.ivieleague.swipeshot.math.minus
import com.lightningkite.kotlin.anko.lifecycle
import com.lightningkite.kotlin.anko.viewcontrollers.AnkoViewController
import com.lightningkite.kotlin.anko.viewcontrollers.implementations.VCActivity
import com.lightningkite.kotlin.lifecycle.LifecycleListener
import com.lightningkite.kotlin.lifecycle.listen
import org.jetbrains.anko.*
import java.util.*

/**
 * Created by josep on 2/15/2017.
 */
class GameVC(
        val networking: NetInterface<Player?> = UDPBroadcastNetInterface<Player>(),
        val game: GameWorld = GameWorld(
                player = Player("Player"),
                networking = networking
        )
) : AnkoViewController() {

    val surfaceHolders = ArrayList<SurfaceHolder>()

    override fun createView(ui: AnkoContext<VCActivity>): View = ui.frameLayout {
        lifecycle.connect(object : LifecycleListener {
            var lock: WifiManager.MulticastLock? = null

            override fun onStart() {
                lock = context.wifiManager.createMulticastLock("multicastLock").apply {
                    setReferenceCounted(true)
                    this.acquire()
                }
            }

            override fun onStop() {
                lock?.release()
                lock = null
            }
        })
        surfaceView {
            holder.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceCreated(holder: SurfaceHolder) {
                    surfaceHolders.add(holder)
                    startLoopIfNotRunning()
                }

                override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

                }

                override fun surfaceDestroyed(holder: SurfaceHolder) {
                    surfaceHolders.remove(holder)
                    stopLoopIfNoHolders()
                }
            })

            setupTouch(this)
        }.lparams(matchParent, matchParent)
    }

    class KeepRunningThread(var holder: Data = Data(), action: Data.() -> Unit) : Thread({ action(holder) }) {
        var keepRunning: Boolean
            get() = holder.keepRunning
            set(value) {
                holder.keepRunning = value
            }

        class Data() {
            var keepRunning = true
        }
    }

    var thread: KeepRunningThread? = null
    var frameNanoseconds = 16666666
    fun startLoopIfNotRunning() {
        if (thread == null) {
            networking.connect()
            thread = KeepRunningThread {
                while (keepRunning) {
                    val startTime = System.nanoTime()
                    game.step((frameNanoseconds / 1000000000.0).toFloat())

                    for (holder in surfaceHolders) {
                        val canvas = holder.lockCanvas() ?: continue
                        game.render(canvas)
                        holder.unlockCanvasAndPost(canvas)
                    }

                    val timeToWait = frameNanoseconds - (System.nanoTime() - startTime)
                    if (timeToWait > 0) {
                        try {
                            Thread.sleep(timeToWait / 1000000, (timeToWait % 1000000).toInt())
                        } catch(e: InterruptedException) {/*Squish*/
                        }
                    }
                }
                networking.disconnect()
            }.apply { start() }
        }
    }

    fun stopLoopIfNoHolders() {
        if (surfaceHolders.isEmpty()) {
            thread?.keepRunning = false
            thread = null
        }
    }

    fun setupTouch(view: View) {
        val positions = object {
            var moveTouchId: Int? = null
            var moveStartPoint = PointF()
            var moveCurrentPoint = PointF()
            var shootTouchId: Int? = null
            var shootStartPoint = PointF()
            var shootCurrentPoint = PointF()
        }
        val paint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }

        view.lifecycle.listen(game.controlOverlayListeners) { canvas ->
            synchronized(positions) {
                if (positions.moveTouchId != null) {
                    canvas.drawLine(positions.moveStartPoint, positions.moveCurrentPoint, paint)
                }
                if (positions.shootTouchId != null) {
                    canvas.drawLine(positions.shootStartPoint, positions.shootCurrentPoint, paint)
                }
            }
        }

        view.lifecycle.listen(game.stepListeners) {
            if (positions.moveTouchId != null) {
                val delta = positions.moveCurrentPoint - positions.moveStartPoint
                if (delta.length > 20f)
                    game.player?.velocity?.set(delta)
            }
        }

        view.setOnTouchListener { view, motionEvent ->
            synchronized(positions) {
                val touchId = motionEvent.getPointerId(motionEvent.actionIndex)
                val action = motionEvent.actionMasked
                val x = motionEvent.getX(motionEvent.actionIndex)
                val y = motionEvent.getY(motionEvent.actionIndex)
                when (action) {
                    MotionEvent.ACTION_DOWN,
                    MotionEvent.ACTION_POINTER_DOWN -> {
                        if (x < view.width / 2) {
                            //move
                            if (positions.moveTouchId == null) {
                                positions.moveTouchId = touchId
                                positions.moveStartPoint.x = x
                                positions.moveStartPoint.y = y
                                positions.moveCurrentPoint.x = x
                                positions.moveCurrentPoint.y = y
                            }
                        } else {
                            //shoot
                            if (positions.shootTouchId == null) {
                                positions.shootTouchId = touchId
                                positions.shootStartPoint.x = x
                                positions.shootStartPoint.y = y
                                positions.shootCurrentPoint.x = x
                                positions.shootCurrentPoint.y = y
                            }
                        }
                        Unit
                    }
                    MotionEvent.ACTION_MOVE -> {
                        for (i in 0..motionEvent.pointerCount - 1) {
                            val subTouchId = motionEvent.getPointerId(i)
                            val subX = motionEvent.getX(i)
                            val subY = motionEvent.getY(i)
                            when (subTouchId) {
                                positions.moveTouchId -> {
                                    positions.moveCurrentPoint.x = subX
                                    positions.moveCurrentPoint.y = subY
                                }
                                positions.shootTouchId -> {
                                    positions.shootCurrentPoint.x = subX
                                    positions.shootCurrentPoint.y = subY
                                }
                            }
                        }
                        Unit
                    }
                    MotionEvent.ACTION_UP,
                    MotionEvent.ACTION_POINTER_UP,
                    MotionEvent.ACTION_CANCEL -> {
                        when (touchId) {
                            positions.moveTouchId -> {
                                positions.moveTouchId = null

                                //stop
                                game.stepQueue += {
                                    game.player?.velocity?.set(0f, 0f)
                                }
                            }
                            positions.shootTouchId -> {
                                positions.shootTouchId = null

                                //shoot
                                game.stepQueue += {
                                    val delta = positions.shootCurrentPoint - positions.shootStartPoint
                                    if (delta.length > 20f)
                                        game.player?.shoot(delta)
                                }
                            }
                        }
                        Unit
                    }
                }
            }
            true
        }
    }
}
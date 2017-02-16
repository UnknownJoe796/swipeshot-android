package com.ivieleague.swipeshot

import android.view.SurfaceHolder
import android.view.View
import com.ivieleague.swipeshot.game.GameModel
import com.lightningkite.kotlin.anko.viewcontrollers.AnkoViewController
import com.lightningkite.kotlin.anko.viewcontrollers.implementations.VCActivity
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.frameLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.surfaceView
import java.util.*

/**
 * Created by josep on 2/15/2017.
 */
class GameVC : AnkoViewController() {

    val game = GameModel()
    val surfaceHolders = ArrayList<SurfaceHolder>()

    override fun createView(ui: AnkoContext<VCActivity>): View = ui.frameLayout {
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
            thread = KeepRunningThread {
                while (keepRunning) {
                    val startTime = System.nanoTime()
                    game.step((frameNanoseconds / 1000000000.0).toFloat())

                    for (holder in surfaceHolders) {
                        val canvas = holder.lockCanvas()
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
            }.apply { start() }
        }
    }

    fun stopLoopIfNoHolders() {
        if (surfaceHolders.isEmpty()) {
            thread?.keepRunning = false
            thread = null
        }
    }
}
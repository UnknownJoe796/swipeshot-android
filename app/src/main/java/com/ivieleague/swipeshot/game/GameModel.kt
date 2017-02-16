package com.ivieleague.swipeshot.game

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.lightningkite.kotlin.aside

class GameModel : DynamicGameComponent() {

    val world = GameWorld().aside { add(it) }

    init {
        //test circle
        world.add(object : GameComponent {
            var time:Float = 0f

            override fun step(timePassed: Float) {
                time += timePassed
            }

            override fun render(canvas: Canvas) {
                canvas.drawColor(Color.WHITE)
                canvas.drawCircle(0f, 0f, Math.sin(time.toDouble()).toFloat() * 2f + 3f, GameWorld.commonPaint)
            }
        })
    }
}


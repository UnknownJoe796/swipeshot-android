package com.ivieleague.swipeshot.game

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import java.net.InetAddress
import java.util.*

/**
 * Created by josep on 2/15/2017.
 */
class Player(val id: InetAddress):GameComponent{

    companion object{
        val radius = 1f
        val cooldownMax = 1f
        val shotSpeed = 10f
    }

    val position = PointF()
    val velocity = PointF()

    var cooldown = cooldownMax

    val bullets = ArrayList<Bullet>()

    fun shoot(){

    }

    override fun step(timePassed: Float) {
        position.x += velocity.x * timePassed
        position.y += velocity.y * timePassed
        cooldown = (cooldown - timePassed).coerceAtMost(0f)
    }
    override fun render(canvas: Canvas) {
        canvas.drawCircle(position.x, position.y, radius, GameWorld.commonPaint)
    }

    class Bullet: GameComponent{
        companion object{
            val radius = .5f
        }

        val position = PointF()
        val velocity = PointF()

        override fun step(timePassed: Float){
            position.x += velocity.x * timePassed
            position.y += velocity.y * timePassed
        }

        override fun render(canvas: Canvas) {
            canvas.drawCircle(position.x, position.y, radius, GameWorld.commonPaint)
        }
    }
}
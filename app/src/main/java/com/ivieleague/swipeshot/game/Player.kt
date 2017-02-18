package com.ivieleague.swipeshot.game

import android.graphics.Canvas
import android.graphics.PointF
import com.ivieleague.swipeshot.math.copy
import com.ivieleague.swipeshot.math.length
import com.ivieleague.swipeshot.math.plusAssign
import com.ivieleague.swipeshot.math.times
import java.util.*

/**
 * Created by josep on 2/15/2017.
 */
class Player {

    companion object{
        val radius = 1f
        val cooldownMax = 1f
        val shotSpeed = 10f
        val travelSpeed = 4f
    }

    val position = PointF()
    val velocity = PointF()
    var cooldown = cooldownMax
    val bullets = ArrayList<Bullet>()

    fun shoot(vector: PointF) {
        if (cooldown > 0f) return
        bullets += Bullet(
                position = position.copy(),
                velocity = vector.copy().apply { length = shotSpeed }
        )
        cooldown = cooldownMax
    }

    fun step(world: GameWorld, timePassed: Float) {
        if (velocity.length > travelSpeed) {
            velocity.length = travelSpeed
        }
        position += velocity * timePassed
        cooldown -= timePassed
        if (cooldown < 0f) cooldown = 0f

        bullets.removeAll {
            it.step(world, this, timePassed)
            it.life <= 0f
        }
    }

    fun render(canvas: Canvas) {
        for (bullet in bullets) {
            bullet.render(canvas)
        }
        canvas.drawCircle(position.x, position.y, radius, GameWorld.commonPaint)
        canvas.drawCircle(position.x, position.y, radius * cooldown / cooldownMax, GameWorld.commonPaint)
    }

    class Bullet(
            val position: PointF = PointF(),
            val velocity: PointF = PointF(),
            var life: Float = lifeMax
    ) {
        companion object{
            val lifeMax = 2f
            val radius = .5f
            val animationStartRadius = 0f
            val animationTime = .25f
        }

        fun step(world: GameWorld, player: Player, timePassed: Float) {
            position += velocity * timePassed
            life -= timePassed
        }

        fun render(canvas: Canvas) {
            val animationRatio = (lifeMax - life).coerceAtMost(animationTime) / animationTime
            val size = radius * animationRatio + animationStartRadius * (1 - animationRatio)
            canvas.drawCircle(position.x, position.y, size, GameWorld.commonPaint)
        }
    }
}
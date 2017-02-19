package com.ivieleague.swipeshot.game

import android.graphics.Canvas
import android.graphics.PointF
import android.graphics.RectF
import com.ivieleague.swipeshot.math.*
import java.util.*

/**
 * Created by josep on 2/15/2017.
 */
class Player(var name: String = "") {

    companion object {
        const val radius = 1f
        const val cooldownMax = 1f
        const val shotSpeed = 15f
        const val travelSpeed = 6f
        const val startupInvincibilityMax = 2f


        //These are shared between instances.  If multithreading is ever done for processing,
        //that will need to change.
        val bounds = RectF()
        val ejector = PointPolygonResult()
    }

    val position = PointF()
    val velocity = PointF()
    var cooldown = cooldownMax
    val bullets = ArrayList<Bullet>()
    var startupInvincibility = startupInvincibilityMax

    var lastMessage = System.currentTimeMillis()
    var id: String = UUID.randomUUID().toString()

    fun shoot(vector: PointF) {
        if (cooldown > 0f) return
        bullets += Bullet(
                position = position.copy(),
                velocity = vector.copy().apply { length = shotSpeed }
        )
        cooldown = cooldownMax
    }

    fun step(world: GameWorld, timePassed: Float) {
        //Limit velocity
        if (velocity.length > travelSpeed) {
            velocity.length = travelSpeed
        }

        //Handle the timers
        cooldown -= timePassed
        if (cooldown < 0f) cooldown = 0f
        startupInvincibility -= timePassed
        if (startupInvincibility < 0f) startupInvincibility = 0f

        //Advance the player
        position += velocity * timePassed

        //Set up bounds for collision detection
        bounds.left = position.x - radius
        bounds.right = position.x + radius
        bounds.top = position.y - radius
        bounds.bottom = position.y + radius

        //Eject from walls
        for (wall in world.environment) {

            //Do the quick check
            if (bounds intersects wall.bounds) {

                //Do the detailed check
                val ejector = Player.ejector
                ejector.polygon = wall.polygon
                ejector.point = this.position
                ejector.calculate()

                val dist = ejector.best.boundedDistance - radius
                if (dist < 0f) {
                    //Eject
                    val amount = ejector.best.normal
                    amount.length -= radius
                    position -= amount
                }
            }
        }

        //Take hits from players
        if (startupInvincibility < .0001f) {
            for ((id, player) in world.players) {
                if (player != this) {
                    for (bullet in player.bullets) {
                        val dist = position distance bullet.position
                        if (dist < radius + Bullet.radius) {
                            die(world)
                        }
                    }
                }
            }
        }

        //Advance the bullets
        bullets.removeAll {
            it.step(world, this, timePassed)
            it.life <= 0f
        }
    }

    fun die(world: GameWorld) {
        position.set(world.selectSpawnPoint())
        startupInvincibility = startupInvincibilityMax
        cooldown = cooldownMax
    }

    fun render(canvas: Canvas) {
        for (bullet in bullets) {
            bullet.render(canvas)
        }
        canvas.drawCircle(position.x, position.y, radius, GameWorld.commonPaint)
        canvas.drawCircle(position.x, position.y, radius * cooldown / cooldownMax, GameWorld.commonPaint)
        if (startupInvincibility > .0001f) {
            GameWorld.commonPaint.alpha = (startupInvincibility / startupInvincibilityMax).times(256f).toInt().coerceIn(0, 255)
            canvas.drawCircle(position.x, position.y, radius + .2f, GameWorld.commonPaint)
            GameWorld.commonPaint.alpha = 255
        }

        canvas.save()
        canvas.translate(position.x, position.y + radius + .7f)
        canvas.scale(.01f, .01f)
        canvas.drawText(name, 0f, 0f, GameWorld.textPaint)
        canvas.restore()
    }

    class Bullet(
            val position: PointF = PointF(),
            val velocity: PointF = PointF(),
            var life: Float = lifeMax
    ) {
        @Transient val bounds = RectF()

        companion object {
            val lifeMax = 2f
            val radius = .5f
            val animationStartRadius = 0f
            val animationEndRadius = 0f
            val animationTime = .25f
        }

        fun step(world: GameWorld, player: Player, timePassed: Float) {
            //Step the bullet forwards
            position += velocity * timePassed

            //Age the bullet
            life -= timePassed

            //Set up bounds for collision detection
            bounds.left = position.x - Player.radius
            bounds.right = position.x + Player.radius
            bounds.top = position.y - Player.radius
            bounds.bottom = position.y + Player.radius

            //Collide with walls
            if (life > animationTime) {
                for (wall in world.environment) {
                    if (bounds intersects wall.bounds) {
                        val ejector = Player.ejector
                        ejector.point = position
                        ejector.polygon = wall.polygon
                        ejector.calculate()
                        val dist = ejector.best.boundedDistance - radius
                        if (dist < 0f) {
                            life = animationTime
                            velocity.projectAssign(ejector.best.line.delta().apply { length = 1f })
                        }
                    }
                }
            }
        }

        fun render(canvas: Canvas) {
            if (lifeMax - life < animationTime) {
                val animationRatio = (lifeMax - life).coerceAtMost(animationTime) / animationTime
                val size = radius * animationRatio + animationStartRadius * (1 - animationRatio)
                canvas.drawCircle(position.x, position.y, size, GameWorld.commonPaint)
            } else if (life < animationTime) {
                val animationRatio = (life).coerceAtMost(animationTime) / animationTime
                val size = radius * animationRatio + animationEndRadius * (1 - animationRatio)
                canvas.drawCircle(position.x, position.y, size, GameWorld.commonPaint)
            } else {
                canvas.drawCircle(position.x, position.y, radius, GameWorld.commonPaint)
            }
        }
    }
}
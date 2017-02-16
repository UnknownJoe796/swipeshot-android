package com.ivieleague.swipeshot.game

import android.graphics.Canvas

abstract class NestedGameComponent : GameComponent {
    abstract val children:Iterable<GameComponent>

    override fun step(timePassed: Float) = children.forEach { it.step(timePassed) }
    override fun render(canvas: Canvas) = children.forEach { it.render(canvas) }
}
package com.ivieleague.swipeshot.game.structure

import android.graphics.Canvas

abstract class NestedGameComponent<DEPENDENCY : Any, SUBDEPENDENCY> : GameComponentImpl<DEPENDENCY>() {
    abstract val subdependency: SUBDEPENDENCY
    abstract val children: Iterable<GameComponent<SUBDEPENDENCY>>

    override fun step(timePassed: Float) = children.forEach { it.step(timePassed) }
    override fun render(canvas: Canvas) = children.forEach { it.render(canvas) }
}
package com.ivieleague.swipeshot.game.structure

import android.graphics.Canvas

/**
 * Created by josep on 2/15/2017.
 */

interface GameComponent<DEPENDENCY> : Comparable<GameComponent<*>> {
    val dependency: DEPENDENCY
    fun init(dependency: DEPENDENCY)

    val depth: Int get() = 0
    fun step(timePassed: Float)
    fun render(canvas: Canvas)
    override operator fun compareTo(other: GameComponent<*>): Int = depth - other.depth
}

abstract class GameComponentImpl<DEPENDENCY : Any> : GameComponent<DEPENDENCY> {
    override lateinit var dependency: DEPENDENCY

    override fun init(dependency: DEPENDENCY) {
        this.dependency = dependency
    }
}
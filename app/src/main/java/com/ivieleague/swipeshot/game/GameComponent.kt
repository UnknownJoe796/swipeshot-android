package com.ivieleague.swipeshot.game

import android.graphics.Canvas

/**
 * Created by josep on 2/15/2017.
 */

interface GameComponent : Comparable<GameComponent>{
    val depth: Int get() = 0
    fun step(timePassed: Float)
    fun render(canvas: Canvas)
    override operator fun compareTo(other: GameComponent): Int = depth - other.depth
}
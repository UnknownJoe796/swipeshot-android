package com.ivieleague.swipeshot.game

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF

/**
 * Created by josep on 2/15/2017.
 */
class Wall(val points:List<PointF>):GameComponent{
    override fun step(timePassed: Float) {}

    val pointsArray = FloatArray(points.size * 2 + 2).apply{
        for(i in points.indices){
            this[i * 2] = points[i].x
            this[i * 2 + 1] = points[i].y
        }
        this[points.size * 2] = points[0].x
        this[points.size * 2 + 1] = points[0].y
    }
    override fun render(canvas: Canvas) {
        canvas.drawLines(pointsArray, GameWorld.commonPaint)
    }
}
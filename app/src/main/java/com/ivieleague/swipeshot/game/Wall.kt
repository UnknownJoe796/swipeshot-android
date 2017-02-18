package com.ivieleague.swipeshot.game

import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import com.ivieleague.swipeshot.math.PolygonF

/**
 * Created by josep on 2/15/2017.
 */
class Wall(val polygon: PolygonF = PolygonF()) {

    init {
        polygon.normalize()
    }

    @Transient val bounds = run {
        var xMin = Float.MIN_VALUE
        var xMax = Float.MAX_VALUE
        var yMin = Float.MIN_VALUE
        var yMax = Float.MAX_VALUE
        for (point in polygon.list) {
            if (point.x < xMin) xMin = point.x
            if (point.y < yMin) yMin = point.y
            if (point.x > xMax) xMax = point.x
            if (point.y > yMax) yMax = point.y
        }
        RectF(xMin, yMin, xMax, yMax)
    }

    val path = Path().apply {
        this.moveTo(polygon[0].x, polygon[0].y)
        for (i in 1..polygon.lastIndex) {
            this.lineTo(polygon[i].x, polygon[i].y)
        }
        this.lineTo(polygon[0].x, polygon[0].y)
    }

    fun render(canvas: Canvas) {
        canvas.drawPath(path, GameWorld.commonPaint)
//        polygon.lineSequence().forEach {
//            canvas.drawLine(it.first, it.second, GameWorld.commonPaint)
//        }
    }
}
package com.ivieleague.swipeshot.game

import android.graphics.Canvas
import android.graphics.Path
import com.ivieleague.swipeshot.math.PolygonF

/**
 * Created by josep on 2/15/2017.
 */
class Wall(val polygon: PolygonF = PolygonF()) {

    val path = Path().apply {
        this.moveTo(polygon[0].x, polygon[0].y)
        for (i in 1..polygon.lastIndex) {
            this.lineTo(polygon[i].x, polygon[i].y)
        }
        this.lineTo(polygon[0].x, polygon[0].y)
    }

    fun render(canvas: Canvas) {
        canvas.drawPath(path, GameWorld.commonPaint)
    }
}
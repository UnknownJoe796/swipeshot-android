package com.ivieleague.swipeshot.game

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF

class GameWorld : DynamicGameComponent(){

    companion object{
        val commonPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = .1f
        }
    }

    val cameraWorldUnitsToShow = 20f
    val cameraPosition = PointF(0f, 0f)

    override fun step(timePassed: Float) {
        super.step(timePassed)
    }

    override fun render(canvas: Canvas) {
        canvas.save()
        canvas.translate(canvas.width/2f, canvas.height/2f)
        val minSide = canvas.width.coerceAtMost(canvas.height)
        val scale = cameraWorldUnitsToShow / minSide
        canvas.scale(1/scale, 1/scale)
        super.render(canvas)
        canvas.restore()
    }
}
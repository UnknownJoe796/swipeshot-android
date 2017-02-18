package com.ivieleague.swipeshot

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF

/**
 * Created by josep on 2/17/2017.
 */
fun Canvas.drawLine(start: PointF, end: PointF, paint: Paint) = drawLine(start.x, start.y, end.x, end.y, paint)
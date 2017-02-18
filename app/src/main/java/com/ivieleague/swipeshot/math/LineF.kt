package com.ivieleague.swipeshot.math

import android.graphics.PointF

/**
 * Created by josep on 2/16/2017.
 */
data class LineF(var first: PointF = PointF(), var second: PointF = PointF())

inline fun LineF.interpolate(amount: Float, existing: PointF = PointF()): PointF {
    existing.x = first.x + amount * (second.x - first.x)
    existing.y = first.y + amount * (second.y - first.y)
    return existing
}

data class PointLineResult(
        var point: PointF = PointF(),
        var line: LineF = LineF()
) {
    var ratio: Float = 0f

    fun calculate() {
        val lineLengthSqr = (line.second.x - line.first.x).sqr() + (line.second.y - line.first.y).sqr()
        if (lineLengthSqr == 0f) {
            ratio = .5f
            return
        }
        ratio = (((point.x - line.first.x) * (line.second.x - line.first.x) + (point.y - line.first.y) * (line.second.y - line.first.y)) / lineLengthSqr)
    }

    val distanceSquared: Float
        get() = (point.x - ((line.first.x + ratio * (line.second.x - line.first.x)))).sqr() +
                (point.x - ((line.first.y + ratio * (line.second.y - line.first.y)))).sqr()

    val distance: Float
        get() = Math.sqrt(distanceSquared.toDouble()).toFloat()

    val boundedDistanceSquared: Float
        get() = (point.x - ((line.first.x + ratio.coerceIn(0f, 1f) * (line.second.x - line.first.x)))).sqr() +
                (point.x - ((line.first.y + ratio.coerceIn(0f, 1f) * (line.second.y - line.first.y)))).sqr()

    val boundedDistance: Float
        get() = Math.sqrt(boundedDistanceSquared.toDouble()).toFloat()
}

data class LineLineResult(
        var first: LineF = LineF(),
        var second: LineF = LineF()
) {
    var ratioFirst: Float = 0f
    var ratioSecond: Float = 0f

    fun calculate() {
        val denom = (second.second.y - second.first.y) * (first.second.x - first.first.x) - (second.second.x - second.first.x) * (first.second.y - first.first.y)
        if (denom == 0f) { // Lines are parallel.
            ratioFirst = Float.NaN
            ratioSecond = Float.NaN
            return
        }
        ratioFirst = ((second.second.x - second.first.x) * (first.first.y - second.first.y) - (second.second.y - second.first.y) * (first.first.x - second.first.x)) / denom
        ratioSecond = ((first.second.x - first.first.x) * (first.first.y - second.first.y) - (first.second.y - first.first.y) * (first.first.x - second.first.x)) / denom
    }

    val segmentsIntersect: Boolean get() = ratioFirst >= 0f && ratioFirst <= 1f && ratioSecond >= 0f && ratioSecond <= 1f

    fun point(existing: PointF = PointF()): PointF? = if (ratioFirst != Float.NaN) first.interpolate(ratioFirst) else null
    val point: PointF? get() = point()
}
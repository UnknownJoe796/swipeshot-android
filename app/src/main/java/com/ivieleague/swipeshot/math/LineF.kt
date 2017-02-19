package com.ivieleague.swipeshot.math

import android.graphics.PointF

/**
 * Created by josep on 2/16/2017.
 */
data class LineF(var first: PointF = PointF(), var second: PointF = PointF())

fun LineF.delta(): PointF = PointF(second.x - first.x, second.y - first.y)

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

    val clockwise: Boolean
        get() {
            val total = (line.second.x - line.first.x) * (line.second.y + line.first.y) +
                    (point.x - line.second.x) * (point.y + line.second.y) +
                    (line.first.x - point.x) * (line.first.y + point.y)
            return total > 0f
        }

    val distanceSquared: Float
        get() = (point.x - ((line.first.x + ratio * (line.second.x - line.first.x)))).sqr() +
                (point.y - ((line.first.y + ratio * (line.second.y - line.first.y)))).sqr()

    val distance: Float
        get() = Math.sqrt(distanceSquared.toDouble()).toFloat()

    val signedDistance: Float
        get() = if (clockwise) distance else -distance

    val boundedDistanceSquared: Float
        get() = (point.x - ((line.first.x + ratio.coerceIn(0f, 1f) * (line.second.x - line.first.x)))).sqr() +
                (point.y - ((line.first.y + ratio.coerceIn(0f, 1f) * (line.second.y - line.first.y)))).sqr()

    val boundedDistance: Float
        get() = Math.sqrt(boundedDistanceSquared.toDouble()).toFloat()

    val signedBoundedDistance: Float
        get() = if (clockwise) boundedDistance else -boundedDistance

    val normal: PointF
        get() = if (ratio < 0f) {
            point - line.first
        } else if (ratio > 1f) {
            point - line.second
        } else {
            (line.second - line.first).apply {
                perpendicularAssign()
                timesAssign(-1f)
                length = signedDistance
            }
        }

    val linePoint: PointF get() = line.interpolate(ratio)
    val boundedLinePoint: PointF get() = line.interpolate(ratio.coerceIn(0f, 1f))
}

infix fun LineF.intersects(other: LineF): Boolean {
    val denom = (other.second.y - other.first.y) * (second.x - first.x) - (other.second.x - other.first.x) * (second.y - first.y)
    if (denom == 0f) { // Lines are parallel.
        return false
    }
    val ratioFirst = ((other.second.x - other.first.x) * (first.y - other.first.y) - (other.second.y - other.first.y) * (first.x - other.first.x)) / denom
    val ratioSecond = ((second.x - first.x) * (first.y - other.first.y) - (second.y - first.y) * (first.x - other.first.x)) / denom
    return ratioFirst >= 0f && ratioFirst <= 1f && ratioSecond >= 0f && ratioSecond <= 1f
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
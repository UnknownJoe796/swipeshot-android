package com.ivieleague.swipeshot.math

import android.graphics.PointF
import java.util.*

/**
 * Created by josep on 2/16/2017.
 */
class PolygonF(val list: MutableList<PointF> = ArrayList()) : MutableList<PointF> by list {
    fun getLine(index: Int, existing: LineF = LineF()): LineF {
        existing.first = list[index]
        existing.second = list[index.plus(1).mod(list.size)]
        return existing
    }

    fun lineSequence(): Sequence<LineF> = list.indices.asSequence().map { getLine(it) }
    fun lineSequence(existing: LineF): Sequence<LineF> = list.indices.asSequence().map { getLine(it, existing) }

    companion object {
        val calcPoint = PointF()
    }

    fun getLineNormal(index: Int): Float {
        calcPoint.x = list[index.plus(1).mod(list.size)].x - list[index].x
        calcPoint.y = list[index.plus(1).mod(list.size)].y - list[index].y
        return calcPoint.angle + (Math.PI / 2).toFloat()
    }

    private val underlyingArea: Double
        get() = lineSequence().sumByDouble { (it.second.x - it.first.x) * (it.second.y + it.first.y).toDouble() }
    val area: Float get() = Math.abs(underlyingArea).toFloat()
    val clockwise: Boolean get() = underlyingArea > 0.0
    val centroid: PointF by lazy {
        val first = list.first()
        var xAverage = 0.0
        var yAverage = 0.0
        var areaTotal = 0.0
        for (i in 1..list.size - 2) {
            val second = list[i]
            val third = list[i + 1]
            val x = (first.x + second.x + third.x) / 3
            val y = (first.y + second.y + third.y) / 3
            val area = Math.abs(
                    (first.x - third.x) * (second.y - first.y) -
                            (first.x - second.x) * (third.y - first.y)
            ) * .5
            xAverage += x * area
            yAverage += y * area
            areaTotal += area
        }
        xAverage /= areaTotal
        yAverage /= areaTotal
        PointF(xAverage.toFloat(), yAverage.toFloat())
    }

    fun normalize() {
        if (clockwise) list.reverse()
    }
}

data class PointPolygonResult(
        var point: PointF = PointF(),
        var polygon: PolygonF = PolygonF(),
        var result: PointLineResult = PointLineResult(point),
        var best: PointLineResult = PointLineResult(point),
        var bestDistanceSquared: Float = 0f,
        var bestIndex: Int = 0
) {
    fun calculate() {
        bestDistanceSquared = Float.MAX_VALUE
        result.point = point
        polygon.lineSequence(/*result.line*/).forEachIndexed { index, it ->
            val result = PointLineResult(point, it)
            result.line = it
            result.calculate()
            val distSqr = result.boundedDistance
            if (distSqr < bestDistanceSquared) {
                bestIndex = index
                bestDistanceSquared = distSqr
                best = result
//                best.point = result.point
//                best.line.first = result.line.first
//                best.line.second = result.line.second
            }
        }
    }
}

data class LinePolygonResult(
        var line: LineF = LineF(),
        var polygon: PolygonF = PolygonF(),
        var intersections: ArrayList<Intersection> = ArrayList(),
        var polyLine: LineF = LineF()
) {
    inner class Intersection(
            val intersection: LineLineResult,
            val index: Int
    )

    fun calculate() {
        intersections.clear()
        val lineLine = LineLineResult(line, polyLine)
        polygon.lineSequence(polyLine).forEachIndexed { index, it ->
            lineLine.calculate()
            if (lineLine.segmentsIntersect) {
                intersections.add(Intersection(
                        lineLine.copy().apply {
                            first = first.copy()
                            second = second.copy()
                        },
                        index
                ))
            }
        }
    }
}
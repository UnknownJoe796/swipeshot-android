package com.ivieleague.swipeshot.math

import android.graphics.PointF

/**
 *
 * Created by josep on 2/16/2017.
 */

fun PointF_polar(angle: Double, length: Float = 1f): PointF
        = PointF(Math.cos(angle).toFloat() * length, Math.sin(angle).toFloat() * length)

val PointF.lengthSquared: Float
    get() = x.sqr() + y.sqr()
var PointF.length: Float
    get() = Math.sqrt(x.sqr() + y.sqr().toDouble()).toFloat()
    set(value) {
        this *= value / length()
    }

var PointF.angle: Float
    get() = Math.atan2(y.toDouble(), x.toDouble()).toFloat()
    set(value) {
        val len = length
        x = Math.cos(value.toDouble()).toFloat() * len
        y = Math.sin(value.toDouble()).toFloat() * len
    }

inline fun PointF.copy() = PointF(x, y)

inline operator fun PointF.plus(other: PointF): PointF = PointF(x + other.x, y + other.y)
inline operator fun PointF.plusAssign(other: PointF) {
    x += other.x
    y += other.y
}

inline operator fun PointF.minus(other: PointF): PointF = PointF(x - other.x, y - other.y)
inline operator fun PointF.minusAssign(other: PointF) {
    x -= other.x
    y -= other.y
}

inline operator fun PointF.times(other: PointF): PointF = PointF(x * other.x, y * other.y)
inline operator fun PointF.timesAssign(other: PointF) {
    x *= other.x
    y *= other.y
}

inline operator fun PointF.div(other: PointF): PointF = PointF(x / other.x, y / other.y)
inline operator fun PointF.divAssign(other: PointF) {
    x /= other.x
    y /= other.y
}

inline operator fun PointF.times(scalar: Float): PointF = PointF(x * scalar, y * scalar)
inline operator fun PointF.timesAssign(scalar: Float) {
    x *= scalar
    y *= scalar
}

inline operator fun PointF.div(scalar: Float): PointF = PointF(x / scalar, y / scalar)
inline operator fun PointF.divAssign(scalar: Float) {
    x /= scalar
    y /= scalar
}

inline infix fun PointF.dot(other: PointF): Float = x * other.x + y * other.y
inline infix fun PointF.cross(other: PointF): Float = x * other.y - y * other.x
inline infix fun PointF.project(other: PointF): PointF {
    val len = this dot other
    return PointF(other.x * len, other.y * len)
}

inline infix fun PointF.projectAssign(other: PointF) {
    val len = this dot other
    x = other.x * len
    y = other.y * len
}

inline fun PointF.perpendicular() = PointF(-y, x)
inline fun PointF.perpendicularAssign() {
    val temp = x
    x = -y
    y = temp
}

inline fun Float.sqr(): Float = this * this
inline infix fun PointF.distance(other: PointF): Float = Math.sqrt(((other.x - x).sqr() + (other.y - y).sqr()).toDouble()).toFloat()
inline infix fun PointF.distanceSquared(other: PointF): Float = (other.x - x).sqr() + (other.y - y).sqr()
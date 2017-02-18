package com.ivieleague.swipeshot.math

import android.graphics.RectF

/**
 * Created by josep on 2/18/2017.
 */
infix fun RectF.intersects(other: RectF): Boolean = RectF.intersects(this, other)
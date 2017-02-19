package com.ivieleague.swipeshot

import java.util.*

/**
 * Created by josep on 2/17/2017.
 */


inline fun <T> MutableIterator<T>.clear(action: (T) -> Unit) {
    while (hasNext()) {
        action(next())
        remove()
    }
}

inline fun <T> MutableCollection<T>.clear(action: (T) -> Unit) = iterator().clear(action)

fun <T> MutableList<T>.shuffle(random: Random = Random()): MutableList<T> {
    for (i in 0..this.size - 1) {
        val randomPosition = random.nextInt(this.size)
        val tmp: T = this[i]
        this[i] = this[randomPosition]
        this[randomPosition] = tmp
    }
    return this
}
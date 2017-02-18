package com.ivieleague.swipeshot

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

inline fun <T> MutableIterator<T>.forEachRemove(action: (T) -> Boolean) {
    while (hasNext()) {
        if (action(next())) {
            remove()
        }
    }
}
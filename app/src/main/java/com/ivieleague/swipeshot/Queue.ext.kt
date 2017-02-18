package com.ivieleague.swipeshot

import com.lightningkite.kotlin.collection.mapping
import java.util.*

/**
 * Created by josep on 2/18/2017.
 */
fun <S, D> Queue<S>.mapping(
        forwards: (S) -> D,
        reverse: (D) -> S
) = object : Queue<D> {
    override fun addAll(elements: Collection<D>): Boolean = this@mapping.addAll(elements.asSequence().map(reverse))
    override fun clear() = this@mapping.clear()
    override fun iterator(): MutableIterator<D> = this@mapping.iterator().mapping(forwards, reverse)
    override fun remove(element: D): Boolean = this@mapping.remove(reverse(element))
    override fun removeAll(elements: Collection<D>): Boolean = this@mapping.removeAll(elements.asSequence().map(reverse))
    override fun retainAll(elements: Collection<D>): Boolean = this@mapping.retainAll(elements.asSequence().map(reverse))
    override fun remove(): D = forwards(this@mapping.remove())
    override fun add(element: D): Boolean = this@mapping.add(reverse(element))
    override fun offer(e: D): Boolean = this@mapping.offer(reverse(e))
    override fun element(): D = forwards(this@mapping.element())
    override fun peek(): D = forwards(this@mapping.peek())
    override fun poll(): D = forwards(this@mapping.poll())
    override val size: Int
        get() = this@mapping.size

    override fun contains(element: D): Boolean = this@mapping.contains(reverse(element))
    override fun containsAll(elements: Collection<D>): Boolean = this@mapping.containsAll(elements.asSequence().map(reverse).toList())
    override fun isEmpty(): Boolean = this@mapping.isEmpty()

}
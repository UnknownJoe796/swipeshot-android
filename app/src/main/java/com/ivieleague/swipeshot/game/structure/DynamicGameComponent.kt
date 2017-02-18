package com.ivieleague.swipeshot.game.structure

import com.lightningkite.kotlin.collection.addSorted
import java.util.*

abstract class DynamicGameComponent<DEPENDENCY : Any, SUBDEPENDENCY> : NestedGameComponent<DEPENDENCY, SUBDEPENDENCY>() {
    private val privateChildren = ArrayList<GameComponent<SUBDEPENDENCY>>()
    override val children: List<GameComponent<SUBDEPENDENCY>> get() = privateChildren
    fun add(child: GameComponent<SUBDEPENDENCY>): Int {
        child.init(subdependency)
        return privateChildren.addSorted(child)
    }

    fun remove(child: GameComponent<SUBDEPENDENCY>) = privateChildren.remove(child)
}
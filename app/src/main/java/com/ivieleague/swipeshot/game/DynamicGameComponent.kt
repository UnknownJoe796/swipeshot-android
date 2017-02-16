package com.ivieleague.swipeshot.game

import com.lightningkite.kotlin.collection.addSorted
import java.util.*

abstract class DynamicGameComponent : NestedGameComponent() {
    private val privateChildren = ArrayList<GameComponent>()
    override val children:List<GameComponent> get() = privateChildren
    fun add(child:GameComponent) = privateChildren.addSorted(child)
    fun remove(child:GameComponent) = privateChildren.remove(child)
}
package com.ivieleague.swipeshot.game

import java.util.*

/**
 * Created by josep on 2/18/2017.
 */
interface NetInterface<T> {
    val broadcastQueue: Queue<T>
    val receiveQueue: Queue<T>
}
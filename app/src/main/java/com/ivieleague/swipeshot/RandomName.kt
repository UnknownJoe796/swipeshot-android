package com.ivieleague.swipeshot

import java.util.*

/**
 * Created by josep on 2/18/2017.
 */
object RandomName {

    val adjective = listOf(
            "Raging",
            "Angry",
            "Dashing",
            "Insane",
            "Mad",
            "Pro",
            "Great",
            "Strong",
            "Serious",
            "Super",
            "Ace",
            "Quick",
            "Burning",
            "Manly",
            "Elite",
            "Dark"
    )
    val noun = listOf(
            "Slash",
            "Swipe",
            "Waters",
            "Shot",
            "Lightning",
            "Flame",
            "Sensei",
            "Cyborg",
            "Raider",
            "Rival",
            "Cataclysm",
            "Man",
            "Maverick"
    )

    fun make(seed: String): String {
        val random = Random(seed.hashCode().toLong() or seed.reversed().hashCode().toLong().shl(32))
        return adjective[random.nextInt(adjective.size)] + " " + noun[random.nextInt(noun.size)]
    }

    fun make(): String {
        val random = Random()
        return adjective[random.nextInt(adjective.size)] + " " + noun[random.nextInt(noun.size)]
    }
}
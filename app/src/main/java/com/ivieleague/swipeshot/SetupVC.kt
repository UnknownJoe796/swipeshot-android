package com.ivieleague.swipeshot

import android.graphics.Color
import android.view.Gravity
import android.view.View
import com.ivieleague.swipeshot.game.GameWorld
import com.ivieleague.swipeshot.game.Player
import com.ivieleague.swipeshot.game.UDPBroadcastNetInterface
import com.lightningkite.kotlin.anko.FullInputType
import com.lightningkite.kotlin.anko.observable.bindString
import com.lightningkite.kotlin.anko.textInputEditText
import com.lightningkite.kotlin.anko.viewcontrollers.AnkoViewController
import com.lightningkite.kotlin.anko.viewcontrollers.containers.VCStack
import com.lightningkite.kotlin.anko.viewcontrollers.implementations.VCActivity
import com.lightningkite.kotlin.observable.property.StandardObservableProperty
import org.jetbrains.anko.*
import org.jetbrains.anko.design.textInputLayout

/**
 * Created by josep on 2/18/2017.
 */
class SetupVC(val stack: VCStack) : AnkoViewController() {

    val nameObs = StandardObservableProperty("")
    val phaseObs = StandardObservableProperty("Default")

    override fun createView(ui: AnkoContext<VCActivity>): View = ui.scrollView {
        backgroundColor = Color.WHITE

        verticalLayout {

            padding = dip(8)
            gravity = Gravity.CENTER


            textView {
                styleTitle()
                textResource = R.string.app_name
            }.lparams(matchParent, wrapContent) { margin = dip(8) }

            textView {
                styleInstructions()
                text = BuildConfig.VERSION_NAME
            }.lparams(matchParent, wrapContent) { margin = dip(8) }

            textView {
                styleInstructions()
                textResource = R.string.instructions
            }.lparams(matchParent, wrapContent) { margin = dip(8) }

            textInputLayout {
                styleDefault()
                textInputEditText {
                    styleDefault()
                    hintResource = R.string.name
                    maxLines = 1
                    inputType = FullInputType.NAME
                    bindString(nameObs)
                }
            }.lparams(dip(200), wrapContent) { margin = dip(8) }

            textInputLayout {
                styleDefault()
                textInputEditText {
                    styleDefault()
                    hintResource = R.string.world
                    maxLines = 1
                    inputType = FullInputType.NAME
                    bindString(phaseObs)
                }
            }.lparams(dip(200), wrapContent) { margin = dip(8) }

            button {
                styleDefault()
                textResource = R.string.start
                onClick {
                    val net = UDPBroadcastNetInterface<Player>()
                    stack.push(GameVC(
                            net,
                            GameWorld(
                                    Player(nameObs.value),
                                    phaseObs.value,
                                    net
                            )
                    ))
                }
            }.lparams(dip(200), wrapContent) { margin = dip(8) }
        }.lparams(matchParent, wrapContent) {
            gravity = Gravity.CENTER
        }
    }
}
package com.ivieleague.swipeshot

import android.graphics.Color
import android.view.Gravity
import android.view.View
import com.ivieleague.swipeshot.game.GameWorld
import com.ivieleague.swipeshot.game.Player
import com.ivieleague.swipeshot.game.State
import com.ivieleague.swipeshot.game.UDPBroadcastNetInterface
import com.lightningkite.kotlin.anko.FullInputType
import com.lightningkite.kotlin.anko.getUniquePreferenceId
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

    init {
        nameObs += {
            if (it.length > 32) {
                nameObs.value = it.substring(0, 32)
            }
        }
    }

    val phaseObs = StandardObservableProperty("Default")

    override fun createView(ui: AnkoContext<VCActivity>): View = ui.scrollView {
        backgroundColor = Color.WHITE
        if (nameObs.value.isBlank()) {
            nameObs.value = context.defaultSharedPreferences.getString(
                    "name",
                    RandomName.make(context.getUniquePreferenceId())
            )
        }

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

            linearLayout {
                gravity = Gravity.CENTER

                textInputLayout {
                    styleDefault()
                    textInputEditText {
                        styleDefault()
                        hintResource = R.string.name
                        maxLines = 1
                        inputType = FullInputType.NAME
                        bindString(nameObs)
                    }
                }.lparams(0, wrapContent, 2f) { margin = dip(8) }

                button {
                    styleDefault()
                    textResource = R.string.generate
                    onClick {
                        nameObs.value = RandomName.make()
                    }
                }.lparams(0, wrapContent, 1f) { margin = dip(8) }
            }.lparams(matchParent, wrapContent)

            textView {
                styleInstructions()
                textResource = R.string.world_explanation
            }.lparams(matchParent, wrapContent) { margin = dip(8) }

            linearLayout {
                gravity = Gravity.CENTER

                textInputLayout {
                    styleDefault()
                    textInputEditText {
                        styleDefault()
                        hintResource = R.string.world
                        maxLines = 1
                        inputType = FullInputType.NAME
                        bindString(phaseObs)
                    }
                }.lparams(0, wrapContent, 2f) { margin = dip(8) }

                button {
                    styleDefault()
                    textResource = R.string.start
                    onClick {
                        context.defaultSharedPreferences.edit().putString("name", nameObs.value).apply()
                        val net = UDPBroadcastNetInterface<State>()
                        stack.push(GameVC(
                                net,
                                GameWorld(
                                        Player(nameObs.value),
                                        BuildConfig.VERSION_NAME + ":" + phaseObs.value.toLowerCase().trim(),
                                        net
                                )
                        ))
                    }
                    onLongClick {
                        context.defaultSharedPreferences.edit().putString("name", nameObs.value).apply()
                        val net = UDPBroadcastNetInterface<State>()
                        stack.push(GameVC(
                                net,
                                GameWorld(
                                        Player(nameObs.value),
                                        BuildConfig.VERSION_NAME + ":" + phaseObs.value.toLowerCase().trim(),
                                        net
                                ).apply {
                                    this.cameraWorldUnitsToShow = 100f
                                }
                        ))
                        true
                    }
                }.lparams(0, wrapContent, 1f) { margin = dip(8) }

            }.lparams(matchParent, wrapContent)
        }.lparams(matchParent, wrapContent) {
            gravity = Gravity.CENTER
        }
    }
}
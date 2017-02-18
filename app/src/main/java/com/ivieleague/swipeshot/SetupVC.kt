package com.ivieleague.swipeshot

import android.graphics.Color
import android.view.Gravity
import android.view.View
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

    override fun createView(ui: AnkoContext<VCActivity>): View = ui.verticalLayout {

        padding = dip(8)
        gravity = Gravity.CENTER

        backgroundColor = Color.WHITE

        textView {
            styleTitle()
            textResource = R.string.app_name
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

        button {
            styleDefault()
            textResource = R.string.start
            onClick {
                stack.push(GameVC(nameObs.value))
            }
        }.lparams(dip(200), wrapContent) { margin = dip(8) }
    }
}
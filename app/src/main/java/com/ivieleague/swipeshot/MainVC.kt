package com.ivieleague.swipeshot

import android.view.Gravity
import android.view.View
import com.lightningkite.kotlin.anko.viewcontrollers.AnkoViewController
import com.lightningkite.kotlin.anko.viewcontrollers.implementations.VCActivity
import org.jetbrains.anko.*

/**
 * Created by josep on 2/15/2017.
 */
class MainVC: AnkoViewController() {

    val gameVC = GameVC()

    override fun createView(ui: AnkoContext<VCActivity>): View = ui.verticalLayout {
        textView("HEY"){
            gravity = Gravity.CENTER
            padding = dip(2)
        }.lparams(matchParent, wrapContent)

        viewController(gameVC){}.lparams(matchParent, 0, 1f)
    }
}
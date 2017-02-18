package com.ivieleague.swipeshot

import android.view.View
import android.widget.FrameLayout
import com.lightningkite.kotlin.anko.viewcontrollers.AnkoViewController
import com.lightningkite.kotlin.anko.viewcontrollers.containers.VCStack
import com.lightningkite.kotlin.anko.viewcontrollers.implementations.VCActivity
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.frameLayout
import org.jetbrains.anko.matchParent

/**
 * Created by josep on 2/15/2017.
 */
class MainVC: AnkoViewController() {

    val stack = VCStack().apply {
        reset(SetupVC(this))
    }

    override fun createView(ui: AnkoContext<VCActivity>): View = ui.frameLayout {
        embedViewContainer(stack, { FrameLayout.LayoutParams(matchParent, matchParent) })
    }

    override fun onBackPressed(backAction: () -> Unit) = stack.onBackPressed(backAction)
}
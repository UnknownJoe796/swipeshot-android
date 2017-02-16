package com.ivieleague.swipeshot

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.lightningkite.kotlin.anko.viewcontrollers.ViewController

import com.lightningkite.kotlin.anko.viewcontrollers.implementations.VCActivity

class MainActivity : VCActivity() {

    companion object{
        val staticController = MainVC()
    }

    override val viewController: ViewController
        get() = staticController
}

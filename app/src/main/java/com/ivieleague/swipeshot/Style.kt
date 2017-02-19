package com.ivieleague.swipeshot

import android.graphics.Color
import android.support.design.widget.TextInputLayout
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import org.jetbrains.anko.dip
import org.jetbrains.anko.padding
import org.jetbrains.anko.textColor


fun TextView.styleTitle() {
    textColor = Color.BLACK
    textSize = 24f
    gravity = Gravity.CENTER
}

fun TextView.styleInstructions() {
    textColor = Color.BLACK
    textSize = 16f
    gravity = Gravity.CENTER
}

fun EditText.styleDefault() {
    padding = dip(8)
    minimumHeight = dip(42)
}

fun TextInputLayout.styleDefault() {
    padding = dip(8)
    minimumHeight = dip(42)
}

fun Button.styleDefault() {
    padding = dip(8)
    minimumHeight = dip(42)
}
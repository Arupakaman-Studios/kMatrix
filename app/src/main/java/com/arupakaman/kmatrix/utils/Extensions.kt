package com.arupakaman.kmatrix.utils

import android.content.Context
import android.graphics.Paint
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.text.HtmlCompat
import androidx.core.text.toSpanned


operator fun <T> T.invoke(block: T.() -> Unit) = block()

/**
 *   Toast
 */

fun Context.toast(mMsg: String) {
    Toast.makeText(this, mMsg, Toast.LENGTH_SHORT).show()
}

fun Context.toast(@StringRes mResId: Int) {
    toast(getString(mResId))
}

fun Context.toastLong(mMsg: String) {
    Toast.makeText(this, mMsg, Toast.LENGTH_LONG).show()
}

fun Context.toastLong(@StringRes mResId: Int) {
    toastLong(getString(mResId))
}

var TextView.isUnderlined: Boolean
    get() = ((paintFlags and Paint.UNDERLINE_TEXT_FLAG) == Paint.UNDERLINE_TEXT_FLAG)
    set(isUnderlined) {
        paintFlags = if(isUnderlined) (paintFlags or Paint.UNDERLINE_TEXT_FLAG) else (paintFlags xor Paint.UNDERLINE_TEXT_FLAG)
    }

var TextView.htmlText: String?
    get() = HtmlCompat.toHtml(
        (text ?: "").toSpanned(),
        HtmlCompat.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE
    )
    set(string) { text = HtmlCompat.fromHtml(string ?: "", HtmlCompat.FROM_HTML_MODE_LEGACY) }

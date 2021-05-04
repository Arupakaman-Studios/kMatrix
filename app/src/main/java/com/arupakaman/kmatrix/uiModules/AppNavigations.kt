package com.arupakaman.kmatrix.uiModules

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.arupakaman.kmatrix.BuildConfig
import com.arupakaman.kmatrix.R
import com.arupakaman.kmatrix.utils.reportExceptionToCrashlytics


fun Context.openContactMail(msg: String? = null){
    kotlin.runCatching {
        Intent(Intent.ACTION_SENDTO).let { emailIntent ->
            emailIntent.data = Uri.parse("mailto:")
            emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.email_publisher)))
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
            emailIntent.putExtra(Intent.EXTRA_TEXT, msg ?: getString(R.string.msg_enter_your_message))
            val packageManager = packageManager

            if (emailIntent.resolveActivity(packageManager) != null) {
                startActivity(Intent.createChooser(emailIntent, getString(R.string.title_send_via)))
            }
        }
    }.onFailure {
        it.reportExceptionToCrashlytics("openContactMail")
    }
}

fun Context.openShareAppIntent(){
    kotlin.runCatching {
        Intent(Intent.ACTION_SEND).let { shareIntent ->
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
            var shareMessage = "\n${getString(R.string.msg_share_kmatrix)}"
            shareMessage = "$shareMessage https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(Intent.createChooser(shareIntent, getString(R.string.title_share_via)))
        }
    }.onFailure {
        it.reportExceptionToCrashlytics("openShareAppIntent Exc")
        Log.e("openShareAppIntent", "shareApp Exc : $it")
    }
}

fun Context.openAppInPlayStore(id: String){
    val optionalIntent =  Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/apps/details?id=$id")
    )
    kotlin.runCatching {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$id"))
        if (intent.resolveActivity(packageManager) != null) startActivity(intent)
        else startActivity(optionalIntent)
    }.onFailure {
        it.reportExceptionToCrashlytics("openAppInPlayStore Exc")
        startActivity(optionalIntent)
    }
}
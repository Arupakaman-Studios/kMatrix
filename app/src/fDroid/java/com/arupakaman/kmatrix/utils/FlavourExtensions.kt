package com.arupakaman.kmatrix.utils

import com.arupakaman.kmatrix.BuildConfig
import com.google.firebase.crashlytics.FirebaseCrashlytics

fun Throwable.reportExceptionToCrashlytics(msg: String){
    if (!BuildConfig.DEBUG) FirebaseCrashlytics.getInstance().log("MANUAL_LOGGING : $msg | Exc : $this")
}
package com.arupakaman.kmatrix

import android.content.Context
import android.content.res.Configuration
import androidx.multidex.MultiDexApplication
import com.arupakaman.kmatrix.data.KMatrixSharedPrefs
import com.arupakaman.kmatrix.utils.DefaultExceptionHandler

class KMatrixApp : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG || BuildConfig.isFDroid)
            Thread.setDefaultUncaughtExceptionHandler(DefaultExceptionHandler(this))

        //Init
        KMatrixSharedPrefs.getInstance(this)

    }

}
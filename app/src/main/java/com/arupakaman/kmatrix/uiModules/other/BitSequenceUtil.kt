package com.arupakaman.kmatrix.uiModules.other

import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.BlurMaskFilter.Blur
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import com.arupakaman.kmatrix.constants.AppConstants
import com.arupakaman.kmatrix.data.KMatrixSharedPrefs
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayDeque
import kotlin.math.pow


/** x,y : The position to draw the sequence at on the screen  */

object BitSequenceUtil {

    @JvmStatic
    fun getCharSetName(context: Context) = KMatrixSharedPrefs.getInstance(context).charSetName

    @JvmStatic
    fun getCharSetCustomRandomName(context: Context) = KMatrixSharedPrefs.getInstance(context).charSetCustomRandom

    @JvmStatic
    fun getCharSetCustomExactName(context: Context) = KMatrixSharedPrefs.getInstance(context).charSetCustomExact

    @JvmStatic
    fun getNumBits(context: Context) = KMatrixSharedPrefs.getInstance(context).numberOfBits

    @JvmStatic
    fun getSizeBits(context: Context) = KMatrixSharedPrefs.getInstance(context).sizeOfBits

    @JvmStatic
    fun getColorBits(context: Context) = KMatrixSharedPrefs.getInstance(context).colorOfBits

    @JvmStatic
    fun getChangeSpeedBits(context: Context) = KMatrixSharedPrefs.getInstance(context).changeSpeedOfBits

    @JvmStatic
    fun getFallSpeedBits(context: Context) = KMatrixSharedPrefs.getInstance(context).fallSpeedOfBits

    @JvmStatic
    fun getDepthEnabled(context: Context) = KMatrixSharedPrefs.getInstance(context).depthEnabled

}

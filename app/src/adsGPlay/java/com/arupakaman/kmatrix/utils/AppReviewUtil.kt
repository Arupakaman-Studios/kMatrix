package com.arupakaman.kmatrix.utils

import android.app.Activity
import android.util.Log
import com.arupakaman.kmatrix.BuildConfig
import com.arupakaman.kmatrix.uiModules.openAppInPlayStore
import com.google.android.play.core.review.ReviewManagerFactory

object AppReviewUtil {

    fun askForReview(mActivity: Activity){
        val reviewManager = ReviewManagerFactory.create(mActivity)
        reviewManager.requestReviewFlow().addOnCompleteListener { request ->
            if (request.isSuccessful) {
                //Received ReviewInfo object
                val reviewInfo = request.result
                Log.d("AppReviewUtil", "reviewInfo -> $reviewInfo")
                kotlin.runCatching {
                    val flow = reviewManager.launchReviewFlow(mActivity, reviewInfo)
                    flow.addOnCompleteListener {
                        Log.d("AppReviewUtil", "CompleteListener -> ${it.isSuccessful} ${it.exception}")
                        if (!it.isSuccessful)
                            mActivity.openAppInPlayStore(BuildConfig.APPLICATION_ID)
                    }
                }.onFailure {
                    mActivity.openAppInPlayStore(BuildConfig.APPLICATION_ID)
                }
            }
        }
    }

}
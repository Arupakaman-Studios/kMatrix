package com.arupakaman.kmatrix.uiModules.home

import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.arupakaman.kmatrix.BuildConfig
import com.arupakaman.kmatrix.R
import com.arupakaman.kmatrix.constants.AppConstants
import com.arupakaman.kmatrix.data.KMatrixSharedPrefs
import com.arupakaman.kmatrix.databinding.ActivityHomeBinding
import com.arupakaman.kmatrix.services.KMatrixWallpaperService
import com.arupakaman.kmatrix.uiModules.dialogs.KMatrixDialogs
import com.arupakaman.kmatrix.uiModules.openAppInPlayStore
import com.arupakaman.kmatrix.uiModules.openShareAppIntent
import com.arupakaman.kmatrix.utils.AdsUtil
import com.arupakaman.kmatrix.utils.AppReviewUtil
import com.arupakaman.kmatrix.utils.invoke
import com.arupakaman.kmatrix.utils.reportExceptionToCrashlytics
import com.arupakaman.kmatrix.utils.setSafeOnClickListener
import com.arupakaman.kmatrix.utils.toast


class ActivityHome : AppCompatActivity() {

    companion object {

        private val TAG by lazy { "ActivityHome" }

        private const val REQUEST_CODE_SET_LIVE_WALLPAPER = 1001

    }

    private val mBinding by lazy { ActivityHomeBinding.inflate(layoutInflater) }
    private val mPrefs by lazy { KMatrixSharedPrefs.getInstance(applicationContext) }
    private lateinit var mAdsUtil: AdsUtil
    private lateinit var mDialogs: KMatrixDialogs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        mDialogs = KMatrixDialogs(this)
        mAdsUtil = AdsUtil(this)

        mBinding{
            setSavedData()
            setViewListeners()

            mAdsUtil.loadBannerAd(includeAdView.adView)
        }
    }

    override fun onDestroy() {
        if (::mDialogs.isInitialized){
            mDialogs.dismiss()
        }
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menuItemShare -> openShareAppIntent()
            R.id.menuItemRate -> AppReviewUtil.askForReview(this)
            R.id.menuItemMoreApps -> openAppInPlayStore("Arupakaman+Studios")
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("SetTextI18n")
    private fun ActivityHomeBinding.setSavedData(){

        tvCharSetDesc.text = mPrefs.charSetName

        viewBitColor.setBackgroundColor(Color.parseColor(mPrefs.colorOfBits))
        viewBgColor.setBackgroundColor(Color.parseColor(mPrefs.colorBackground))

        tvBitsNumDesc.text = mPrefs.numberOfBits.toString()
        tvBitSizeDesc.text = mPrefs.sizeOfBits.toString()
        tvChangeSpeedDesc.text = "${mPrefs.changeSpeedOfBits} %"
        tvFallSpeedDesc.text = "${mPrefs.fallSpeedOfBits} %"
        switchDepth.isChecked = mPrefs.depthEnabled
    }

    private fun ActivityHomeBinding.setViewListeners(){

        btnSetWallpaper.setSafeOnClickListener {
            if (BuildConfig.isAdsOn){
                val count = mPrefs.setWallpaperCount
                if (count == 3) {
                    mAdsUtil.showInterstitialAd(getString(R.string.key_ad_mob_interstitial_id)) {
                        mPrefs.setWallpaperCount = 0
                    }
                }else {
                    mPrefs.setWallpaperCount = count + 1
                    onSetWallpaper()
                }
            }else onSetWallpaper()
        }

        clCharSet.setSafeOnClickListener { onCharSetClick() }

        clBitColor.setSafeOnClickListener { onBitColorClick() }

        clBgColor.setSafeOnClickListener { onBgColorClick() }

        switchDepth.setOnCheckedChangeListener { _, b ->
            mPrefs.depthEnabled = b
        }

        clBitsNum.setSafeOnClickListener { onNumOfBitsClick() }

        clBitSize.setSafeOnClickListener { onSizeOfBitsClick() }

        clBitChangeSpeed.setSafeOnClickListener { onChangeSpeedOfBitsClick() }

        clBitFallSpeed.setSafeOnClickListener { onFallSpeedOfBitsClick() }

        clReset.setSafeOnClickListener { onResetClick() }

        clDonateVersion.setSafeOnClickListener { openAppInPlayStore(getString(R.string.donate_version_pkg_name)) }

        clAboutUs.setSafeOnClickListener { mDialogs.showAboutUsDialog() }

    }

    private fun onSetWallpaper(){
        val intent = Intent()
        kotlin.runCatching {
            val component = ComponentName(this, KMatrixWallpaperService::class.java)
            intent.action = WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER
            intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, component)
        }.onFailure {
            // try the generic android wallpaper chooser next
            Log.e(TAG, "onSetWallpaper Exc -> $it")
            it.reportExceptionToCrashlytics("onSetWallpaper Exc")
            intent.action = WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER
        }
        startActivityForResult(intent, REQUEST_CODE_SET_LIVE_WALLPAPER)
    }

    private fun onNumOfBitsClick(){
        mDialogs.showCommonSliderDialog(getString(R.string.title_number_of_bits),
                AppConstants.MIN_NUM_OF_BITS, AppConstants.MAX_NUM_OF_BITS, mPrefs.numberOfBits,
                rBtnTxt = getString(R.string.action_set), rBtnClick = {
            mBinding.tvBitsNumDesc.text = it.toString()
            mPrefs.numberOfBits = it
            mDialogs.dismiss()
        })
    }

    private fun onSizeOfBitsClick(){
        mDialogs.showCommonSliderDialog(getString(R.string.title_bit_text_size),
                AppConstants.MIN_SIZE_OF_BITS, AppConstants.MAX_SIZE_OF_BITS, mPrefs.sizeOfBits,
                rBtnTxt = getString(R.string.action_set), rBtnClick = {
            mBinding.tvBitSizeDesc.text = it.toString()
            mPrefs.sizeOfBits = it
            mDialogs.dismiss()
        })
    }

    @SuppressLint("SetTextI18n")
    private fun onChangeSpeedOfBitsClick(){
        mDialogs.showCommonSliderDialog(getString(R.string.title_bit_change_speed),
                AppConstants.MIN_CHANGE_SPEED_OF_BITS, AppConstants.MAX_CHANGE_SPEED_OF_BITS, mPrefs.changeSpeedOfBits, postfix = "%",
                rBtnTxt = getString(R.string.action_set), rBtnClick = {
            mBinding.tvChangeSpeedDesc.text = "$it %"
            mPrefs.changeSpeedOfBits = it
            mDialogs.dismiss()
        })
    }

    @SuppressLint("SetTextI18n")
    private fun onFallSpeedOfBitsClick(){
        mDialogs.showCommonSliderDialog(getString(R.string.title_bit_falling_speed),
                AppConstants.MIN_FALL_SPEED_OF_BITS, AppConstants.MAX_FALL_SPEED_OF_BITS, mPrefs.fallSpeedOfBits, 1, postfix = "%",
                rBtnTxt = getString(R.string.action_set), rBtnClick = {
            mBinding.tvFallSpeedDesc.text = "$it %"
            mPrefs.fallSpeedOfBits = it
            mDialogs.dismiss()
        })
    }

    private fun onBitColorClick(){
        mDialogs.showCommonColorPickerDialog(getString(R.string.title_bit_color), Color.parseColor(mPrefs.colorOfBits), rBtnClick = {
            mPrefs.colorOfBits = it
            mBinding.viewBitColor.setBackgroundColor(Color.parseColor(it))
            mDialogs.dismiss()
        })
    }

    private fun onBgColorClick(){
        mDialogs.showCommonColorPickerDialog(getString(R.string.title_background_color), Color.parseColor(mPrefs.colorBackground), rBtnClick = {
            mPrefs.colorBackground = it
            mBinding.viewBgColor.setBackgroundColor(Color.parseColor(it))
            mDialogs.dismiss()
        })
    }

    private fun onCharSetClick(){
        mDialogs.showCharSetPickerDialog {
            mBinding.tvCharSetDesc.text = it
            mDialogs.dismiss()
        }
    }

    private fun onResetClick(){
        mPrefs{
            charSetName = AppConstants.CHAR_SET_NAME_MATRIX
            charSetCustomRandom = getString(R.string.app_name)
            charSetCustomExact = getString(R.string.app_name)
            numberOfBits = 10
            colorOfBits = "#03A062"
            colorBackground = "#000000"
            sizeOfBits = 35
            changeSpeedOfBits = 100
            fallSpeedOfBits = 100
            depthEnabled = true
        }
        mBinding.setSavedData()
        toast(R.string.msg_values_reset)
    }

}
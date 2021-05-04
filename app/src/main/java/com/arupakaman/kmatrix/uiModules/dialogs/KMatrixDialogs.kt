package com.arupakaman.kmatrix.uiModules.dialogs

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.viewbinding.ViewBinding
import com.arupakaman.kmatrix.R
import com.arupakaman.kmatrix.constants.AppConstants
import com.arupakaman.kmatrix.data.KMatrixSharedPrefs
import com.arupakaman.kmatrix.databinding.DialogAboutUsLayoutBinding
import com.arupakaman.kmatrix.databinding.DialogCharSetPickerLayoutBinding
import com.arupakaman.kmatrix.databinding.DialogColorPickerLayoutBinding
import com.arupakaman.kmatrix.databinding.DialogSliderLayoutBinding
import com.arupakaman.kmatrix.uiModules.openAppInPlayStore
import com.arupakaman.kmatrix.uiModules.openContactMail
import com.arupakaman.kmatrix.uiModules.openShareAppIntent
import com.arupakaman.kmatrix.utils.AppReviewUtil
import com.arupakaman.kmatrix.utils.invoke
import com.arupakaman.kmatrix.utils.isUnderlined
import com.arupakaman.kmatrix.utils.setSafeOnClickListener
import com.arupakaman.kmatrix.utils.toast
import org.w3c.dom.Text


class KMatrixDialogs(private val mActivity: Activity) {

    companion object{
        private val TAG by lazy { "SOSAppDialogs" }

        private const val BOTTOM_DIALOG_Y_POSITION = 300
    }

    var mDialog: Dialog? = null

    val isShowingDialog: Boolean
        get() {
            return mDialog?.isShowing?:false
        }

    fun showDialog(){
        mDialog?.runCatching {
            if (!mActivity.isFinishing) show()
        }
    }


    fun dismiss() {
        mDialog?.runCatching {
            if (isShowing) dismiss()
        }
        mDialog = null
    }

    private fun setGravity(mGravity: Int, xPos: Int = 0, yPos: Int = 0){
        mDialog?.apply {
            val attrs = window?.attributes
            attrs?.apply {
                gravity = mGravity
                x = xPos
                y = yPos
            }
            window?.attributes = attrs
            window?.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }


    /**
     *   Init Dialog
     */

    fun <T : ViewBinding> initNewDialog(viewBinding: T): T {
        dismiss()

        Dialog(mActivity).apply {
            window?.let { win ->
                win.requestFeature(Window.FEATURE_NO_TITLE)
                win.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
            setContentView(viewBinding.root)
        }.also { dialog ->
            setGravity(Gravity.BOTTOM, yPos = BOTTOM_DIALOG_Y_POSITION)
            mDialog = dialog
        }
        return viewBinding
    }

    @SuppressLint("SetTextI18n")
    fun showCommonSliderDialog(
            title: String, fromVal: Int, toVal: Int, currVal: Int, step: Int = 1, postfix: String = "",
            rBtnTxt: String?, isCancelable: Boolean = true,
            lBtnClick: (() -> Unit)? = null, rBtnClick: ((Int) -> Unit)? = null
    ): DialogSliderLayoutBinding {
        return initNewDialog(DialogSliderLayoutBinding.inflate(mActivity.layoutInflater)).apply {
            mDialog?.setCancelable(isCancelable)
            tvDialogTitle.text = title
            tvDialogMsg.text = "$currVal $postfix"
            btnLeft.text = mActivity.getString(R.string.action_cancel)
            btnRight.text = rBtnTxt?:""
            btnRight.isVisible = !rBtnTxt.isNullOrBlank()
            sliderDialog{
                value = currVal.toFloat()
                valueFrom = fromVal.toFloat()
                valueTo = toVal.toFloat()
                stepSize = step.toFloat()
            }

            sliderDialog.addOnChangeListener { _, value, _ ->
                tvDialogMsg.text = "${value.toInt()} $postfix"
            }

            btnLeft.setSafeOnClickListener {
                if (lBtnClick == null){
                    dismiss()
                }else lBtnClick.invoke()
            }
            btnRight.setSafeOnClickListener {
                rBtnClick?.invoke(sliderDialog.value.toInt())
            }
            showDialog()
        }
    }

    fun showCommonColorPickerDialog(
            title: String, selColor: Int, lBtnClick: (() -> Unit)? = null,
            rBtnClick: ((String) -> Unit)? = null
    ): DialogColorPickerLayoutBinding{
        return initNewDialog(DialogColorPickerLayoutBinding.inflate(mActivity.layoutInflater)).apply {
            tvDialogTitle.text = title
            btnLeft.text = mActivity.getString(R.string.action_cancel)
            btnRight.text = mActivity.getString(R.string.action_select)

            colorPicker.setInitialColor(selColor)
            cardSelColor.setCardBackgroundColor(selColor)

            etColorCode.setText(String.format("#%06X", 0xFFFFFF and selColor))

            colorPicker.subscribe { color, _, _ ->
                cardSelColor.setCardBackgroundColor(color)
                etColorCode.setText(String.format("#%06X", 0xFFFFFF and color))
            }

            btnLeft.setSafeOnClickListener {
                if (lBtnClick == null){
                    dismiss()
                }else lBtnClick.invoke()
            }
            btnRight.setSafeOnClickListener {
                rBtnClick?.invoke(String.format("#%06X", 0xFFFFFF and colorPicker.color))
            }

            etColorCode.setOnEditorActionListener(TextView.OnEditorActionListener { _, p1, _ ->
                if (p1 == EditorInfo.IME_ACTION_DONE) {
                    kotlin.runCatching {
                        val color = Color.parseColor(etColorCode.text?.toString()?.trim()?:"")
                        colorPicker.setInitialColor(color)
                    }.onFailure { mActivity.toast(R.string.err_enter_valid_color_hex) }
                    return@OnEditorActionListener true
                }
                return@OnEditorActionListener false
            })

            showDialog()
        }
    }

    fun showCharSetPickerDialog(rBtnClick: ((String) -> Unit)? = null
    ): DialogCharSetPickerLayoutBinding {
        return initNewDialog(DialogCharSetPickerLayoutBinding.inflate(mActivity.layoutInflater)).apply {
            acTvCharSet.setAdapter(ArrayAdapter(mActivity, android.R.layout.simple_list_item_1, listOf(
                    AppConstants.CHAR_SET_NAME_MATRIX,
                    AppConstants.CHAR_SET_NAME_BINARY,
                    AppConstants.CHAR_SET_NAME_CUSTOM_RANDOM,
                    AppConstants.CHAR_SET_NAME_CUSTOM_EXACT
            )))

            val mPrefs = KMatrixSharedPrefs.getInstance(mActivity)
            acTvCharSet.setText(mPrefs.charSetName, false)

            fun customStrIpVisibility(charSetName: String){
                Log.d(TAG, "customStrIpVisibility -> $charSetName")
                when (charSetName) {
                    AppConstants.CHAR_SET_NAME_CUSTOM_RANDOM -> {
                        iplCustomStr.isVisible = true
                        etCustomStr.setText(mPrefs.charSetCustomRandom)
                    }
                    AppConstants.CHAR_SET_NAME_CUSTOM_EXACT -> {
                        iplCustomStr.isVisible = true
                        etCustomStr.setText(mPrefs.charSetCustomExact)
                    }
                    else -> {
                        iplCustomStr.isVisible = false
                    }
                }
            }

            customStrIpVisibility(acTvCharSet.text.toString())

            acTvCharSet.setOnItemClickListener { _, _, i, _ ->
                Log.d(TAG, "customStrIpVisibility -> $i")
                customStrIpVisibility(acTvCharSet.text.toString())
            }

            btnLeft.setSafeOnClickListener {
                dismiss()
            }
            btnRight.setSafeOnClickListener {
                val selCharSetName = acTvCharSet.text?.toString()?:AppConstants.CHAR_SET_NAME_MATRIX
                val customStr = etCustomStr.text?.toString()?.trim()
                if (selCharSetName == AppConstants.CHAR_SET_NAME_CUSTOM_RANDOM){
                    if (customStr.isNullOrBlank()){
                        mActivity.toast(R.string.err_enter_valid_custom_characters)
                        return@setSafeOnClickListener
                    }
                    mPrefs.charSetCustomRandom = customStr
                }else if (selCharSetName == AppConstants.CHAR_SET_NAME_CUSTOM_EXACT){
                    if (customStr.isNullOrBlank()){
                        mActivity.toast(R.string.err_enter_valid_custom_characters)
                        return@setSafeOnClickListener
                    }
                    mPrefs.charSetCustomExact = customStr
                }

                mPrefs.charSetName = selCharSetName
                rBtnClick?.invoke(selCharSetName)
            }

            showDialog()
        }
    }

    fun showAboutUsDialog(): DialogAboutUsLayoutBinding {
        return initNewDialog(DialogAboutUsLayoutBinding.inflate(mActivity.layoutInflater)).apply {

            tvContactMail.isUnderlined = true
            tvShare.isUnderlined = true
            tvMoreApps.isUnderlined = true
            tvRate.isUnderlined = true

            tvAboutUsMsg.movementMethod = LinkMovementMethod.getInstance()

            tvContactMail.setSafeOnClickListener { mActivity.openContactMail() }

            tvShare.setSafeOnClickListener { mActivity.openShareAppIntent() }

            tvMoreApps.setSafeOnClickListener { mActivity.openAppInPlayStore("Arupakaman+Studios") }

            tvRate.setSafeOnClickListener { AppReviewUtil.askForReview(mActivity) }

            showDialog()
        }
    }


}
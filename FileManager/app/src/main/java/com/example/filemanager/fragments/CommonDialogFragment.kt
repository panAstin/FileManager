package com.example.filemanager.fragments

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.support.v4.app.DialogFragment
import com.example.filemanager.R

/**
 * Created by 110466 on 2018/2/4
 * 公共dialogfragment类
 */

class CommonDialogFragment : DialogFragment() {

    /**
     * 监听弹出窗是否被取消
     */
    private var mCancelListener: OnDialogCancelListener? = null

    /**
     * 回调获得需要显示的 dialog
     */
    private var mOnCallDialog: OnCallDialog? = null

    interface OnDialogCancelListener {
        fun onCancel()
    }

    interface OnCallDialog {
        fun getDialog(context: Context): Dialog
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (null == mOnCallDialog) {
            super.onCreate(savedInstanceState)
        }
        return mOnCallDialog!!.getDialog(activity)
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {

            // 在 5.0 以下的版本会出现白色背景边框，若在 5.0 以上设置则会造成文字部分的背景也变成透明
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                // 目前只有这两个 dialog 会出现边框
                if (dialog is ProgressDialog || dialog is DatePickerDialog) {
                    getDialog().window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                }
            }

            val window = getDialog().window
            val windowParams = window!!.attributes
            windowParams.dimAmount = 0.0f
            windowParams.width = resources.getDimensionPixelSize(R.dimen.df_width)
            window.attributes = windowParams
        }
    }

    override fun onCancel(dialog: DialogInterface?) {
        super.onCancel(dialog)
        if (mCancelListener != null) {
            mCancelListener!!.onCancel()
        }
    }

    companion object {

        @JvmOverloads
        fun newInstance(callDialog: OnCallDialog, cancelable: Boolean, cancelListener: OnDialogCancelListener? = null): CommonDialogFragment {
            val instance = CommonDialogFragment()
            instance.isCancelable = cancelable
            instance.mCancelListener = cancelListener
            instance.mOnCallDialog = callDialog
            return instance
        }
    }
}


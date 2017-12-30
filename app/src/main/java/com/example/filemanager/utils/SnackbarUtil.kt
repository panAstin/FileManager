package com.example.filemanager.utils

import android.support.design.widget.Snackbar
import android.view.View

/**
 * Created by 11046 on 2017/11/29.
 * Snackbar工具类
 */
class SnackbarUtil(snackbar: Snackbar){
    companion object {
        private val color_info = 0XFF2094F3
        private val color_confirm = 0XFF4CB04E
        private val color_warning = 0XFFFEC005
        private val color_danger = 0XFFF44336
        var snackbar:Snackbar? = null

        /**
         * 实例化方法
         */
        fun short(view:View,message:String){
            Snackbar.make(view,message,Snackbar.LENGTH_SHORT).show()
        }
    }

    init {
        SnackbarUtil.snackbar = snackbar
    }

    fun getSnackbar():Snackbar = SnackbarUtil.snackbar!!
}
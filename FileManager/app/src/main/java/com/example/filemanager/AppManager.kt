package com.example.filemanager

import android.annotation.SuppressLint
import android.content.Context

class AppManager {
    companion object {
        @SuppressLint("StaticFieldLeak")
        private var sInstance:AppManager? = null

        fun getInstance(context:Context):AppManager{
            if (sInstance == null){
                sInstance = AppManager(context.applicationContext)
            }
            return sInstance!!
        }

        fun getContext():Context = sInstance?.mContext!!
    }


    private var mContext:Context? = null

    private constructor(context: Context):this(){
        mContext = context
    }

    constructor()

}


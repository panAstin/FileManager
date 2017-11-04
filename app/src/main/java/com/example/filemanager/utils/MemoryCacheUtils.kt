package com.example.filemanager.utils

import android.graphics.Bitmap
import android.util.LruCache

/**
 * Created by 11046 on 2017/11/2.
 * 内存缓存
 */
class MemoryCacheUtils(){
    private var mMemoryCache: LruCache<Int, Bitmap>? = null

    fun MemoryCacheUtils(){
        val maxMemory = Runtime.getRuntime().maxMemory()/8 //默认16M
        mMemoryCache = LruCache(maxMemory.toInt())
    }

    fun getBitmapFromMemory(id:Int) = mMemoryCache?.get(id) //从内存读图片

    fun setBitmapToMemory(id: Int,bitmap: Bitmap){
        mMemoryCache?.put(id,bitmap)
    }
}
package com.example.filemanager.utils

import android.graphics.Bitmap
import android.util.Log
import android.util.LruCache

/**
 * Created by 11046 on 2017/11/2.
 * 内存缓存
 */
class MemoryCacheUtils {
    private var mMemoryCache: LruCache<Any, Any>? = null  //缓存

    /**
     * 初始化
     */
    init{
        val maxMemory = Runtime.getRuntime().maxMemory()/8 //默认16M
        mMemoryCache = LruCache(maxMemory.toInt())
    }

    /**
     * 从内存中获取图片
     * @param id 标识
     */
    fun getBitmapFromMemory(id:Int) = mMemoryCache?.get(id) //从内存读图片

    /**
     * 将图片存入内存
     * @param id 标识
     * @param bitmap 图片
     */
    fun setBitmapToMemory(id: Int,bitmap: Bitmap){
        mMemoryCache?.put(id,bitmap)
        Log.i("mmmm","put")
    }

    /**
     * 从内存中获取滚动条位置
     * @param path 路径
     */
    fun getPositionFromMemory(path:String) = mMemoryCache?.get(path) //从内存读图片

    /**
     * 将滚动条存入内存
     * @param path 标识
     * @param position 位置
     */
    fun setPositionToMemory(path: String, position: Int) {
        mMemoryCache?.put(path, position)
        Log.i("mmmm","put")
    }
}
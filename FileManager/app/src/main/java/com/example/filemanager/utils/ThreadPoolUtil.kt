package com.example.filemanager.utils

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Created by panAstin on 2018/3/21.
 * 线程池单例类
 */
object ThreadPoolUtil{
    private var cacheThreadPool:ExecutorService? = null

    fun getThreadPool():ExecutorService{
        if (cacheThreadPool == null){
            cacheThreadPool = Executors.newCachedThreadPool()
        }
        return cacheThreadPool!!
    }
}
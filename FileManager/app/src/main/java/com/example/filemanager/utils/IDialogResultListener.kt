package com.example.filemanager.utils

/**
 * Created by 11046 on 2018/2/4
 * dialog结果监听
 */

interface IDialogResultListener<in T> {
    fun onDataResult(result: T)
}

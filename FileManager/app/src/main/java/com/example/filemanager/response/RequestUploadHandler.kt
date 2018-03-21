package com.example.filemanager.response

import android.util.Log
import com.example.filemanager.utils.FileUtil
import java.io.File

/**
 * Created by 11046 on 2017/12/4.
 * 文件上传Handler
 */
class RequestUploadHandler(var mode: Int){
    fun upload(params:Map<String,String>,files:Map<String,String>):Boolean {
        val uploadpath = params["path"]
        var targetFilepath = when (mode) {
            0 -> {
                FileUtil.ROOT_PATH + "/Download"
            }
            else -> {
                FileUtil.ROOT_PATH + uploadpath
            }
        }
        for (entry in params.entries) {
            val paramKey = entry.key
            Log.i("file", paramKey)
            if (paramKey == "uploadfile") {
                Log.i("file", entry.value)
                val tempFilepath = files[paramKey]
                Log.i("file", tempFilepath)
                val tempFile = File(tempFilepath)
                targetFilepath += entry.value
                if (File(targetFilepath).exists()) {
                    return false
                } else {
                    moveFile(tempFile, targetFilepath)
                }
            }
        }
        return true
    }

    /**
     * 移动文件到相应目录
     */
    private fun moveFile(f:File,topath:String){
        FileUtil.copy(f.path,topath)
        f.delete()
    }
}
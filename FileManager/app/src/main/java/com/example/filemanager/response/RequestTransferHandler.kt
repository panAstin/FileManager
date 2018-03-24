package com.example.filemanager.response

import android.util.Log
import com.example.filemanager.utils.FileUtil
import java.io.File
import java.net.URLDecoder

/**
 * Created by 11046 on 2017/12/4.
 * 文件上传Handler
 */
object RequestTransferHandler{
    /**
     * 上传文件
     */
    fun upload(params:Map<String,String>,files:Map<String,String>,mode: Int):Boolean {
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

    /**
     * 打包下载文件
     */
    fun download(params:Map<String,String>):String? {
        Log.i("Andserver","Params"+params.toString())
        val path = FileUtil.ROOT_PATH + URLDecoder.decode(params["path"],"utf-8")
        val fnames = URLDecoder.decode(params["filenames"],"utf-8")
        val fnamelist:List<String> = fnames.split("/")
        val zipName = fnamelist[0] + "等文件打包.zip"
        val zipPath = path + File.separator + zipName
        val files = ArrayList<File>()
        fnamelist
                .map { path + File.separator + it }
                .mapTo(files) { File(it) }
        if (FileUtil.zipFiles(files,zipPath)) {
            return zipPath
        }
        return null
    }
}
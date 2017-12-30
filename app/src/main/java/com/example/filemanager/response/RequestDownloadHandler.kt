package com.example.filemanager.response

import android.os.Environment
import android.util.Log
import com.example.filemanager.utils.FileUtil
import java.io.File
import java.net.URLDecoder

/**
 * Created by 11046 on 2017/12/9.
 * 文件下载handler
 */
class RequestDownloadHandler{
    private val ROOT_PATH = Environment.getExternalStorageDirectory().path    //根目录
    fun download(params:Map<String,String>,agent:String):String? {
        Log.i("Andserver","Params"+params.toString())
        val path = ROOT_PATH + URLDecoder.decode(params["path"],"utf-8")
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
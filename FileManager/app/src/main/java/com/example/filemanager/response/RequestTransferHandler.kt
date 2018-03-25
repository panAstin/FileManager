package com.example.filemanager.response

import android.util.Log
import com.example.filemanager.utils.FileUtil
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.URLDecoder

/**
 * Created by 11046 on 2017/12/4.
 * 文件上传Handler
 */
object RequestTransferHandler{
    /**
     * 获取同步文件
     */
    fun getsynclist(params: Map<String, String>):JSONObject?{
        Log.i("Nanoserver","Params"+params.toString())
        var result:JSONObject? = null
        val syncjson = URLDecoder.decode(params[""],"utf-8")
        val filejson = JSONObject(syncjson)
        val syncmap = FileUtil.getSyncFiles(filejson)
        if(!syncmap.isEmpty()){
            result = JSONObject(syncmap)
        }
        return result
    }
    /**
     * 上传文件
     */
    fun upload(params:Map<String,String>,files:Map<String,String>,mode: Int):Boolean {
        val uploadpath = params["path"]
        val targetdict = when (mode) {
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
                val targetpath = targetdict + entry.value
                if (File(targetpath).exists()) {
                    return false
                } else {
                    moveFile(tempFile, targetpath)
                }
            }
        }
        return true
    }

    /**
     * 上传同步文件
     */
    fun syncupload(params:Map<String,String>,files:Map<String,String>):Boolean{
        for (entry in params.entries) {
            val paramKey = entry.key
            Log.i("file", paramKey)
            if (paramKey == "uploadfile") {
                Log.i("file", entry.value)
                val tempFilepath = files[paramKey]
                Log.i("file", tempFilepath)
                val targetpath = FileUtil.SYNC_PATH + File.separator + entry.value
                try {
                    FileUtil.copy(tempFilepath!!,targetpath)
                }catch (e:IOException){
                    Log.e("syncupload","出错:"+e)
                    return false
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
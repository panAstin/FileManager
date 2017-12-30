package com.example.filemanager.response

import android.os.Environment
import android.util.Log
import com.example.filemanager.FileBean
import com.example.filemanager.FileType
import com.example.filemanager.utils.FileSortUtil
import com.example.filemanager.utils.FileUtil
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.URLDecoder

/**
 * Created by 11046 on 2017/12/6.
 * 列出文件handler
 */
class RequestListfileHandler{
    private val ROOT_PATH = Environment.getExternalStorageDirectory().path    //根目录
    fun listFiles(params:Map<String,String>):JSONArray? {
        Log.i("Nanoserver","Params"+params.toString())

        val path = ROOT_PATH + URLDecoder.decode(params["path"],"utf-8")
        val key = URLDecoder.decode(params["key"],"utf-8")
        val type = URLDecoder.decode(params["sort"],"utf-8")
        if(key=="" && type=="") {
            val file = File(path)
            return if (file.exists() && file.isDirectory) {
                getFile(file)
            } else {
                null
            }
        }else if (key==""){
            var sortfile = ArrayList<FileBean>()
            when(type){
                "image"->{
                    sortfile = FileSortUtil.mAllFiles[FileType.photo]!!
                }
                "music"->{
                    sortfile = FileSortUtil.mAllFiles[FileType.music]!!
                }
                "video"->{
                    sortfile = FileSortUtil.mAllFiles[FileType.video]!!
                }
            }
            return getFile(sortfile)
        } else{
            val results = FileUtil.FileSearch(key,path)
            return getFile(results)
        }
    }

    /**
     * 将文件转为json
     */
    private fun getFile(file:File):JSONArray{
        val files = file.listFiles()
        val js = JSONArray()
        files
                .filterNot {
                    it.isHidden
                }
                .forEach{
                    val fb = FileBean(it)
                    val jsonobj = JSONObject()
                    jsonobj.put("name",it.name)
                    jsonobj.put("type",fb.getTypeID().toString())
                    js.put(jsonobj)
                }
        return js
    }
    private fun getFile(filebean:ArrayList<FileBean>):JSONArray{
        val js = JSONArray()
        for(fb in filebean){
            val jsonobj = JSONObject()
            jsonobj.put("name",fb.getFile().name)
            jsonobj.put("type",fb.getTypeID().toString())
            js.put(jsonobj)
        }
        return js
    }
}
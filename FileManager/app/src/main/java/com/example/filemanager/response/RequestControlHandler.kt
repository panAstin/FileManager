package com.example.filemanager.response

import android.util.Log
import com.example.filemanager.ExFile
import com.example.filemanager.FileType
import com.example.filemanager.utils.FileSortUtil
import com.example.filemanager.utils.FileUtil
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.URLDecoder

/**
 *
 * Created by 11046 on 2018/3/12.
 */
object RequestControlHandler{
    /**
     * 获取文件请求
     */
    fun listFiles(params:Map<String,String>): JSONArray? {
        Log.i("Nanoserver","Params"+params.toString())

        val path = FileUtil.ROOT_PATH + URLDecoder.decode(params["path"],"utf-8")
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
            var sortfile = ArrayList<ExFile>()
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
    private fun getFile(file:File): JSONArray {
        val files = file.listFiles()
        val js = JSONArray()
        files
                .filterNot {
                    it.isHidden
                }
                .forEach{
                    val ef = ExFile(it.path)
                    val jsonobj = JSONObject()
                    jsonobj.put("name",it.name)
                    jsonobj.put("type",ef.getTypeID().toString())
                    jsonobj.put("path",it.path.replace(FileUtil.ROOT_PATH,""))
                    js.put(jsonobj)
                }
        return js
    }

    private fun getFile(filebean:ArrayList<ExFile>): JSONArray {
        val js = JSONArray()
        for(fb in filebean){
            val jsonobj = JSONObject()
            jsonobj.put("name",fb.name)
            jsonobj.put("type",fb.getTypeID().toString())
            jsonobj.put("path",fb.path.replace(FileUtil.ROOT_PATH,""))
            js.put(jsonobj)
        }
        return js
    }

    /**
     * 重命名请求
     */
    fun rename(params:Map<String,String>):Boolean {
        Log.i("Andserver","Params"+params.toString())
        val path = FileUtil.ROOT_PATH + URLDecoder.decode(params["path"],"utf-8")
        val oldname = URLDecoder.decode(params["oldfile"],"utf-8")
        val newname = URLDecoder.decode(params["newfile"],"utf-8")
        val oldfile = File(path + File.separator + oldname)
        val newfile = File(path + File.separator + newname)
        if(FileUtil.renameFile(oldfile,newfile)){
            return true
        }
        return false
    }

    /**
     * 删除请求
     */
    fun delete(params:Map<String,String>):Boolean {
        Log.i("Andserver","Params"+params.toString())
        val path = FileUtil.ROOT_PATH + URLDecoder.decode(params["path"],"utf-8")
        val fnames = URLDecoder.decode(params["filenames"],"utf-8")
        val fnamelist:List<String> = fnames.split("/")
        var f:File
        for (fname in fnamelist){
            f = File(path + File.separator +fname)
            if(!FileUtil.deleteFile(f)){
                return false
            }
        }
        return true
    }

}
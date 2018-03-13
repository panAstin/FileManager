package com.example.filemanager.response

import android.util.Log
import com.example.filemanager.utils.FileUtil
import java.io.File
import java.net.URLDecoder

/**
 *
 * Created by 11046 on 2018/3/12.
 */
class RequestControlHandler{
    fun rename(params:Map<String,String>):Boolean {
        Log.i("Andserver","Params"+params.toString())
        val path = FileUtil.ROOT_PATH + URLDecoder.decode(params["path"],"utf-8")
        val oldname = URLDecoder.decode(params["oldfile"],"utf-8")
        val newname = URLDecoder.decode(params["newfile"],"utf-8")
        val oldfile = File(path + File.separator + oldname)
        val newfile = File(path + File.separator + newname)
        if(FileUtil.renameFile(null,oldfile,newfile)){
            return true
        }
        return false
    }

    fun delete(params:Map<String,String>):Boolean {
        Log.i("Andserver","Params"+params.toString())
        val path = FileUtil.ROOT_PATH + URLDecoder.decode(params["path"],"utf-8")
        val fnames = URLDecoder.decode(params["filenames"],"utf-8")
        val fnamelist:List<String> = fnames.split("/")
        var f:File
        for (fname in fnamelist){
            f = File(path + File.separator +fname)
            if(!FileUtil.deleteFile(null,f)){
                return false
            }
        }
        return true
    }

}
package com.example.filemanager.response

import android.os.Environment
import android.util.Log
import com.example.filemanager.FileBean
import com.example.filemanager.FileType
import com.example.filemanager.utils.FileSortUtil
import com.example.filemanager.utils.FileUtil
import com.yanzhenjie.andserver.RequestHandler
import com.yanzhenjie.andserver.util.HttpRequestParser
import org.apache.http.HttpRequest
import org.apache.http.HttpResponse
import org.apache.http.entity.StringEntity
import org.apache.http.protocol.HttpContext
import java.io.File
import java.net.URLDecoder

/**
 * Created by 11046 on 2017/12/6.
 * 列出文件handler
 */
class RequestListfileHandler:RequestHandler{
    val ROOT_PATH = Environment.getExternalStorageDirectory().path    //根目录
    override fun handle(request: HttpRequest?, response: HttpResponse?, context: HttpContext?) {
        val params = HttpRequestParser.parse(request)
        Log.i("Andserver","Params"+params.toString())

        val path = ROOT_PATH + URLDecoder.decode(params["path"],"utf-8")
        val key = URLDecoder.decode(params["key"],"utf-8")
        val type = URLDecoder.decode(params["sort"],"utf-8")
        if(key=="" && type=="") {
            val file = File(path)
            if (file.exists() && file.isDirectory) {
                val se = StringEntity(listFile(file), "utf-8")
                se.setContentType("text/json")
                response?.setStatusCode(200)
                response?.entity = se
            } else {
                response?.setStatusCode(500)
                response?.entity = StringEntity("Wrong path!", "utf-8")
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
            val se = StringEntity(listFile(sortfile), "utf-8")
            se.setContentType("text/json")
            response?.setStatusCode(200)
            response?.entity = se
        } else{
            val results = FileUtil.FileSearch(key,path)
            val se = StringEntity(listFile(results), "utf-8")
            se.setContentType("text/json")
            response?.setStatusCode(200)
            response?.entity = se
        }
    }

    /**
     * 将文件转为json
     */
    private fun listFile(file:File):String{
        val files = file.listFiles()
        val js = StringBuilder()
        js.append("[")
        files
                .filterNot {
                    it.isHidden
                }
                .forEach{
                    val fb = FileBean(it)
                    js.append("{\"name\":\""+it.name+"\",\"type\":\""+ fb.getTypeID()+"\"},")
                }
        js.deleteCharAt(js.lastIndex)
        if(js.last()==','){
            js.deleteCharAt(js.lastIndex)
        }
        js.append("]")
        return js.toString()
    }
    private fun listFile(filebean:ArrayList<FileBean>):String{
        val js = StringBuilder()
        js.append("[")
        for(fb in filebean){
            js.append("{\"name\":\""+fb.getFile().name+"\",\"type\":\""+ fb.getTypeID()+"\"},")
        }
        if(js.last()==','){
            js.deleteCharAt(js.lastIndex)
        }
        js.append("]")
        return js.toString()
    }
}
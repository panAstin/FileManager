package com.example.filemanager

import android.content.res.AssetManager
import android.os.Environment
import android.util.Log
import com.example.filemanager.response.RequestDownloadHandler
import com.example.filemanager.response.RequestListfileHandler
import com.example.filemanager.response.RequestUploadHandler
import com.example.filemanager.utils.FileUtil
import fi.iki.elonen.NanoHTTPD
import org.json.JSONObject
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream

/**
 * Created by 11046 on 2017/12/26.
 * 服务器
 */
class HttpServer(private val asset_mgr:AssetManager,private val mode:Int,port:Int):NanoHTTPD(port){
    companion object {
        val MIME_JS = "application/javascript"
        val MIME_JSON = "text/json"
        val MIME_CSS = "text/css"
        val MIME_PNG = "image/png"
        val MIME_DEFAULT_BINARY = "application/octet-stream"
        val MIME_XML = "text/xml"
    }
    var instream:InputStream? = null

    override fun serve(session: IHTTPSession?): Response {
        var uri = session?.uri?.substring(1) //去除开头'/'
        Log.i("uri",uri)
        val method = session?.method
        val files:Map<String,String> = HashMap()
        var filepath = ""
        val agent = session?.headers?.get("USER-AGENT").toString()
        if(Method.POST == method||Method.PUT == method){
            try {
                session.parseBody(files)   //必要，不然无法获取parms
            }catch (ioe:IOException){
                return Response("Internal Error IO Exception: " + ioe.message)
            }catch (re:ResponseException){
                return Response(re.status, MIME_PLAINTEXT,re.message)
            }
        }
        if(uri!!.contains("openfile")){
            val index = uri.indexOf("/")
            filepath = uri.substring(index,uri.length)
            uri = uri.substring(0,index)
        }
        try {
            when(uri){
                "listfile" ->{           //文件展示
                    val jsonarr = RequestListfileHandler().listFiles(session!!.parms)
                    return if(jsonarr != null){
                        Response(Response.Status.OK, MIME_JSON,jsonarr.toString())
                    }else{
                        Response(Response.Status.NOT_FOUND, MIME_PLAINTEXT,"出现错误")
                    }
                }
                "upload" ->{    //文件上传
                    if(RequestUploadHandler(mode).upload(session!!.parms,files)){
                        val ja = JSONObject()
                        ja.put("message","Success")
                        return Response(Response.Status.OK, MIME_JSON,ja.toString())
                    }else{
                        throw IOException("上传出错")
                    }
                }
                "openfile" -> {
                    val file = File(Environment.getExternalStorageDirectory().path + filepath)
                    if (file.exists()){
                        Log.i("path",file.path)
                        val mimetype = FileUtil.getMIMEType(file)
                        return Response(Response.Status.OK,mimetype, file.inputStream())
                    }else{
                        throw FileNotFoundException("文件未找到")
                    }
                }
                "download" -> {   //文件下载
                    val path = RequestDownloadHandler().download(session!!.parms,agent)
                    if(path != null){
                        val response = Response(Response.Status.OK,"multipart/form-data", File(path).inputStream())
                        response.addHeader("Content-disposition", String.format("attachment; filename=\"%s\"", File(path).name))
                        File(path).delete()
                        return response
                    }else{
                        throw FileNotFoundException("下载出错")
                    }
                }

                "" -> {          //主页
                    instream = asset_mgr.open("web/index.html")
                    return Response(Response.Status.OK, MIME_HTML,instream)
                }
                else -> {
                    when {
                        uri.contains("js") -> {
                            instream = asset_mgr.open(uri)
                            return Response(Response.Status.OK, MIME_JS ,instream)
                        }
                        uri.contains("css") -> {
                            instream = asset_mgr.open(uri)
                            return Response(Response.Status.OK, MIME_CSS ,instream)
                        }
                        uri.contains("png") -> {
                            instream = asset_mgr.open(uri)
                            return Response(Response.Status.OK, MIME_PNG ,instream)
                        }
                        else -> {
                            instream = asset_mgr.open(uri)
                            return Response(Response.Status.OK, MIME_DEFAULT_BINARY ,instream)
                        }
                    }
                }
            }
        }catch (e:IOException){
            e.printStackTrace()
        }
        return Response("出现错误")
    }
}
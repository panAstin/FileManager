package com.example.filemanager.utils

import android.util.Log
import okhttp3.*
import java.io.IOException
import okhttp3.RequestBody
import okhttp3.OkHttpClient
import java.io.File
import java.io.FileOutputStream
import okhttp3.MultipartBody
import org.json.JSONObject


/**
 * Created by 11046 on 2018/3/18.
 * Http客户端
 */
class HttpClientUtil{
    private val JSON_TYPE = MediaType.parse("application/json; charset=utf-8")

    /**
     * Get请求
     */
    private fun getAsynHttp(url:String,callback: Callback) {
        val mOkHttpClient = OkHttpClient()
        val requestBuilder = Request.Builder().url(url)
        //可以省略，默认是GET请求
        requestBuilder.method("GET", null)
        val request = requestBuilder.build()
        val mcall = mOkHttpClient.newCall(request)
        mcall.enqueue(callback)
    }

    /**
     * Post请求
     */
    private fun postAsynHttp(request: Request,callback: Callback) {
        val mOkHttpClient = OkHttpClient()
        val call = mOkHttpClient.newCall(request)
        call.enqueue(callback)
    }

    fun getSyncList(url: String,jsonObject: JSONObject):JSONObject{
        val mbody = RequestBody.create(JSON_TYPE,jsonObject.toString())
        val requet = Request.Builder()
                .url(url+ "/getsyncfiles")
                .post(mbody)
                .build()
        var result = JSONObject()
        postAsynHttp(requet,object :Callback{
            override fun onResponse(call: Call?, response: Response) {
                Log.i("FileSync","获取同步文件成功")
                val filesjson = response.body()?.string()
                result = JSONObject(filesjson)
            }

            override fun onFailure(call: Call?, e: IOException?) {
                Log.e("FileSync","获取同步文件失败  "+e)
            }

        })
        return result
    }

    /**
     * 上传文件
     */
    fun postAsynFile(url: String,filelist:ArrayList<File>) {
        val mbody = MultipartBody.Builder().setType(MultipartBody.FORM)
        var i = 1
        for (file in filelist){
            if(file.exists()){
                Log.i("file",file.name)
                mbody.addFormDataPart("file" + i,file.name, RequestBody.create(
                        MediaType.parse(FileUtil.getMIMEType(file)+"; charset=utf-8"),file))
                i++
            }
        }
        val request = Request.Builder()
                .url(url)
                .post(mbody.build())
                .build()
        postAsynHttp(request,object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("FileSync","发送同步文件失败  "+e)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                Log.i("FileSync","发送请求成功")
                val jsonstr = response.body()?.string()
                Log.i("FileSync",jsonstr)
                val jsonobject = JSONObject(jsonstr)
                if(jsonobject["message"]=="Success"){
                    Log.i("FileSync","发送同步文件成功")
                }else{
                    Log.e("FileSync","发送同步文件失败")
                }
            }
        })
    }

    /**
     * 下载文件
     */
    fun downloadAsynFile(url: String,file: File) {
        getAsynHttp(url,object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("syncfile", "下载请求失败:" + e)
            }

            override fun onResponse(call: Call, response: Response) {
                val inputStream = response.body()!!.byteStream()
                val fileOutputStream: FileOutputStream?
                try {
                    fileOutputStream = FileOutputStream(file)
                    val buffer = ByteArray(2048)
                    var len = inputStream.read(buffer)
                    while (len != -1) {
                        fileOutputStream.write(buffer, 0, len)
                        len = inputStream.read(buffer)
                    }
                    fileOutputStream.flush()
                } catch (e: IOException) {
                    Log.i("syncfile", "IOException")
                    e.printStackTrace()
                }

                Log.d("syncfile", "文件下载成功")
            }
        })
    }

    /**
     * 多类型请求
     */
    fun sendMultipart(url: String,jsonObject: JSONObject) {
        val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("title", "wangshu")
                .addFormDataPart("image", "wangshu.jpg",
                        RequestBody.create(MediaType.parse(""), File("")))
                .build()

        val request = Request.Builder()
                .header("Authorization", "Client-ID " + "...")
                .url(url)
                .post(requestBody)
                .build()

        postAsynHttp(request,object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                Log.i("wangshu", response.body()!!.string())
            }
        })
    }
}
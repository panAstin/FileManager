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
    var mOkHttpClient:OkHttpClient? = null
    val JSON_TYPE = "application/json"

    /**
     * Get请求
     */
    private fun getAsynHttp(url:String,callback: Callback) {
        mOkHttpClient = OkHttpClient()
        val requestBuilder = Request.Builder().url(url)
        //可以省略，默认是GET请求
        requestBuilder.method("GET", null)
        val request = requestBuilder.build()
        val mcall = mOkHttpClient!!.newCall(request)
        mcall.enqueue(callback)
    }

    /**
     * Post请求
     */
    private fun postAsynHttp(request: Request,callback: Callback) {
        mOkHttpClient = OkHttpClient()
        val call = mOkHttpClient!!.newCall(request)
        call.enqueue(callback)
    }

    private fun getSyncList(url: String,jsonObject: JSONObject){
        val mbody = RequestBody.create(MediaType.parse(JSON_TYPE+"; charset=utf-8"),jsonObject.toString())
        val requet = Request.Builder()
                .url(url)
                .post(mbody)
                .build()
        postAsynHttp(requet,object :Callback{
            override fun onResponse(call: Call?, response: Response?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onFailure(call: Call?, e: IOException?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        })
    }

    /**
     * 上传文件
     */
    private fun postAsynFile(url: String,filelist:ArrayList<File>) {
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

            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                Log.i("wangshu", response.body()?.string())
            }
        })
    }

    /**
     * 下载文件
     */
    private fun downloadAsynFile() {
        val url = "http://img.my.csdn.net/uploads/201603/26/1458988468_5804.jpg"
        getAsynHttp(url,object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {
                val inputStream = response.body()!!.byteStream()
                val fileOutputStream: FileOutputStream?
                try {
                    fileOutputStream = FileOutputStream(File("/sdcard/wangshu.jpg"))
                    val buffer = ByteArray(2048)
                    var len = inputStream.read(buffer)
                    while (len != -1) {
                        fileOutputStream.write(buffer, 0, len)
                        len = inputStream.read(buffer)
                    }
                    fileOutputStream.flush()
                } catch (e: IOException) {
                    Log.i("wangshu", "IOException")
                    e.printStackTrace()
                }

                Log.d("wangshu", "文件下载成功")
            }
        })
    }

    /**
     * 多类型请求
     */
    private fun sendMultipart(url: String,jsonObject: JSONObject) {
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
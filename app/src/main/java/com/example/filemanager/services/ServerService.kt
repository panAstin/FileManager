package com.example.filemanager.services

import android.app.Service
import android.content.Intent
import android.content.res.AssetManager
import android.util.Log
import com.example.filemanager.HttpServer
import com.example.filemanager.activities.MainActivity
import com.example.filemanager.receivers.ServerStatusReceiver

/**
 * Created by 11046 on 2017/11/27.
 * 服务器service
 */
class ServerService: Service() {
    private var mServer:HttpServer? = null  //AndServer
    private var mAssetManager:AssetManager? = null

    override fun onCreate() {
        mAssetManager = assets
        val port = MainActivity.CONFIG["port"]!!.toInt()
        val savemode = MainActivity.CONFIG["mode"]!!.toInt()

        mServer = HttpServer(mAssetManager!!,savemode,port)  //服务器
    }

    override fun onBind(intent: Intent?) = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startServer()    //启动服务器

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        stopServer()    //停止服务器

        if(mAssetManager!=null){
            mAssetManager?.close()
        }
    }

    /**
     * 启动服务器
     */
    private fun startServer(){
        if(mServer != null){
            if(mServer!!.wasStarted()){
                serverHasStarted()
            }
            else{
                mServer?.start()
                serverStart()
                Log.i("service","start")
            }
        }
    }

    /**
     * 停止服务器
     */
    private fun stopServer(){
        if(mServer != null){
            mServer?.stop()
            serverStop()
            Log.i("service","stop")
        }
    }
    /**
     * 通知 服务器启动
     */
    private fun serverStart(){
        sendBroadcast(ServerStatusReceiver.STATUS_VALUE_START)
    }

    /**
     *通知 服务器已启动
     */
    private fun serverHasStarted(){
        sendBroadcast(ServerStatusReceiver.STATUS_VALUE_STARTED)
    }

    /**
     *通知 服务器已停止
     */
    private fun serverStop(){
        sendBroadcast(ServerStatusReceiver.STATUS_VALUE_STOP)
    }

    /**
     * 发送广播
     */
    private fun sendBroadcast(status:Int){
        val broadcast = Intent(ServerStatusReceiver.ACTION)
        broadcast.putExtra(ServerStatusReceiver.STATUS_KEY,status)
        this.sendBroadcast(broadcast)
    }
}
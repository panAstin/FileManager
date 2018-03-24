package com.example.filemanager.utils

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import com.example.filemanager.activities.MainActivity
import com.example.filemanager.receivers.ServerStatusReceiver
import com.example.filemanager.services.ServerService
import java.net.Inet6Address
import java.net.NetworkInterface
import java.net.SocketException

/**
 * Created by 11046 on 2017/11/22.
 * 局域网服务器功能
 */
class ServerUtil(var context: Context?){
    companion object {
        var ip = getIpaddr()           //当前IP
        var SWITCH = false         //服务器运行状态标识
        /**
         * 获取IP
         */
        private fun getIpaddr():String{
            try {
                val ntf = NetworkInterface.getNetworkInterfaces()
                while (ntf.hasMoreElements()){
                    val intf = ntf.nextElement()
                    val enumIpAddr = intf.inetAddresses
                    while (enumIpAddr.hasMoreElements()){
                        val ia = enumIpAddr.nextElement()
                        if(ia is Inet6Address){
                            continue
                        }
                        ip = ia.hostAddress
                        if ("127.0.0.1" != ip) {
                            return ia.hostAddress
                        }
                    }
                }
            }catch (ex:SocketException){
                ex.printStackTrace()
            }
            return ""
        }

        /**
         * 判断是否为IP+port
         */
        fun isIP(str:String):Boolean{
            val rexp = "((?:(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))\\.){3}(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?[1-9])))(:\\d)*)"
            return  str.matches(Regex(rexp))
        }
    }

    private var mService:Intent? = null
    private var mReceiver:ServerStatusReceiver? = null

    /**
     * 初始化服务器页面
     */
    init {
        //Server 运行服务
        mService = Intent(context,ServerService::class.java)
        mReceiver = ServerStatusReceiver(this,context as MainActivity)
        mReceiver?.register()
    }

    /**
     * 获取service
     */
    fun getservice() = mService

    /**
     * 启动服务器
     */
    fun serverStart(){
        SWITCH = true
    }

    /**
     * 服务器已启动
     */
    fun serverStarted(){

    }
    /**
     * 停止服务器
     */
    fun serverStop(){
        SWITCH = false
    }

    /**
     * 获取wifi是否连接
     */
    fun ifWifiAvailabel():Boolean{
        val wifiManager = context?.applicationContext?.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return wifiManager.isWifiEnabled
    }

    fun destroy(){
        context = null
        mReceiver?.unRegister()
    }
}
package com.example.filemanager.services

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.*
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.example.filemanager.receivers.WifiDirectReceiver
import java.io.IOException
import java.net.InetAddress
import java.net.ServerSocket

/**
 * Created by 11046 on 2018/2/12.
 * wifidirect服务
 */
class WifiDirectService:IntentService("Wifip2pIntentService"),WifiP2pManager.PeerListListener,WifiP2pManager.ConnectionInfoListener{
    private var mManager:WifiP2pManager? = null
    private var mChannel:WifiP2pManager.Channel? = null
    private var mFilter:IntentFilter? = null
    private var mWifiDirectReceiver:WifiDirectReceiver? = null

    private var connected = false

    override fun onCreate() {
        super.onCreate()

        mFilter = IntentFilter()
        //指示wifi p2p的状态变化
        mFilter?.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        //指示可用节点列表的变化*
        mFilter?.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        //指示连接状态的变化
        mFilter?.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        //指示当前设备发生变化
        mFilter?.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)

        //初始化wifi p2p的控制器
        mManager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        mChannel = mManager?.initialize(this, Looper.getMainLooper(),null)

        mWifiDirectReceiver = WifiDirectReceiver(this,mManager!!)
        registerReceiver(mWifiDirectReceiver,mFilter)
    }

    override fun onHandleIntent(intent: Intent?) {
        //开启设备发现
        mManager?.discoverPeers(mChannel,object:WifiP2pManager.ActionListener{
            override fun onFailure(reason: Int) {
                Log.e("wifip2p","搜索失败-->"+reason)
            }

            override fun onSuccess() {
                Log.e("wifip2p","搜索成功")
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()

        //注销广播
        unregisterReceiver(mWifiDirectReceiver)
        if (connected){
            mManager?.removeGroup(mChannel,object:WifiP2pManager.ActionListener{
                override fun onFailure(reason: Int) {
                    Log.e("wifip2p","移除失败-->"+reason)
                }

                override fun onSuccess() {
                    Log.e("wifip2p","移除成功")
                }
            })
        }
    }

    fun setIsWifiDirectEnable(enabled:Boolean){
        //设备是否支持Wi-Fi Direct或者打开开关，通知一下
        if(!enabled){

        }
    }

    fun getChannel() = mChannel

    //连接设备
    fun connectDevice(config: WifiP2pConfig){
        mManager?.connect(mChannel,config,object:WifiP2pManager.ActionListener{
            override fun onFailure(reason: Int) {
                Log.e("wifip2p","connect failure->"+reason)
            }

            override fun onSuccess() {
                Log.e("wifip2p","connect success")
            }
        })
    }

    override fun onConnectionInfoAvailable(info: WifiP2pInfo?) {
        var address:InetAddress? = null
        var isGroupOwner = false
        if (info!!.groupFormed && info.isGroupOwner){
            Log.i("wifip2p","server")
            address = info.groupOwnerAddress
            isGroupOwner = true
        }else if(info.groupFormed){
            Log.i("wifip2p","client")
            address = info.groupOwnerAddress
            isGroupOwner = false
        }
        if(null != address){
            connected = true
        }
    }

    fun onConnectDisabled(){
        connected = false
    }

    //发现周围设备
    override fun onPeersAvailable(peers: WifiP2pDeviceList?) {
        val config = WifiP2pConfig()
        config.deviceAddress = peers!!.deviceList.first().deviceAddress
        config.wps.setup = WpsInfo.PBC
        connectDevice(config)
    }

    override fun onBind(intent: Intent?): IBinder {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun FilesyncServer(){
        try {
            val serverSocket = ServerSocket(8988)
            val client = serverSocket.accept()
        }catch (e:IOException){
            Log.e("wifip2p",e.message)
        }


    }
}
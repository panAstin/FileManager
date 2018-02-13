package com.example.filemanager.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pManager
import com.example.filemanager.services.WifiDirectService

/**
 * Created by 11046 on 2018/2/12.
 * wifidirect广播接收器
 */

class WifiDirectReceiver() : BroadcastReceiver() {
    private var service: WifiDirectService? = null
    private var manager: WifiP2pManager? = null

    constructor(service: WifiDirectService,manager: WifiP2pManager):this(){
        this.service = service
        this.manager = manager
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION == action){
            //判断wifi p2p是否可用
            val state=intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE,-1)
            if(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED){
                service?.setIsWifiDirectEnable(true)
            }else {
                service?.setIsWifiDirectEnable(false)
            }
        }else if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION == action){
            //可用设备列表发生变化
            manager?.requestPeers(service?.getChannel(),service)
        }else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION == action){
            //连接状态发生改变
            val info:NetworkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO)
            if (info.isConnected){
                manager?.requestConnectionInfo(service?.getChannel(),service)
            }else{
                service?.onConnectDisabled()
            }
        }else if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION == action){
            //当前设备发生变化
        }
    }

}
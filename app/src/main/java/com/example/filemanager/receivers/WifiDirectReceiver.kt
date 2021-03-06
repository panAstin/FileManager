package com.example.filemanager.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log
import com.example.filemanager.activities.MainActivity

/**
 * Created by 11046 on 2018/2/12.
 * wifidirect广播接收器
 */

class WifiDirectReceiver() : BroadcastReceiver() {
    private var activity: MainActivity? = null
    private var manager: WifiP2pManager? = null

    constructor(activity: MainActivity,manager: WifiP2pManager):this(){
        this.activity = activity
        this.manager = manager
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION == action){
            Log.e("wifip2p","state changed")
            //判断wifi p2p是否可用
            val state=intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE,-1)
            if(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED){
                activity?.setIsWifiDirectEnable(true)
            }else {
                activity?.setIsWifiDirectEnable(false)
            }
        }else if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION == action){
            Log.e("wifip2p","peers changed")
            //可用设备列表发生变化
            manager?.requestPeers(activity?.getChannel(),activity?.peerListListener)
        }else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION == action){
            Log.e("wifip2p","connection changed")
            //连接状态发生改变
            val info:NetworkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO)
            if (info.isConnected){
                manager?.requestConnectionInfo(activity?.getChannel(),activity?.connectionInfoListener)
            }else{
                activity?.onConnectDisabled()
            }
        }else if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION == action){
            //当前设备发生变化
        }
    }

}
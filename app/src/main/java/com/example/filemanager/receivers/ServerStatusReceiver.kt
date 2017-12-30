package com.example.filemanager.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.example.filemanager.activities.MainActivity
import com.example.filemanager.fragments.ServerFragment

/**
 * Created by 11046 on 2017/11/28.
 * 服务器状态广播接收器
 */
class ServerStatusReceiver(serverFragment: ServerFragment,mainActivity: MainActivity) : BroadcastReceiver() {
    companion object {
        val ACTION = "com.example.filemanager.receiver"
        val STATUS_KEY = "STATUS_KEY"
        val STATUS_VALUE_START = 1
        val STATUS_VALUE_STARTED = 2
        val STATUS_VALUE_STOP = 3
    }

    private var mActivity:MainActivity = mainActivity
    private var mServerFragment = serverFragment

    /**
     * 注册广播接收器
     */
    fun register(){
        val filter = IntentFilter(ACTION)
        mActivity.registerReceiver(this,filter)
    }

    /**
     * 注销广播接收器
     */
    fun unRegister(){
        mActivity.unregisterReceiver(this)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        if (ACTION == action){
            val status = intent.getIntExtra(STATUS_KEY,0)
            when(status){
                STATUS_VALUE_START ->{
                    mServerFragment.serverStart()
                }
                STATUS_VALUE_STARTED ->{
                    mServerFragment.serverStarted()
                }
                STATUS_VALUE_STOP -> {
                    mServerFragment.serverStop()
                }
            }
        }
    }
}
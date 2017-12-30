package com.example.filemanager.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.example.filemanager.R
import com.example.filemanager.activities.MainActivity
import com.example.filemanager.receivers.ServerStatusReceiver
import com.example.filemanager.services.ServerService
import com.example.filemanager.utils.SnackbarUtil
import org.jetbrains.anko.find
import java.net.Inet6Address
import java.net.NetworkInterface
import java.net.SocketException

/**
 * Created by 11046 on 2017/11/22.
 * 局域网服务器功能
 */
class ServerFragment : Fragment(){
    private var ip:String = ""           //当前IP
    private var SWITCH = false         //服务器运行状态标识

    private var server_btn:Button? = null
    private var setting_btn:Button? = null
    private var address_tv:TextView? = null

    private var mService:Intent? = null
    private var mReceiver:ServerStatusReceiver? = null

    fun newInstnace():ServerFragment{
        val args = Bundle()
        val serverFragment = ServerFragment()
        serverFragment.arguments = args
        return serverFragment
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val serverview:View? = inflater?.inflate(R.layout.server_layout,container,false)
        initServer(serverview!!)
        return serverview
    }

    /**
     * 初始化服务器页面
     */
    private fun initServer(view: View){
        server_btn = view.find(R.id.server_btn)
        server_btn?.setOnClickListener({
            if(SWITCH){
                context.stopService(mService)
            }else{
                context.startService(mService)
            }
        })
        setting_btn = view.find(R.id.setting_btn)
        setting_btn?.setOnClickListener({
            val dialog = SettingDialogFragment()
            dialog.show(activity.fragmentManager,"SettingDialogFragment")
        })
        address_tv = view.find(R.id.address_tv)

        //Server 运行服务
        mService = Intent(context,ServerService::class.java)
        mReceiver = ServerStatusReceiver(this,context as MainActivity)
        mReceiver?.register()
    }

    /**
     * 启动服务器
     */
    fun serverStart(){
        SWITCH = true
        updateUI()
    }

    /**
     * 服务器已启动
     */
    fun serverStarted(){
        SnackbarUtil.short(view!!,"服务器已启动")
    }
    /**
     * 停止服务器
     */
    fun serverStop(){
        SWITCH = false
        updateUI()
    }

    /**
     * 更新UI
     */
    private fun updateUI(){
        when(SWITCH){
            true ->{
                val ipformat = getString(R.string.httpadd)
                getIp()
                if(!TextUtils.isEmpty(ip)){
                    val port = context.getSharedPreferences("ServerSetting", Context.MODE_PRIVATE).getInt("port",9090)
                    address_tv?.text = String.format(ipformat,ip,port)
                }
                server_btn?.text = getString(R.string.stopserver)
            }
            false ->{
                address_tv?.text = getText(R.string.noserver)
                server_btn?.text = getString(R.string.startserver)
            }
        }
    }
    /**
     * 获取IP
     */
    private fun getIp(){
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
                    if (!"127.0.0.1".equals(ip)) {
                        ip = ia.hostAddress
                        break
                    }
                }
            }
        }catch (ex:SocketException){
            ex.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mReceiver?.unRegister()
    }
}
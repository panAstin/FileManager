package com.example.filemanager.activities

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.*
import android.support.design.widget.TabLayout
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.*
import org.jetbrains.anko.setContentView

import android.view.WindowManager
import org.jetbrains.anko.find
import com.example.filemanager.adapters.FragmentPageAdapter
import UI.MainActivityUI
import android.content.*
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pManager
import android.support.v4.app.Fragment
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.example.filemanager.FileAsyncClient
import com.example.filemanager.FileAsyncServer
import com.example.filemanager.utils.FileSortUtil
import com.example.filemanager.R
import com.example.filemanager.fragments.FileListFragment
import com.example.filemanager.utils.ServerUtil
import com.example.filemanager.fragments.SettingDialogFragment
import com.example.filemanager.receivers.WifiDirectReceiver
import com.example.filemanager.utils.SnackbarUtil
import java.net.InetAddress
import kotlin.collections.ArrayList
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    companion object {
        var CONFIG = HashMap<String,String>()
    }
    private val REQUEST_CODE = 11
    private var tablayout: TabLayout? = null
    private var viewpager: ViewPager? = null
    private var mDrawerLayout:DrawerLayout? = null
    private var mDrawerToggle:ActionBarDrawerToggle? = null
    private var toolbar:Toolbar? = null
    private var address_tv:TextView? = null
    private var mManager:WifiP2pManager? = null
    private var mChannel:WifiP2pManager.Channel? = null
    private var mFilter:IntentFilter? = null
    private var mWifiDirectReceiver:WifiDirectReceiver? = null
    private var connected = false
    //申请权限
    private val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.INTERNET,Manifest.permission.ACCESS_WIFI_STATE,Manifest.permission.CHANGE_WIFI_STATE)
    private var serverUtil:ServerUtil? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MainActivityUI().setContentView(this)
        //状态栏沉浸
        //if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.TRANSPARENT
            window.navigationBarColor = Color.TRANSPARENT
        //}

        checkPermissions()
        initConf()
        if(CONFIG["synflag"]!!.toBoolean()){
            doFileAsync()
        }
    }

    /**
     * 控件初始化
     */
    private fun initview(){
        toolbar = find(R.id.toolbar)
        mDrawerLayout = find(R.id.dl_left)
        tablayout = find(R.id.tablayout)
        viewpager = find(R.id.viewpager)
        address_tv = find(R.id.address_tv)

        setSupportActionBar(toolbar)
        mDrawerToggle = ActionBarDrawerToggle(this,mDrawerLayout,toolbar,R.string.open,R.string.close)
        mDrawerToggle?.syncState()
        mDrawerLayout?.addDrawerListener(mDrawerToggle!!)
        tablayout?.tabMode=TabLayout.MODE_FIXED
        tablayout?.tabGravity=TabLayout.GRAVITY_CENTER
        val fragmentpageadapter= FragmentPageAdapter(supportFragmentManager)
        viewpager?.adapter =fragmentpageadapter
        viewpager?.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tablayout))
        tablayout?.setupWithViewPager(viewpager)
        serverUtil = ServerUtil(this)
    }

    /**
     * 初始化设置信息
     */
    private fun initConf(){
        val preferences = getSharedPreferences("ServerSetting", Context.MODE_PRIVATE)
        CONFIG["mode"] = preferences.getInt("mode",0).toString()
        CONFIG["synflag"] = preferences.getBoolean("synflag",false).toString()
        CONFIG["port"] = preferences.getInt("port",9090).toString()
    }

    /**
     * 检查权限
     */
    private fun checkPermissions() {
         val mPermissionList = ArrayList<String>()
        //检查权限
        permissions
                .filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
                .forEach { mPermissionList.add(it) }
        if (mPermissionList.isEmpty()) {
            initview()
        } else {
            //进入到这里代表没有权限.
            val mpermissions = mPermissionList.toTypedArray()
            ActivityCompat.requestPermissions(this, mpermissions, REQUEST_CODE)
        }
    }

    /**
     * 确认授权
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE) {
            if (grantResults.isNotEmpty()&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //用户同意授权
                refresh()
            } else {
                //用户拒绝授权
                SnackbarUtil.short(window.decorView,"已拒绝授权")
            }
        }
    }

    //刷新
    private fun refresh() {
        finish()
        val intent =  Intent()
        intent.setClass(this,this::class.java)
        startActivity(intent)
    }

    //获取当前页面
    private fun getVisibleFragment(): Fragment? {
        val fragments = this@MainActivity.supportFragmentManager.fragments
        return fragments.firstOrNull { it != null && it.isVisible }
    }

    //开启wifi direct进行文件同步
    private fun doFileAsync(){
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
        //开启设备发现
        mManager?.discoverPeers(mChannel,object: WifiP2pManager.ActionListener{
            override fun onFailure(reason: Int) {
                Log.e("wifip2p","搜索失败-->"+reason)
            }

            override fun onSuccess() {
                Log.e("wifip2p","搜索成功")
            }
        })

        mWifiDirectReceiver = WifiDirectReceiver(this,mManager!!)
        registerReceiver(mWifiDirectReceiver,mFilter)
    }

    fun setIsWifiDirectEnable(enabled:Boolean){
        //设备是否支持Wi-Fi Direct或者打开开关，通知一下
        if(!enabled){

        }
    }

    fun getChannel() = mChannel

    fun onConnectDisabled(){
        connected = false
    }

    val peerListListener = WifiP2pManager.PeerListListener { peers ->
        //发现周围设备
        val config = WifiP2pConfig()
        if(!peers!!.deviceList.isEmpty()){
            for(device in peers.deviceList){
                Log.e("wifip2p",device.toString())
                config.deviceAddress = device.deviceAddress
                config.wps.setup = WpsInfo.PBC
                mManager?.connect(mChannel,config,object:WifiP2pManager.ActionListener{
                    override fun onFailure(reason: Int) {
                        Log.e("wifip2p","connect failure->"+reason)
                    }

                    override fun onSuccess() {
                        Log.e("wifip2p","connect success")
                        connected = true
                    }
                })
                if (connected){
                    break
                }
            }
        }else{
            Log.e("wifip2p","No devices found")
        }
    }

    val connectionInfoListener = WifiP2pManager.ConnectionInfoListener { info ->
        var address: InetAddress? = null
        var isGroupOwner = false
        if (info!!.groupFormed && info.isGroupOwner){
            Log.i("wifip2p","server")
            address = info.groupOwnerAddress
            isGroupOwner = true
            thread {
                FileAsyncServer()
            }.start()
        }else if(info.groupFormed){
            Log.i("wifip2p","client")
            address = info.groupOwnerAddress
            isGroupOwner = false
            thread {
                FileAsyncClient(address)
            }.start()
        }
        if(null != address){
            connected = true
        }
    }


    /**
     * 服务器控制按钮监听
     */
    fun serverBtnOnClick(v:View){
        when(v.id){
            R.id.server_btn ->{
                if(serverUtil!!.ifWifiAvailabel()){ //wifi已连接
                    val serverBtn = v as Button
                    if(!ServerUtil.SWITCH){
                        this.startService(serverUtil?.getservice())
                        val ipformat = getString(R.string.httpadd)
                        if(!TextUtils.isEmpty(ServerUtil.ip)){
                            val port = CONFIG["port"]?.toInt()
                            address_tv?.text = String.format(ipformat, ServerUtil.ip,port)
                        }
                        serverBtn.text = getString(R.string.stopserver)
                        SnackbarUtil.short(v,"服务器启动")
                    }
                    else{
                        this.stopService(serverUtil?.getservice())
                        address_tv?.text = getText(R.string.noserver)
                        serverBtn.text = getString(R.string.startserver)
                        SnackbarUtil.short(v,"服务器停止")
                    }
                }else{
                    SnackbarUtil.short(v,"请连接WiFi后使用！")
                }
            }
            R.id.setting_btn ->{
                val dialog = SettingDialogFragment()
                dialog.show(this.fragmentManager,"SettingDialogFragment")
            }
        }
    }

    override fun onPause() {
        super.onPause()
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

    /**
     * 退出确认
     */
    private fun quit(context: Context){
        AlertDialog.Builder(context)
                .setTitle("退出")
                .setMessage("您确定要退出吗？")
                .setPositiveButton("退出") { _, _ ->
                    // TODO Auto-generated method stub
                    FileSortUtil().destory()
                    serverUtil?.destroy()
                    this.finish()
                }.setNegativeButton("取消") { _, _ ->
            // TODO Auto-generated method stub
        }.show()
    }

    /**
     * 返回键监听
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.repeatCount == 0) {
            if(getVisibleFragment() is FileListFragment){
                if((getVisibleFragment() as FileListFragment).onBack()){
                    return true
                } else {
                   quit(this)
                }
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}


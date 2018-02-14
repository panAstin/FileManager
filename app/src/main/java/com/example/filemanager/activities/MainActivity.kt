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
import android.support.v4.app.Fragment
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.text.TextUtils
import android.widget.Button
import android.widget.TextView
import com.example.filemanager.utils.FileSortUtil
import com.example.filemanager.R
import com.example.filemanager.fragments.FileListFragment
import com.example.filemanager.utils.ServerUtil
import com.example.filemanager.fragments.SettingDialogFragment
import com.example.filemanager.services.WifiDirectService
import com.example.filemanager.utils.SnackbarUtil
import kotlin.collections.ArrayList

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
    fun getVisibleFragment(): Fragment? {
        val fragments = this@MainActivity.supportFragmentManager.fragments
        return fragments.firstOrNull { it != null && it.isVisible }
    }

    fun doFileAsync(){
        val wifiDirectService = Intent(this,WifiDirectService::class.java)
        this.startService(wifiDirectService)
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


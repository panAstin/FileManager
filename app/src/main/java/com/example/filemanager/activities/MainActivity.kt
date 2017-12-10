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
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.SimpleAdapter
import com.example.filemanager.utils.FileSortUtil
import com.example.filemanager.R
import com.example.filemanager.fragments.FileListFragment
import com.example.filemanager.fragments.ServerFragment
import com.example.filemanager.utils.SnackbarUtil
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private val REQUEST_CODE = 11
    private var tablayout: TabLayout? = null
    private var viewpager: ViewPager? = null
    private var mDrawerLayout:DrawerLayout? = null
    private var mDrawerToggle:ActionBarDrawerToggle? = null
    private var toolbar:Toolbar? = null
    private var dl_list:ListView? = null
    private val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.INTERNET)
    private var mPermissionList = ArrayList<String>()

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

    }

    /**
     * 控件初始化
     */
    private fun initview(){
        toolbar = find(R.id.toolbar)
        mDrawerLayout = find(R.id.dl_left)
        tablayout = find(R.id.tablayout)
        viewpager = find(R.id.viewpager)
        dl_list = find(R.id.drawermenu)

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
        binddata()
        dl_list?.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            //侧滑菜单点击监听
            selectItem(position)
        }
        supportFragmentManager.beginTransaction().add(R.id.fragmentcontent,ServerFragment().newInstnace()).commit()
    }

    /**
     * 侧滑菜单绑定数据
     */
    private fun binddata(){
        val texts = arrayOf("文件管理","远程管理","退出")
        val icons = arrayOf(R.drawable.fmbtn,R.drawable.lkbtn,R.drawable.shutdown)
        val data:ArrayList<Map<String,Any>> = ArrayList()
        for (index in texts.indices){
            val item = HashMap<String,Any>()
            item.put("txt",texts[index])
            item.put("icon",icons[index])
            data.add(item)
        }
        val simplead = SimpleAdapter(this,data,R.layout.dl_menu, arrayOf("txt","icon"),
                intArrayOf(R.id.menu_tv,R.id.menu_img))
        dl_list?.adapter = simplead
    }

    /**
     * 侧滑菜单项点击
     */
    private fun selectItem(position: Int){
        when (position){
            0 -> {
                tablayout?.visibility = View.VISIBLE
                viewpager?.visibility = View.VISIBLE
                mDrawerLayout?.closeDrawers()            }
            1 ->{
                tablayout?.visibility = View.GONE
                viewpager?.visibility = View.GONE
                mDrawerLayout?.closeDrawers()
            }
            2 ->{
                quit(this)
            }
        }
    }
    /**
     * 检查权限
     */
    private fun checkPermissions() {
        //检查权限
        permissions
                .filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
                .forEach { mPermissionList.add(it) }
        if (mPermissionList.isEmpty()) {
            initview()
        } else {
            //进入到这里代表没有权限.
            val mpermissions = mPermissionList.toArray()
            ActivityCompat.requestPermissions(this, mpermissions as Array<out String>, REQUEST_CODE)
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


package com.example.filemanager.activities

import UI.SortActivityUI
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.ArrayMap
import android.view.*
import com.example.filemanager.*
import com.example.filemanager.adapters.fmAdapter
import com.example.filemanager.fragments.FileListFragment
import com.example.filemanager.utils.*
import org.jetbrains.anko.setContentView
import java.io.File
import java.util.concurrent.Executors


class SortActivity : AppCompatActivity() {
    private var sFiles: ArrayList<ExFile>? = null
    private var sRecyclerView: RecyclerView? = null
    private var fmadapter: fmAdapter? = null
    private val Sortstxt = arrayOf("文档","下载","音乐","图片","视频","压缩包","安装包")
    private var sort = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SortActivityUI().setContentView(this)
        //if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.TRANSPARENT
            window.navigationBarColor = Color.TRANSPARENT
        //}
        //接收数据
        val bundle = this.intent.extras
        //接收sort值
        sort = bundle.getInt("sort")
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        toolbar.title = Sortstxt[sort]
        setSupportActionBar(toolbar)
        sRecyclerView = findViewById(R.id.filelist) as RecyclerView
        sRecyclerView!!.layoutManager = LinearLayoutManager(this)          //设置布局管理器
        sRecyclerView!!.itemAnimator = DefaultItemAnimator()               //设置Item增加、移除动画
        sRecyclerView!!.addOnItemTouchListener(object : OnRecyclerItemClickListener(sRecyclerView) {
            override fun onItemClick(viewHolder: RecyclerView.ViewHolder) {        //点击

                if(fmAdapter.selectFlag >0){
                    fmAdapter.isSelected!![viewHolder.adapterPosition] = when(fmAdapter.isSelected!![viewHolder.adapterPosition] ){
                        true -> false
                        else -> true
                    }
                    fmadapter!!.notifyDataSetChanged()
                    this@SortActivity.invalidateOptionsMenu()
                }else{
                    val path = sFiles!![viewHolder.adapterPosition].path
                    val file = File(path)
                    if (file.exists() && file.canRead()) {            // 文件存在并可读
                        FileUtil.openFile(this@SortActivity,file)              //打开文件
                    } else {   //没有权限
                        DialogFragmentHelper.showTips(supportFragmentManager,"没有权限!")
                    }
                }
            }
            override fun onItemLongClick(viewHolder: RecyclerView.ViewHolder) {            //长按
                changeSelecFlag(viewHolder.adapterPosition)
            }
        })
        fmadapter = fmAdapter(this)
        sRecyclerView?.adapter = fmadapter           //设置adapter
        showFiles(sort)
    }

    /**
     * 显示文件列表
     * @param sort 文件类型
     */
    private fun showFiles(sort:Int){
        sFiles = ArrayList()
        val exfiles = FileSortUtil().getFilesByType(FileType.getFileTypeByOrdinal(sort))
        if(exfiles != null){
          for (ef in exfiles){
              ef.initIcon()
              ThreadPoolUtil.getThreadPool().execute{
                  try {
                      ef.setSize(FileUtil.getAutoFileOrFilesSize(ef.path))
                  }catch (e:Exception){
                      e.stackTrace
                  }
              }
              sFiles!!.add(ef)
          }
        }
        fmadapter?.setListData(sFiles!!)
    }

    private fun displaySnackbar(message: String) {
        Snackbar.make(this.window.decorView,message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {         //菜单栏
        menuInflater.inflate(R.menu.main, menu)
        //获取搜索的菜单组件
        val menuItemS = menu.findItem(R.id.search)
        menuItemS.isVisible = false
        val menuItemD = menu.findItem(R.id.delete)
        val menuItemA = menu.findItem(R.id.selectAll)
        menuItemD.setOnMenuItemClickListener({//删除文件
            val filebeans = getSelectedFiles()
            var i = filebeans.size - 1
            DialogFragmentHelper.showConfirmDialog(supportFragmentManager,"确定要删除选中的文件吗？",
                    object :IDialogResultListener<Int>{
                        override fun onDataResult(result: Int) {
                            if (-1 == result){
                                try {
                                    while (i> -1 ) {
                                        if (FileUtil.deleteFile(filebeans.valueAt(i))) {
                                            fmadapter!!.removeItem(filebeans.keyAt(i)) //移除列表项
                                        }
                                        i--
                                    }
                                    displaySnackbar("删除成功！")
                                }catch (e:Exception){
                                    e.stackTrace
                                    displaySnackbar("删除失败！")
                                }
                                changeSelecFlag(null)
                            }else{
                                displaySnackbar("操作取消")
                            }
                        }
                    },true,null)
            true
        })
        menuItemA.setOnMenuItemClickListener({
            selectAll()
            this.invalidateOptionsMenu()
            true
        })
        if(fmAdapter.selectFlag >0){
            menuItemD.isVisible = true
            menuItemA.isVisible = true
            menu.add(Menu.NONE, Menu.FIRST + 4, 4, "复制")
            menu.add(Menu.NONE, Menu.FIRST + 5, 6, "重命名")
            if(fmadapter!!.getSelectCount() > 1){
                menu.findItem(Menu.FIRST + 5).isEnabled = false
            }
        }else{
            menuItemD.isVisible = false
            menuItemA.isVisible = false
        }
        menu.add(Menu.NONE, Menu.FIRST + 2, 3, "取消")
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            Menu.FIRST + 2 -> displaySnackbar("取消")
            Menu.FIRST + 4 -> {//复制文件
                val filebeans = getSelectedFiles()
                FileListFragment.selectedFiles = ArrayList()
                for (filebean in filebeans){
                    if (!filebean.value.exists()) {
                        displaySnackbar("复制失败")
                    }
                    FileListFragment.selectedFiles!!.add(filebean.value)
                }
                displaySnackbar("文件已复制")
                fmadapter!!.changeSelecFlag()
                this.invalidateOptionsMenu()
            }
            Menu.FIRST + 5 ->{ //重命名
                val file = getSelectedFiles().valueAt(0)
                val position = getSelectedFiles().keyAt(0)
                DialogFragmentHelper.showInsertDialog(this.supportFragmentManager,"重命名文件",file.name,
                        object: IDialogResultListener<String> {
                            override fun onDataResult(result: String) {
                                var modifyName = result
                                val fpath = file.parentFile.path
                                var i = 0
                                var newFile = File(fpath + "/" + modifyName)
                                while (newFile.exists()){
                                    i++
                                    modifyName += "("+i.toString()+")"
                                    newFile = File(fpath + "/" + modifyName)
                                }
                                if (FileUtil.renameFile(file,newFile)) {
                                    sFiles!![position] = ExFile(newFile.path)
                                    fmadapter!!.notifyItemChanged(position)
                                    displaySnackbar("重命名成功！")
                                } else {
                                    displaySnackbar("重命名失败！")
                                }
                            }
                        },true)
            }
        }
        return false
    }

    //获取被选中的文件
    private fun getSelectedFiles():ArrayMap<Int,ExFile>{
        val files = ArrayMap<Int,ExFile>()
        for (isselect in fmAdapter.isSelected!!){
            if(isselect.value){
                files.put(isselect.key,sFiles!![isselect.key])
            }
        }
        return files
    }

    //更改选择模式标识
    fun changeSelecFlag(position: Int?){
        fmAdapter.selectFlag = when{
            fmAdapter.selectFlag > 0 -> 0
            else ->{
                fmadapter!!.initIsSelected()
                if (position!=null){
                    fmAdapter.isSelected!![position] = true
                }
                1
            }
        }
        fmadapter!!.notifyDataSetChanged()
        this.invalidateOptionsMenu()
    }

    //全选
    private fun selectAll(){
        if(fmadapter!!.getSelectCount()< fmAdapter.isSelected!!.size){
            for (isselect in fmAdapter.isSelected!!){
                if (!isselect.value){
                    isselect.setValue(true)
                }
            }
            fmadapter!!.notifyDataSetChanged()
        }else {
            changeSelecFlag(null)
        }
    }

    //退出确认
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.repeatCount == 0) {
            if(fmAdapter.selectFlag > 0){
                changeSelecFlag(null)
                return true
            }else{
                this.finish()
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

}

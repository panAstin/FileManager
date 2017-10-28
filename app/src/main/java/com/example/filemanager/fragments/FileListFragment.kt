package com.example.filemanager.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.os.*
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.ArrayMap
import android.view.*
import android.widget.EditText
import android.widget.SearchView
import android.widget.TextView
import com.example.filemanager.*
import com.example.filemanager.activities.MainActivity
import com.example.filemanager.adapters.fmAdapter
import com.example.filemanager.adapters.fmAdapter.Companion.selectFlag
import com.example.filemanager.adapters.pathAdapter
import com.example.filemanager.utils.FileUtil
import com.example.filemanager.utils.MediaUtil
import java.io.File
import kotlin.collections.ArrayList

/**
 * Created by 11046 on 2017/4/24.
 * 文件列表
 */
class FileListFragment : Fragment() {
    private val ROOT_PATH = Environment.getExternalStorageDirectory().path        //根目录
    private var mFiles: ArrayList<FileBean>? = null     //存储文件信息
    private var Pathnotes: ArrayList<String>? = null     //存储路径记录
    private var currentpath = ROOT_PATH        //当前文件路径
    private var mRecyclerView: RecyclerView? = null
    private var patharea: RecyclerView? = null
    private var pathtxt:TextView? = null
    private var SEARCH_SWITCH = 0
    private var RESULT_COUNT: Int = 0
    private var progressDialog: ProgressDialog? = null
    private var pathadapter: pathAdapter? = null
    private var fmadapter: fmAdapter? = null
    private var mactivity: MainActivity? = null
    companion object {
        var selectedFiles: ArrayList<FileBean>? = null
    }

    fun newInstance(): FileListFragment {
        val args = Bundle()
        val filelistFragment = FileListFragment()
        filelistFragment.arguments = args
        return filelistFragment
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val listview:View?= inflater?.inflate(R.layout.filelist_layout,container,false)
        setHasOptionsMenu(true)
        initfilelist(listview!!)
        return listview
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mactivity=context as MainActivity
        progressDialog = ProgressDialog(mactivity)
    }

    /**
     * 创建菜单项
     */
    override fun onCreateOptionsMenu(menu: Menu,inflater:MenuInflater) {         //菜单栏
        inflater.inflate(R.menu.main, menu)
        //获取菜单组件
        val menuItemS = menu.findItem(R.id.search)
        val menuItemD = menu.findItem(R.id.delete)
        val menuItemA = menu.findItem(R.id.selectAll)
        val searchView = menuItemS.actionView as android.support.v7.widget.SearchView
        //设置搜索的事件
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener, android.support.v7.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                if (query == "") {
                    displaySnackbar("请输入关键字")
                } else {
                    RESULT_COUNT = 0
                    //显示ProgressDialog
                    initprogressdialog("正在搜索...","请稍候...",0)
                    //新建线程
                     val mthread = Thread(Runnable {
                        //需要花时间的方法
                        try {
                           showSearchresult(query)
                        } catch(e:Exception) {
                            e.printStackTrace()
                        } finally {
                            progressDialog?.dismiss()
                        }
                    })
                    progressDialog?.setOnCancelListener({
                        try {
                            mthread.interrupt()
                            displaySnackbar("搜索已中断！")
                        } catch (e:Exception){
                            e.printStackTrace()
                        }
                    })
                    progressDialog?.show()
                    mthread.start()
                }
                return false
            }
            override fun onQueryTextChange(newText: String): Boolean {
                if (newText == "") {
                    patharea!!.visibility = View.VISIBLE
                    pathtxt!!.visibility = View.GONE
                    showFileDir(currentpath)
                }
                return false
            }
        })
        menuItemD.setOnMenuItemClickListener({//删除文件
            val filebeans = getSelectedFiles()
            var i = filebeans.size - 1
            AlertDialog.Builder(mactivity)
                    .setTitle("注意!")
                    .setMessage("确定要删除选中的文件吗？")
                    .setPositiveButton("确定") { _, _ ->
                        try {
                            while (i> -1 ) {
                                if (FileUtil.deleteFile(context, filebeans.valueAt(i).getFile())) {
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
                    }
                    .setNegativeButton("取消") { _, _ -> }.show()
            true
        })
        menuItemA.setOnMenuItemClickListener({
            selectAll()
            mactivity?.invalidateOptionsMenu()
            true
        })
        if(selectFlag>0){
            menuItemS.isVisible = false
            menuItemD.isVisible = true
            menuItemA.isVisible = true
            menu.add(Menu.NONE, Menu.FIRST + 4, 4, "复制")
            menu.add(Menu.NONE, Menu.FIRST + 5, 6, "重命名")
            if(fmadapter!!.getSelectCount() > 1){
                menu.findItem(Menu.FIRST + 5).isEnabled = false
            }
        }else{
            menuItemS.isVisible = true
            menuItemD.isVisible = false
            menuItemA.isVisible = false
        }
        menu.add(Menu.NONE, Menu.FIRST + 1, 7, "新建文件夹")
        if (selectedFiles != null) {
            menu.add(Menu.NONE, Menu.FIRST + 3, 5, "粘贴")
        }
        menu.add(Menu.NONE, Menu.FIRST + 2, 3, "取消")
    }

    /**
     * 菜单项选择事件
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            Menu.FIRST + 1 -> {  //新建文件夹
                val editText = EditText(view?.context)
                AlertDialog.Builder(view?.context).setTitle("请输入文件名称").setView(editText).setPositiveButton("确定"
                ) { _, _ ->
                    // TODO Auto-generated method stub
                    val mFileName = editText.text.toString()
                    val IMAGES_PATH = "$currentpath/$mFileName/"       //获取根目录
                    FileUtil.createMkdir(IMAGES_PATH)
                    fmadapter!!.addItem(FileBean(File(IMAGES_PATH)))
                }.setNegativeButton("取消", null).show()
            }
            Menu.FIRST + 2 -> displaySnackbar("取消")
            Menu.FIRST + 3 -> {    //粘贴文件
                //显示ProgressDialog
                initprogressdialog("正在粘贴...","请稍候...",selectedFiles!!.size)
                //新建线程
                val mthread = Thread(Runnable {
                    for (selectfile in selectedFiles!!) {
                        //需要花时间的方法
                        try {
                            val targetpath = currentpath + "/" + selectfile.getFile().name
                            FileUtil.copy(selectfile.getFile().path, targetpath)
                            fmadapter!!.addItem(FileBean(File(targetpath)))
                            if(MediaUtil.isMediaFile(targetpath)){
                                MediaUtil.sendScanFileBroadcast(context,targetpath)
                            }
                            progressDialog?.incrementProgressBy(1)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            displaySnackbar("粘贴失败")
                        } finally {
                            progressDialog?.dismiss()
                            displaySnackbar("粘贴成功")
                        }
                    }
                })
                progressDialog?.setOnCancelListener({
                    try {
                        mthread.interrupt()
                        displaySnackbar("已中断操作！")
                    }
                    catch (e:Exception){
                        e.printStackTrace()
                        displaySnackbar("粘贴失败")
                    }
                })
                progressDialog?.show()
                mthread.start()
            }
            Menu.FIRST + 4 -> {   //复制文件
                val filebeans = getSelectedFiles()
                selectedFiles = ArrayList()
                for (filebean in filebeans){
                    if (!filebean.value.getFile().exists()) {
                        displaySnackbar("复制失败")
                    }
                    selectedFiles!!.add(filebean.value)
                }
                displaySnackbar("文件已复制")
                fmadapter!!.changeSelecFlag()
                mactivity?.invalidateOptionsMenu()
            }
            Menu.FIRST + 5 ->{   //重命名
                val file = getSelectedFiles().valueAt(0).getFile()
                val position = getSelectedFiles().keyAt(0)
                val factory = LayoutInflater.from(mactivity)
                val view = factory.inflate(R.layout.rename_dialog, null)
                val editText = view!!.findViewById(R.id.editText) as EditText
                editText.setText(file.name)
                val listener2 = DialogInterface.OnClickListener { _, _ ->
                    // TODO Auto-generated method stub
                    var modifyName = editText.text.toString()
                    val fpath = file.parentFile.path
                    var i = 0
                    var newFile = File(fpath + "/" + modifyName)
                    while (newFile.exists()){
                        i++
                        modifyName += "("+i.toString()+")"
                        newFile = File(fpath + "/" + modifyName)
                    }
                    if (FileUtil.renameFile(context,file,newFile)) {
                        mFiles!![position] = FileBean(newFile)
                        fmadapter!!.notifyItemChanged(position)
                        displaySnackbar("重命名成功！")
                    } else {
                        displaySnackbar("重命名失败！")
                    }
                }
                val renameDialog = AlertDialog.Builder(view.context)
                renameDialog.setView(view)
                renameDialog.setPositiveButton("确定", listener2)
                renameDialog.setNegativeButton("取消") { _, _ ->
                    // TODO Auto-generated method stub
                }
                renameDialog.show()
            }
        }
        return false
    }

    /**
     * 文件列表初始化
     */
    private fun initfilelist(view:View){
        mFiles = ArrayList()
        Pathnotes = ArrayList()
        patharea = view.findViewById(R.id.patharea) as RecyclerView
        pathtxt = view.findViewById(R.id.pathtxt) as TextView
        mRecyclerView = view.findViewById(R.id.filelist) as RecyclerView
        val llmanager = LinearLayoutManager(view.context)
        llmanager.orientation = LinearLayoutManager.HORIZONTAL   //设置横向布局
        patharea!!.layoutManager = llmanager       //设置布局管理器
        patharea!!.itemAnimator = DefaultItemAnimator()               //设置Item增加、移除动画
        //设置点击监听
        patharea!!.addOnItemTouchListener(object : OnRecyclerItemClickListener(patharea){
            override fun onItemLongClick(viewHolder: RecyclerView.ViewHolder?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onItemClick(viewHolder: RecyclerView.ViewHolder?) {
                var i=1
                var topath = ROOT_PATH
                //点击的根据路径项得到跳转路径
                while(i <= viewHolder!!.adapterPosition ){
                    topath += "/" + Pathnotes!![i]
                    i++
                }
                //重置选择
                selectFlag = 0
                mactivity!!.invalidateOptionsMenu()
                showFileDir(topath)
            }

        } )
        mRecyclerView!!.layoutManager = LinearLayoutManager(view.context)          //设置布局管理器
        mRecyclerView!!.itemAnimator = DefaultItemAnimator()               //设置Item增加、移除动画
        //设置点击与长按监听器
        mRecyclerView!!.addOnItemTouchListener(object : OnRecyclerItemClickListener(mRecyclerView) {
            override fun onItemClick(viewHolder: RecyclerView.ViewHolder) {        //点击
                if(selectFlag >0){           //选择模式下，点击选择文件项
                    fmAdapter.isSelectd!![viewHolder.adapterPosition] = when(fmAdapter.isSelectd!![viewHolder.adapterPosition] ){
                        true -> false
                        else -> true
                    }
                    fmadapter!!.notifyDataSetChanged()
                    mactivity?.invalidateOptionsMenu()
                }else{
                    val path =mFiles!![viewHolder.adapterPosition].getFile().path
                    val file = File(path)
                    if (file.exists() && file.canRead()) {            // 文件存在并可读
                        if (file.isDirectory) {
                            showFileDir(path)                      //显示子目录及文件
                        } else {
                            FileUtil.openFile(context,file)               //打开文件
                        }
                    } else {   //没有权限
                        AlertDialog.Builder(mactivity).setTitle("信息")     //弹出窗口
                                .setMessage("没有权限!")
                                .setPositiveButton("确定") {  _,  _ -> displaySnackbar("没有权限") }.show()
                    }
                }
            }
            override fun onItemLongClick(viewHolder: RecyclerView.ViewHolder) {            //长按
                changeSelecFlag(viewHolder.adapterPosition)    //进入选择模式
            }
        })
        showFileDir(ROOT_PATH)
    }

    /**
     * 显示文件列表
     * @param path 文件路径
     */
    private fun showFileDir(path: String) {
        mFiles = ArrayList()
        Pathnotes = ArrayList()
        val file = File(path)
        val files = file.listFiles()
        val strpath = path.substring(path.indexOf("0"),path.length)
        Pathnotes = strpath.split("/") as ArrayList<String>  //分割路径
        files
                .filterNot {
                    it.isHidden      //筛选隐藏文件
                }
                .forEach { mFiles!!.add(FileBean(it)) }
        SEARCH_SWITCH = 0

        currentpath = path

        pathadapter= pathAdapter(context, Pathnotes!!)
        patharea!!.adapter= pathadapter
        fmadapter = fmAdapter(context,mFiles!!)
        mRecyclerView?.adapter = fmadapter           //设置adapter
    }

    //提示信息
    private fun displaySnackbar(message: String) {
        Snackbar.make(activity.window.decorView,message, Snackbar.LENGTH_SHORT).show()
    }

    /**
     * 初始化进度框
     * @param title 标题
     * @param message 提示信息
     * @param max 最大值
     */
    fun initprogressdialog(title:String,message: String,max:Int){
        progressDialog?.setTitle(title)    //标题
        progressDialog?.setMessage(message)  //显示信息
        if(max>0){       //已知进度值
            progressDialog?.max = max
            progressDialog?.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        }else{           //未知进度值
            progressDialog?.isIndeterminate=true
        }
        progressDialog?.setCancelable(true)
    }

    /**
     * 文件搜索
     *@param key 关键字
     * @param path 路径
     * @return 搜索结果
     */
    private fun FileSearch(key: String, path: String):ArrayList<FileBean> {
        val fileBeans = ArrayList<FileBean>()
        val file = File(path)
        val files = file.listFiles()
        files
                .filterNot {
                    it.isHidden      //筛选隐藏文件
                }
                .forEach {
                    if (it.isDirectory) {
                        fileBeans.addAll(FileSearch(key, it.path))
                    }
                    if (key in it.name) {
                        fileBeans.add(FileBean(it))
                        RESULT_COUNT++
                    }
                }
        return  fileBeans
    }

    /**
     * 显示搜索结果
     *@param keywd 关键字
     */
    fun showSearchresult(keywd: String) {
        mFiles= FileSearch(keywd, currentpath)
        SEARCH_SWITCH = 1
        val msg = Message()
        msg.obj = fmAdapter(context,mFiles!!)
        handler.sendMessage(msg)   //传递结果集
    }

    /**
     * 获取被选文件
     */
    private fun getSelectedFiles():ArrayMap<Int,FileBean>{
        val files = ArrayMap<Int,FileBean>()
        for (isselect in fmAdapter.isSelectd!!){
            if(isselect.value){
                files.put(isselect.key,mFiles!![isselect.key])
            }
        }
        return files
    }

    /**
     * 更改模式标识
     */
    fun changeSelecFlag(position: Int?){
        selectFlag = when{
            selectFlag > 0 -> 0
            else ->{
                fmadapter!!.initIsSelectd()
                if (position!=null){
                    fmAdapter.isSelectd!![position] = true
                }
                1
            }
        }
        fmadapter!!.notifyDataSetChanged()
        mactivity?.invalidateOptionsMenu()
    }

    /**
     * 全选
     */
    private fun selectAll(){
        if(fmadapter!!.getSelectCount()< fmAdapter.isSelectd!!.size){
            for (isselect in fmAdapter.isSelectd!!){
                if (!isselect.value){
                    isselect.setValue(true)
                }
            }
            fmadapter!!.notifyDataSetChanged()
        }else {
            changeSelecFlag(null)
        }
    }

    /**
     * 定义Handler
     */
    private var handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            //关闭ProgressDialog
            progressDialog?.dismiss()
            //更新UI
            fmadapter = msg.obj as fmAdapter
            mRecyclerView!!.adapter = fmadapter
            patharea!!.visibility = View.GONE
            pathtxt!!.visibility = View.VISIBLE
            if (fmadapter!!.itemCount == 0) {
                pathtxt!!.text = "无对应文件"
            } else {
                pathtxt!!.text=String.format(resources.getString(R.string.result_cout),RESULT_COUNT)
            }
        }
    }

    /**
     * 返回键事件
     */
    fun onBack():Boolean{    //返回键
        if(selectFlag > 0){          //退出选择模式
            changeSelecFlag(null)
            return true
        } else if (currentpath!=ROOT_PATH){    //返回上级目录
            val file = File(currentpath)
            showFileDir(file.parent)
            return true
        }
        return false
    }

}

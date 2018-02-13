package com.example.filemanager.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.*
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.ArrayMap
import android.view.*
import android.widget.SearchView
import android.widget.TextView
import com.example.filemanager.*
import com.example.filemanager.activities.MainActivity
import com.example.filemanager.adapters.fmAdapter
import com.example.filemanager.adapters.fmAdapter.Companion.selectFlag
import com.example.filemanager.adapters.pathAdapter
import com.example.filemanager.utils.*
import java.io.File
import java.util.concurrent.Executors
import kotlin.collections.ArrayList

/**
 * Created by 11046 on 2017/4/24.
 * 文件列表
 */
class FileListFragment : Fragment() {
    private val ROOT_PATH = Environment.getExternalStorageDirectory().path        //根目录
    private var mFiles: ArrayList<FileBean>? = null     //存储文件信息
    private var currentpath = ROOT_PATH        //当前文件路径
    private var mRecyclerView: RecyclerView? = null
    private var patharea: RecyclerView? = null
    private var pathtxt:TextView? = null
    private var SEARCH_SWITCH = 0
    private var pathadapter: pathAdapter? = null
    private var fmadapter: fmAdapter? = null
    private var mactivity: MainActivity? = null
    private var cacheThreadPool = Executors.newCachedThreadPool()           //线程池

    companion object {
        var selectedFiles: ArrayList<FileBean>? = null        //被选中的文件
        var Pathnotes = ArrayList<String>()     //存储路径记录
        @Volatile var isFinished = true          //正常完成标识
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
        val menuItemZ = menu.findItem(R.id.unzip)
        val searchView = menuItemS.actionView as android.support.v7.widget.SearchView
        //设置搜索的事件
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener, android.support.v7.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                if (query == "") {
                    displaySnackbar("请输入关键字")
                } else {
                    //显示ProgressDialog
                    DialogFragmentHelper.showProgress(fragmentManager,"正在搜索...",
                            true,object :CommonDialogFragment.OnDialogCancelListener{
                        override fun onCancel() {
                            try {
                                isFinished = false
                                displaySnackbar("搜索已中断！")
                            } catch (e:Exception){
                                e.printStackTrace()
                            }
                        }
                    })
                    //新建线程
                    cacheThreadPool.execute{
                         //需要花时间的方法
                         try {
                             isFinished = true
                             var result:ArrayList<FileBean> = ArrayList()
                             while(!Thread.currentThread().isInterrupted){
                                 result= FileSearch(query, currentpath)
                                 Thread.currentThread().interrupt()
                             }
                             if(isFinished){  //正常结束
                                 mFiles = result
                                 val msg = Message()
                                 msg.arg1 = 1
                                 handler.sendMessage(msg)   //传递结果集
                                 SEARCH_SWITCH = 1
                             }
                         } catch(e:Exception) {
                             e.printStackTrace()
                         } finally {
                             if(null != fragmentManager.findFragmentByTag(DialogFragmentHelper.getProgressTag())){
                                 val pdf:DialogFragment? = fragmentManager.findFragmentByTag(DialogFragmentHelper.getProgressTag()) as DialogFragment
                                 pdf?.dismiss()
                             }
                         }
                     }
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
            DialogFragmentHelper.showConfirmDialog(fragmentManager,"确定要删除选中的文件吗？",
                    object :IDialogResultListener<Int>{
                        override fun onDataResult(result: Int) {
                            if (-1 == result){
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
                            }else{
                                displaySnackbar("操作取消")
                            }
                        }
                    },true,null)
            true
        })
        menuItemZ.setOnMenuItemClickListener({
            when(selectFlag){
                2 -> {
                    val filebean = getSelectedFiles()[0]
                    if(FileUtil.unzipFile(filebean!!.getFile().path,currentpath)){
                        showFileDir(currentpath)
                        displaySnackbar("解压完成")
                    }else{
                        displaySnackbar("解压失败")
                    }
                    selectFlag = 0
                }
                3 -> {
                    showFileDir(ROOT_PATH)
                    selectFlag = 2
                }
            }
            true
        })
        menuItemA.setOnMenuItemClickListener({
            selectAll()
            mactivity?.invalidateOptionsMenu()
            true
        })
        when(selectFlag) {
            0 -> {
                menuItemS.isVisible = true
                menuItemD.isVisible = false
                menuItemA.isVisible = false
                menuItemZ.isVisible = false
            }
            1 -> {
                menuItemS.isVisible = false
                menuItemD.isVisible = true
                menuItemA.isVisible = true
                menuItemZ.isVisible = false
                menu.add(Menu.NONE, Menu.FIRST + 4, 4, "复制")
                menu.add(Menu.NONE, Menu.FIRST + 5, 6, "重命名")
                menu.add(Menu.NONE, Menu.FIRST + 6, 8, "压缩")
                if (fmadapter!!.getSelectCount() > 1) {
                    menu.findItem(Menu.FIRST + 5).isEnabled = false
                }
            }
            2 or 3 -> {
                menuItemS.isVisible = false
                menuItemD.isVisible = false
                menuItemA.isVisible = false
                menuItemZ.isVisible = true
            }
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
                DialogFragmentHelper.showInsertDialog(fragmentManager,"请输入文件名称","",
                        object:IDialogResultListener<String>{
                            override fun onDataResult(result: String) {
                                // TODO Auto-generated method stub
                                val mFileName = result
                                val newfilepath = "$currentpath/$mFileName/"       //获取根目录
                                FileUtil.createMkdir(newfilepath)
                                val newfb = FileBean(File(newfilepath)).getInitailed(context)
                                fmadapter!!.addItem(newfb)
                            }
                        },true)
            }
            Menu.FIRST + 2 -> displaySnackbar("取消")
            Menu.FIRST + 3 -> {    //粘贴文件
                //显示ProgressDialog
                DialogFragmentHelper.showProgress(fragmentManager,"正在粘贴...",true)
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
                        } catch (e: Exception) {
                            e.printStackTrace()
                            displaySnackbar("粘贴失败")
                        } finally {
                            val pdf = fragmentManager.findFragmentByTag(DialogFragmentHelper.getProgressTag()) as DialogFragment
                            pdf.dismiss()
                            displaySnackbar("粘贴成功")
                        }
                    }
                })
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
                DialogFragmentHelper.showInsertDialog(fragmentManager,"重命名文件",file.name,
                        object:IDialogResultListener<String>{
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
                                if (FileUtil.renameFile(context,file,newFile)) {
                                    mFiles!![position] = FileBean(newFile).getInitailed(context)
                                    fmadapter!!.notifyItemChanged(position)
                                    displaySnackbar("重命名成功！")
                                } else {
                                    displaySnackbar("重命名失败！")
                                }
                            }
                        },true)
            }
            Menu.FIRST + 6 ->{   //压缩
                val files = ArrayList<File>()
                val filebeans = getSelectedFiles()
                var zipresult = false
                for (filebean in filebeans){
                    if (!filebean.value.getFile().exists()) {
                        displaySnackbar("压缩失败")
                    }
                    files.add(filebean.value.getFile())
                }

                DialogFragmentHelper.showInsertDialog(fragmentManager,"输入压缩文件名","",
                        object:IDialogResultListener<String>{
                    override fun onDataResult(result: String) {
                        val zippath = currentpath + File.separator + result + ".zip"
                        if (FileUtil.zipFiles(files,zippath)) {
                            zipresult = true
                        }
                        if(zipresult){
                            fmadapter?.changeSelecFlag()
                            fmadapter!!.addItem(FileBean(File(zippath)).getInitailed(context))
                            displaySnackbar("压缩成功")
                        }else{
                            displaySnackbar("压缩失败")
                        }
                    }
                },true)

            }
        }
        return false
    }

    /**
     * 文件列表初始化
     */
    private fun initfilelist(view:View) {
        mFiles = ArrayList()
        Pathnotes.clear()
        patharea = view.findViewById(R.id.patharea) as RecyclerView
        pathtxt = view.findViewById(R.id.pathtxt) as TextView
        mRecyclerView = view.findViewById(R.id.filelist) as RecyclerView
        val llmanager = LinearLayoutManager(view.context)
        llmanager.orientation = LinearLayoutManager.HORIZONTAL   //设置横向布局
        patharea!!.layoutManager = llmanager       //设置布局管理器
        patharea!!.itemAnimator = DefaultItemAnimator()               //设置Item增加、移除动画
        //设置点击监听
        patharea!!.addOnItemTouchListener(object : OnRecyclerItemClickListener(patharea) {
            override fun onItemLongClick(viewHolder: RecyclerView.ViewHolder?) {
            }

            override fun onItemClick(viewHolder: RecyclerView.ViewHolder?) {
                var i = 1
                var topath = ROOT_PATH
                //点击的根据路径项得到跳转路径
                while (i <= viewHolder!!.adapterPosition) {
                    topath += "/" + Pathnotes[i]
                    i++
                }
                mactivity!!.invalidateOptionsMenu()
                showFileDir(topath)
            }

        })
        pathadapter = pathAdapter(context)
        patharea!!.adapter = pathadapter
        mRecyclerView!!.layoutManager = LinearLayoutManager(view.context)          //设置布局管理器
        mRecyclerView!!.itemAnimator = DefaultItemAnimator()               //设置Item增加、移除动画
        //设置点击与长按监听器
        mRecyclerView!!.addOnItemTouchListener(object : OnRecyclerItemClickListener(mRecyclerView) {
            override fun onItemClick(viewHolder: RecyclerView.ViewHolder) {        //点击
                if (selectFlag > 0) {           //选择模式下，点击选择文件项
                    fmAdapter.isSelected!![viewHolder.adapterPosition] = when (fmAdapter.isSelected!![viewHolder.adapterPosition]) {
                        true -> false
                        else -> true
                    }
                    fmadapter!!.notifyDataSetChanged()
                    mactivity?.invalidateOptionsMenu()
                } else {
                    val path = mFiles!![viewHolder.adapterPosition].getFile().path
                    val file = File(path)
                    if (file.exists() && file.canRead()) {            // 文件存在并可读
                        if (file.isDirectory) {
                            showFileDir(path)                      //显示子目录及文件
                        } else {
                            FileUtil.openFile(context, file)               //打开文件
                        }
                    } else {   //没有权限
                        DialogFragmentHelper.showTips(fragmentManager,"没有权限!")
                    }
                }
            }

            override fun onItemLongClick(viewHolder: RecyclerView.ViewHolder) {            //长按
                changeSelecFlag(viewHolder.adapterPosition)    //进入选择模式
            }
        })
        fmadapter = fmAdapter(context)
        mRecyclerView?.adapter = fmadapter           //设置adapter
        showFileDir(ROOT_PATH)
    }

    /**
     * 显示文件列表
     * @param path 文件路径
     */
    private fun showFileDir(path: String) {
        //重置选择
        SEARCH_SWITCH = 0
        currentpath = path

        mFiles = ArrayList()
        Pathnotes.clear()
        val file = File(path)
        val files = file.listFiles()
        val strpath = path.substring(path.indexOf("0"),path.length)
        Pathnotes = strpath.split("/") as ArrayList<String>  //分割路径
        pathadapter?.notifyDataSetChanged()          //更新路径
        files
                .filterNot {
                    it.isHidden      //筛选隐藏文件
                }
                .forEach {
                    val fb  = FileBean(it)
                    fb.initIcon(context)
                    cacheThreadPool.execute{
                        try {
                            fb.setSize(FileUtil.getAutoFileOrFilesSize(it.path))
                        }catch (e:Exception){
                            e.stackTrace
                        }
                    }
                    mFiles!!.add(fb) }

        fmadapter?.setListData(mFiles!!)
        Handler().postDelayed( { //消息处理延迟执行
            fmadapter?.notifyDataSetChanged()
        },800)
    }

    //提示信息
    private fun displaySnackbar(message: String) {
        SnackbarUtil.short(activity.window.decorView,message)
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
                        val fb = FileBean(it)
                        fb.initIcon(context)
                        cacheThreadPool.execute {
                            try {
                                fb.setSize(FileUtil.getAutoFileOrFilesSize(it.path))
                            } catch (e: Exception) {
                                e.stackTrace
                            }
                        }
                        fileBeans.add(fb)
                    }
                }
        return  fileBeans
    }

    /**
     * 获取被选文件
     */
    private fun getSelectedFiles():ArrayMap<Int,FileBean>{
        val files = ArrayMap<Int,FileBean>()
        for (isselect in fmAdapter.isSelected!!){
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
                fmadapter!!.initIsSelected()
                if (position!=null){
                    fmAdapter.isSelected!![position] = true
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

    /**
     * 定义Handler
     */
    private var handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            //关闭ProgressDialog
            val pdf = fragmentManager.findFragmentByTag(DialogFragmentHelper.getProgressTag()) as DialogFragment
            pdf.dismiss()
            //更新UI
            if(msg.arg1 == 1){
                fmadapter?.setListData(mFiles!!)
            }
            patharea!!.visibility = View.GONE
            pathtxt!!.visibility = View.VISIBLE
            if (fmadapter!!.itemCount == 0) {
                pathtxt!!.text = "无对应文件"
            } else {
                pathtxt!!.text=String.format(resources.getString(R.string.result_cout),mFiles?.size)
            }
        }
    }

    /**
     * 返回键事件
     */
    fun onBack():Boolean{    //返回键
        if(selectFlag == 1){          //退出选择模式
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

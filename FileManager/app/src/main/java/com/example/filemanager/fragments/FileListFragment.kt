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
import android.util.Log
import android.view.*
import android.widget.SearchView
import android.widget.TextView
import com.example.filemanager.*
import com.example.filemanager.activities.MainActivity
import com.example.filemanager.adapters.FMAdapter
import com.example.filemanager.adapters.FMAdapter.Companion.selectFlag
import com.example.filemanager.adapters.PathAdapter
import com.example.filemanager.utils.*
import java.io.File
import kotlin.collections.ArrayList

/**
 * Created by 11046 on 2017/4/24.
 * 文件列表
 */
class FileListFragment : Fragment() {
    private var mFiles: ArrayList<ExFile>? = null     //存储文件信息
    private var currentpath = FileUtil.ROOT_PATH        //当前文件路径
    private var mRecyclerView: RecyclerView? = null
    private var patharea: RecyclerView? = null
    private var pathtxt:TextView? = null
    private var SEARCH_SWITCH = 0
    private var pathadapter: PathAdapter? = null
    private var fmadapter: FMAdapter? = null
    private var mactivity: MainActivity? = null
    private val positioncache = MemoryCacheUtils()     //滚动条位置缓存
    var msg = Message()    //消息

    companion object {
        var selectedFiles: ArrayList<ExFile>? = null        //被选中的文件
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
                    ThreadPoolUtil.getThreadPool().execute{
                         try {
                             isFinished = true
                             var result:ArrayList<ExFile> = ArrayList()
                             while(!Thread.currentThread().isInterrupted){
                                 result = FileUtil.FileSearch(query, currentpath)  //执行查询
                                 Thread.currentThread().interrupt()
                             }
                             if(isFinished){  //正常结束
                                 mFiles = result
                                 msg.arg1 = 1

                                 SEARCH_SWITCH = 1
                             }
                         } catch(e:Exception) {
                             e.printStackTrace()
                             msg.arg1 = -1
                         } finally {
                             handler.sendMessage(msg)   //传递结果集
                             msg = Message()
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
            val exfiles = getSelectedFiles()
            var i = exfiles.size - 1
            DialogFragmentHelper.showConfirmDialog(fragmentManager,"确定要删除选中的文件吗？",
                    object :IDialogResultListener<Int>{
                        override fun onDataResult(result: Int) {
                            if (-1 == result){
                                try {
                                    while (i> -1 ) {
                                        if (FileUtil.deleteFile(exfiles.valueAt(i))) {
                                            fmadapter!!.removeItem(exfiles.keyAt(i)) //移除列表项
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
            mactivity?.invalidateOptionsMenu()
            true
        })
        when(selectFlag) {
            0 -> {
                menuItemS.isVisible = true
                menuItemD.isVisible = false
                menuItemA.isVisible = false
            }
            1 -> {
                menuItemS.isVisible = false
                menuItemD.isVisible = true
                menuItemA.isVisible = true
                menu.add(Menu.NONE, Menu.FIRST + 4, 4, "复制")
                menu.add(Menu.NONE, Menu.FIRST + 5, 6, "重命名")
                menu.add(Menu.NONE, Menu.FIRST + 6, 8, "压缩")
                if (fmadapter!!.getSelectCount() > 1) {
                    menu.findItem(Menu.FIRST + 5).isEnabled = false
                }
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
                                val newfilepath = "$currentpath/$result/"       //获取根目录
                                if (FileUtil.createMkdir(newfilepath)) {
                                    val newfb = ExFile(newfilepath)
                                    fmadapter!!.addItem(newfb)
                                } else {
                                    displaySnackbar(getString(R.string.isexits))
                                }
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
                            val targetpath = FileUtil.fixPath(currentpath + "/" + selectfile.name)
                            FileUtil.copy(selectfile.path, targetpath)
                            msg.arg1 = 2
                            val bundle = Bundle()
                            bundle.putString("path",targetpath)
                            msg.data = bundle
                        } catch (e: Exception) {
                            e.printStackTrace()
                            msg.arg1 = -1
                        } finally {
                            handler.sendMessage(msg)
                            msg = Message()
                        }
                    }
                })
                mthread.start()
            }
            Menu.FIRST + 4 -> {   //复制文件
                val exfiles = getSelectedFiles()
                selectedFiles = ArrayList()
                for (exfile in exfiles){
                    if (!exfile.value.exists()) {
                        displaySnackbar("复制失败")
                    }
                    selectedFiles!!.add(exfile.value)
                }
                displaySnackbar("文件已复制")
                fmadapter!!.changeSelecFlag()
                mactivity?.invalidateOptionsMenu()
            }
            Menu.FIRST + 5 ->{   //重命名
                val file = getSelectedFiles().valueAt(0)
                val position = getSelectedFiles().keyAt(0)
                DialogFragmentHelper.showInsertDialog(fragmentManager,"重命名文件",file.name,
                        object:IDialogResultListener<String>{
                            override fun onDataResult(result: String) {
                                var modifyName = result
                                val fpath = file.parentFile.path
                                var i = 0
                                var newFile = File("$fpath/$modifyName")
                                while (newFile.exists()){
                                    i++
                                    modifyName += "("+i.toString()+")"
                                    newFile = File("$fpath/$modifyName")
                                }
                                if (FileUtil.renameFile(file,newFile)) {
                                    mFiles!![position] = ExFile(newFile.path)
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
                val exfiles = getSelectedFiles()
                for (exfile in exfiles){
                    if (!exfile.value.exists()) {
                        displaySnackbar("压缩失败")
                    }
                    files.add(exfile.value)
                }

                DialogFragmentHelper.showInsertDialog(fragmentManager,"输入压缩文件名","",
                        object:IDialogResultListener<String>{
                    override fun onDataResult(result: String) {
                        val zippath = FileUtil.fixPath(currentpath + File.separator + result + ".zip")
                        if (FileUtil.zipFiles(files,zippath)) {
                            fmadapter?.changeSelecFlag()
                            fmadapter!!.addItem(ExFile(zippath))
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
                var topath = FileUtil.ROOT_PATH
                //根据点击的路径项得到跳转路径
                while (i <= viewHolder!!.adapterPosition) {
                    topath += "/" + Pathnotes[i]
                    i++
                }
                mactivity!!.invalidateOptionsMenu()
                showFileDir(topath)
            }

        })
        pathadapter = PathAdapter(context)
        patharea!!.adapter = pathadapter
        mRecyclerView!!.layoutManager = LinearLayoutManager(view.context)          //设置布局管理器
        mRecyclerView!!.itemAnimator = DefaultItemAnimator()               //设置Item增加、移除动画
        mRecyclerView!!.addOnItemTouchListener(object : OnRecyclerItemClickListener(mRecyclerView) {
            //设置点击与长按监听器
            override fun onItemClick(viewHolder: RecyclerView.ViewHolder) {        //点击
                if (selectFlag > 0) {           //选择模式下，点击选择文件项
                    FMAdapter.isSelected!![viewHolder.adapterPosition] = when (FMAdapter.isSelected!![viewHolder.adapterPosition]) {
                        true -> false
                        else -> true
                    }
                    fmadapter!!.notifyDataSetChanged()
                    mactivity?.invalidateOptionsMenu()
                } else {
                    Log.i("click",viewHolder.adapterPosition.toString())
                    val path = mFiles!![viewHolder.adapterPosition].path
                    val file = File(path)
                    if (file.exists() && file.canRead()) {  // 文件存在并可读
                        when {
                            file.isDirectory -> {   //文件夹
                                positioncache.setPositionToMemory(currentpath, viewHolder.adapterPosition)  //记录当前滚动位置
                                showFileDir(path) //显示子目录及文件
                            }
                            FileUtil.getType(file) == FileType.zip -> //压缩文件，解压到当前目录
                                DialogFragmentHelper.showConfirmDialog(fragmentManager,
                                        "是否解压文件到当前目录？",
                                        object : IDialogResultListener<Int> {
                                    override fun onDataResult(result: Int) {
                                        if (-1 == result){
                                            //显示ProgressDialog
                                            DialogFragmentHelper.showProgress(fragmentManager, "正在解压...", true,
                                                    object : CommonDialogFragment.OnDialogCancelListener {
                                                override fun onCancel() {
                                                    try {
                                                        displaySnackbar("解压已中断！")
                                                    } catch (e:Exception){
                                                        e.printStackTrace()
                                                    }
                                                }
                                            })
                                            val mthread = Thread(Runnable {
                                                //执行新线程
                                                try {
                                                    val targetpath = FileUtil.fixPath(currentpath + File.separator + file.nameWithoutExtension)
                                                    if(FileUtil.unzipFile(file.path,targetpath)){
                                                        msg.arg1 = 3
                                                    }else{
                                                        msg.arg1 = -1
                                                    }
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                    msg.arg1 = -1
                                                } finally {
                                                    handler.sendMessage(msg)
                                                    msg = Message()
                                                }
                                            })
                                            mthread.start()
                                        }else{
                                            displaySnackbar("操作取消")
                                        }
                                    }
                                },true,null)
                            else -> FileUtil.openFile(context, file)               //打开文件
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
        fmadapter = FMAdapter(context)
        mRecyclerView?.adapter = fmadapter           //设置adapter
        showFileDir(FileUtil.ROOT_PATH)
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
        val strpath = path.substring(path.indexOf("0"),path.length)
        Pathnotes.addAll(strpath.split("/"))
        pathadapter?.notifyDataSetChanged()          //更新路径
        file.listFiles()
                .filterNot {
                    it.isHidden      //筛选隐藏文件
                }
                .forEach {
                    val ef  = ExFile(it.path)
                    mFiles!!.add(ef) }

        fmadapter?.setListData(mFiles!!)
        val position = positioncache.getPositionFromMemory(path)
        if (position != null) {
            mRecyclerView?.scrollToPosition(position as Int)
        }
        Handler().postDelayed({
            //延迟执行
            fmadapter?.notifyDataSetChanged()  //更新列表数据
        },1500)
    }

    //提示信息
    private fun displaySnackbar(message: String) {
        SnackbarUtil.short(activity.window.decorView,message)
    }

    /**
     * 获取被选文件
     */
    private fun getSelectedFiles():ArrayMap<Int,ExFile>{
        val files = ArrayMap<Int,ExFile>()
        for (isselect in FMAdapter.isSelected!!){
            if(isselect.value){
                files[isselect.key] = mFiles!![isselect.key]
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
                    FMAdapter.isSelected!![position] = true
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
        if(fmadapter!!.getSelectCount()< FMAdapter.isSelected!!.size){
            for (isselect in FMAdapter.isSelected!!){
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
            when {
                msg.arg1 == -1 -> {
                    displaySnackbar("操作出错")
                }
                msg.arg1 == 1 -> {
                    fmadapter?.setListData(mFiles!!)

                    patharea!!.visibility = View.GONE
                    pathtxt!!.visibility = View.VISIBLE
                    if (fmadapter!!.itemCount == 0) {
                        pathtxt!!.text = "无对应文件"
                    } else {
                        pathtxt!!.text=String.format(resources.getString(R.string.result_cout),mFiles?.size)
                    }
                }
                msg.arg1 == 2 -> {
                    val targetpath = msg.data.getString("path")
                    fmadapter!!.addItem(ExFile(targetpath))

                    if(MediaUtil.isMediaFile(targetpath)){
                        MediaUtil.sendScanFileBroadcast(targetpath)
                    }
                    displaySnackbar("粘贴成功")
                }
                msg.arg1 == 3 -> {
                    showFileDir(currentpath)
                    displaySnackbar("解压完成")
                }
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
        } else if (currentpath!=FileUtil.ROOT_PATH){    //返回上级目录
            val file = File(currentpath)
            showFileDir(file.parent)
            return true
        }
        return false
    }

}

package com.example.filemanager.fragments

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.annotation.Nullable
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import com.example.filemanager.utils.FileSortUtil
import com.example.filemanager.activities.SortActivity
import com.example.filemanager.adapters.fsAdapter
import com.example.filemanager.OnRecyclerItemClickListener
import com.example.filemanager.R
import android.content.Context
import android.content.IntentFilter

/**
 * Created by 11046 on 2017/4/29.
 * 文件分类
 */
class FileSortFragment:Fragment() {
    private var mRecyclerView: RecyclerView? = null
    private var fsadapter: fsAdapter? = null
    private var myreceiver:myReceiver?=null

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        myreceiver = myReceiver()
        val filter = IntentFilter("com.11046")
        context.registerReceiver(myreceiver,filter)    //注册广播接收器
        FileSortUtil.AsyncSortTask(handler).execute(context)
    }

    fun newInstance(): FileSortFragment {
        val args = Bundle()
        val filesortFragment = FileSortFragment()
        filesortFragment.arguments = args
        return filesortFragment
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val sortview: View? = inflater?.inflate(R.layout.filesort_layout, container, false)
        initsorts(sortview!!)
        return sortview
    }

    /**
     * 初始化分类页面
     */
    private fun initsorts(view: View){
        mRecyclerView = view.findViewById(R.id.recycleview) as RecyclerView
        mRecyclerView!!.layoutManager = GridLayoutManager(view.context,2)          //设置布局管理器
        mRecyclerView!!.itemAnimator = DefaultItemAnimator()               //设置Item增加、移除动画
        mRecyclerView!!.addOnItemTouchListener(object : OnRecyclerItemClickListener(mRecyclerView) {
            override fun onItemLongClick(viewHolder: RecyclerView.ViewHolder?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onItemClick(viewHolder: RecyclerView.ViewHolder) {        //点击
                //跳转浏览该类文件
                val intent = Intent()
                intent.setClass(activity,SortActivity::class.java)
                val bundle = Bundle()
                bundle.putInt("sort",viewHolder.adapterPosition)
                intent.putExtras(bundle)
                startActivity(intent)
                }
            })
        fsadapter= fsAdapter(context)
        mRecyclerView!!.adapter=fsadapter  //设置adapter
    }

    /**
     * 定义Handler
     */
    private var handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            //更新UI
            fsadapter!!.notifyDataSetChanged()
        }
    }

    /**
     *定义 广播接收器
     */
    inner class myReceiver: BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            //更新UI
            fsadapter!!.notifyDataSetChanged()
        }
    }

    /**
     * 销毁事件
     */
    override fun onDestroy() {
        try{
            context.unregisterReceiver(myreceiver)      //注销广播接收器
        }catch(e:Exception) {
            if (e.message!!.contains("Receiver not registered")) {
                // Ignore this exception. This is exactly what is desired
            } else {
                // unexpected, re-throw
                throw e
            }
        }
        super.onDestroy()
    }
}
package com.example.filemanager.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.filemanager.utils.FileSortUtil
import com.example.filemanager.FileType
import com.example.filemanager.R

internal class fsAdapter //参数初始化
(context: Context) : RecyclerView.Adapter<fsAdapter.myViewHolder>() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val Sortstxt = arrayOf("文档","下载","音乐","图片","视频","压缩包","安装包")
    private val Sortsimg = arrayOf(R.drawable.doc, R.drawable.download, R.drawable.music, R.drawable.img, R.drawable.video, R.drawable.zip, R.drawable.apk)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)=myViewHolder(inflater.inflate(R.layout.sort, parent, false))

    override fun onBindViewHolder(holder: myViewHolder, position: Int) {
        holder.text.text = Sortstxt[position]
        holder.image.setImageResource(Sortsimg[position])
        holder.count.text= "("+ FileSortUtil().getTypeCount(FileType.getFileTypeByOrdinal(position)).toString()+")"
    }

    override fun getItemId(position: Int) = position.toLong()

    override fun getItemCount() = Sortstxt.size

    internal inner class myViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
        internal val text: TextView = view.findViewById(R.id.tvpart) as TextView
        internal val count: TextView = view.findViewById(R.id.countpart) as TextView
        val image: ImageView = view.findViewById(R.id.imgpart) as ImageView
    }

}
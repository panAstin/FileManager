package com.example.filemanager.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.filemanager.R
import com.example.filemanager.fragments.FileListFragment
import java.util.ArrayList

internal class pathAdapter//参数初始化
(context: Context) : RecyclerView.Adapter<pathAdapter.myViewHolder>() {
    private val inflater: LayoutInflater


    init {
        inflater = LayoutInflater.from(context)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)= myViewHolder(inflater.inflate(R.layout.path, parent, false))


    override fun onBindViewHolder(holder: myViewHolder, position: Int) {
        holder.text.text = FileListFragment.Pathnotes[position]
    }

    override fun getItemId(position: Int)= position.toLong()

    override fun getItemCount()= FileListFragment.Pathnotes.size

    internal inner class myViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
        internal val text: TextView = view.findViewById(R.id.path_tv) as TextView
        private val image: ImageView = view.findViewById(R.id.path_img) as ImageView
    }

}

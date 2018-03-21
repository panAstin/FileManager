package com.example.filemanager.adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.example.filemanager.fragments.FileListFragment
import com.example.filemanager.fragments.FileSortFragment

class FragmentPageAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
    private val COUNT = 2
    private val title=arrayOf("文件列表", "文件分类")

    override fun getItem(position: Int): Fragment? {
        when(position){
            0 -> return FileListFragment().newInstance()
            1 -> return FileSortFragment().newInstance()
        }
        return null
    }

    override fun getCount() = COUNT

    override fun getPageTitle(position: Int) = title[position]
}
package com.example.filemanager

import com.example.filemanager.utils.FileUtil
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by 11046 on 2017/9/27.
 */
class FileBean(file: File) {
    private var name:String = ""
    private var path:String = ""
    private var date:String = ""
    private var size:String = ""
    private var iconID :Int = 0
    private var format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")   //时间格式
    private val txttypes = arrayOf("text/plain", "application/msword", "application/pdf", "application/vnd.ms-powerpoint", "application/vnd.ms-excel") //文档类型

    fun setName(name:String){
        this.name = name
    }

    fun setPath(path:String){
        this.path = path
    }

    fun setDate(date:String){
        this.date = date
    }

    fun setSize(size:String){
        this.size = size
    }

    fun getName():String = name

    fun getPath():String = path

    fun getDate():String = date

    fun getSize():String = size

    fun getIconID(): Int = iconID

    fun getFile():File = File(path)

    private fun initIcon(file: File){
        val type = FileUtil.getMIMEType(file)
        iconID=when{
            file.isDirectory ->0
            file.isFile -> when {
                txttypes.contains(type) -> 2
                type.contains("audio") or (type == "application/ogg") -> 3
                type.contains("video") -> 4
                type == "application/x-zip-compressed" -> 5
                type == "application/vnd.android.package-archive" -> 6
                type.contains("image") ->7
                else -> 1
            }
            else -> 1
        }
    }

    init {
        this.name = file.name
        this.path = file.path
        this.date = format.format(Date(file.lastModified()))
        this.size = FileUtil.getAutoFileOrFilesSize(file.path)
        initIcon(file)
    }

}
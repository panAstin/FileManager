package com.example.filemanager

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.filemanager.utils.FileUtil
import com.example.filemanager.utils.MemoryCacheUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by 11046 on 2017/9/27.
 * 封装文件与文件大小、文件图标
 */
class FileBean(private var file: File) {
    companion object {
        private var iconCache:MemoryCacheUtils? = null        //图片缓存

        fun getCache():MemoryCacheUtils {
            if (iconCache==null){
                iconCache = MemoryCacheUtils()
            }
            return iconCache!!
        }
    }
    private var date:String = ""
    private var size:String = ""
    private var typeID:Int = 0
    private var icon:Bitmap?  = null
    private var format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")   //时间格式
    private val Icons = arrayOf(R.drawable.dict,R.drawable.file,R.drawable.doc,  R.drawable.music, R.drawable.video, R.drawable.zip, R.drawable.apk)
    private val txttypes = arrayOf("text/plain", "application/msword", "application/pdf", "application/vnd.ms-powerpoint", "application/vnd.ms-excel") //文档类型

    fun setDate(){
        this.date = format.format(Date(file.lastModified()))
    }

    fun setSize(size:String){
        this.size = size
    }

    fun setIcon(icon:Bitmap){
        this.icon = icon
    }

    fun getDate():String = date

    fun getSize():String = size

    fun getTypeID(): Int = typeID

    fun getIcon(): Bitmap? = icon

    fun getFile():File = file

    private fun initType(file: File){
        val type = FileUtil.getMIMEType(file)
        typeID=when{
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

    fun initIcon(context: Context){
            if (typeID<7) {
                val iconincache = getCache().getBitmapFromMemory(typeID)
                icon = if(iconincache!=null){
                    iconincache
                }else{
                    val iconbitmap = BitmapFactory.decodeResource(context.resources, Icons[typeID])
                    getCache().setBitmapToMemory(typeID,iconbitmap)
                    iconbitmap
                }
        }
    }

    fun getInitailed(context:Context):FileBean{
        this.initIcon(context)
        this.setSize(FileUtil.getAutoFileOrFilesSize(this.file.path))
        return this
    }

    init {
        this.date = format.format(Date(file.lastModified()))
        this.size = "计算中.."
        initType(file)
    }

}
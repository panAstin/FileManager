package com.example.filemanager.utils

import android.content.ContentResolver
import android.content.Context
import android.os.AsyncTask
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.util.ArrayMap
import android.util.Log
import com.example.filemanager.FileBean
import com.example.filemanager.FileType
import java.io.File


/**
 * Created by 11046 on 2017/9/23.
 * 分类相关
 */

class FileSortUtil{
    private var mContentResolver: ContentResolver? = null
    companion object {
        val mAllFilePaths = ArrayMap<FileType,ArrayList<FileBean>>()

        //删除
        fun deleteFile(file:File){
            val filebean = FileBean(file)
            if (file.parent == Environment.getExternalStorageDirectory().path+"/Download" ){
                var i=0
                while(i<mAllFilePaths[FileType.download]!!.size) {
                    if(filebean.getFile() == mAllFilePaths[FileType.download]!![i].getFile()){
                        mAllFilePaths[FileType.download]!!.removeAt(i)
                        break
                    }
                    i++
                }
            }
            val type = when(filebean.getIconID()){
                2 -> FileType.text
                3 -> FileType.music
                4 -> FileType.video
                5 -> FileType.zip
                6 -> FileType.apk
                7 -> FileType.photo
                else -> null
            }
            if (type != null){
                var i=0
                while(i<mAllFilePaths[type]!!.size) {
                    if(filebean.getFile() == mAllFilePaths[type]!![i].getFile()){
                        mAllFilePaths[type]!!.removeAt(i)
                        break
                    }
                    i++
                }
            }
        }
    }

    /**
     * 添加文件
     * @param type 文件类型
     * @param fileBean 文件
     */
    fun addFileByType(type: FileType?, fileBean: FileBean?) {
        if (type == null || fileBean == null) {
            return
        }
        var fileBeans = mAllFilePaths[type]

        if (fileBeans == null) {
            fileBeans = ArrayList()
            mAllFilePaths.put(type, fileBeans)
        }
        fileBeans.add(fileBean)
    }

    /**
     * 添加文件
     * @param type 文件类型
     * @param fileBean 文件集合
     */
     private fun addFilePathsByType(type: FileType?, fileBean: ArrayList<FileBean>?) {
        if (type == null || fileBean == null) {
            return
        }
        var fileBeans = mAllFilePaths[type]

        if (fileBeans == null) {
            fileBeans = ArrayList()
            mAllFilePaths.put(type, fileBeans)
        }
         fileBeans.addAll(fileBean)
    }

    /**
     * 获取文件
     * @param fileType 文件类型
     * @return 文件集合
     */
    fun getFilePathsByType(fileType: FileType?): ArrayList<FileBean>? {
        return if (fileType == null) {
            null
        } else mAllFilePaths[fileType]

    }

    /**
     * 获取某类文件计数
     */
    fun getTypeCount(fileType: FileType)= mAllFilePaths[fileType]?.size ?: 0

    /**
     * 销毁分类集合
     */
    fun destory() {
        mAllFilePaths.clear()
    }

    /**
     * 获取图片类型文件
     */
    private //projection 是定义返回的数据，selection 通常的sql 语句，例如  selection=MediaStore.Images.ImageColumns.MIME_TYPE+"=? " 那么 selectionArgs=new String[]{"jpg"};
    val allPhoto: ArrayList<FileBean>
        get() {
            val photos = ArrayList<FileBean>()
            val projection = arrayOf(MediaStore.Images.ImageColumns.DATA, MediaStore.Images.ImageColumns.DISPLAY_NAME)
            val cursor = mContentResolver!!.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Images.ImageColumns.DATE_MODIFIED + "  desc")
            var filePath: String
            while (cursor!!.moveToNext()) {
                filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA))
                if (!File(filePath).isHidden){
                    photos.add(FileBean(File(filePath)))
                }
            }
            cursor.close()
            return photos
        }

    /**
     * 获取音乐类型文件
     */
    private val allMusic: ArrayList<FileBean>
        get() {
            val musics = ArrayList<FileBean>()
            val projection = arrayOf(MediaStore.Audio.AudioColumns.DATA, MediaStore.Audio.AudioColumns.DISPLAY_NAME)
            val cursor = mContentResolver!!.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Audio.AudioColumns.DATE_MODIFIED + " desc")
            var filePath: String
            while (cursor!!.moveToNext()) {
                filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA))
                if (!File(filePath).isHidden){
                    musics.add(FileBean(File(filePath)))
                }
            }
            cursor.close()
            return musics
        }

    /**
     * 获取下载文件夹中文件
     */
    private val allDownload: ArrayList<FileBean>
        get() {
            val downloads = ArrayList<FileBean>()
            val files = File(Environment.getExternalStorageDirectory().path+"/Download" ).listFiles()
            files
                    .filterNot { it.isHidden }
                    .mapTo(downloads) { FileBean(it) }
            return downloads
        }

    /**
     * 获取视频类型文件
     */
    private val allVideo: ArrayList<FileBean>
        get() {
            val videos = ArrayList<FileBean>()
            val projection = arrayOf(MediaStore.Video.VideoColumns.DATA, MediaStore.Video.VideoColumns.DISPLAY_NAME)
            val cursor = mContentResolver!!.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Video.VideoColumns.DATE_MODIFIED + " desc")
            var filePath: String
            while (cursor!!.moveToNext()) {
                filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATA))
                if (!File(filePath).isHidden){
                    videos.add(FileBean(File(filePath)))
                }
            }
            cursor.close()
            return videos
        }

    /**
     * 获取文本类型文件
     */
    private val allText: ArrayList<FileBean>
        get() {
            val texts = ArrayList<FileBean>()
            val projection = arrayOf(MediaStore.Files.FileColumns.DATA, MediaStore.Files.FileColumns.TITLE, MediaStore.Files.FileColumns.MIME_TYPE)
            val selection = (MediaStore.Files.FileColumns.MIME_TYPE + "= ? "
                    + " or " + MediaStore.Files.FileColumns.MIME_TYPE + " = ? "
                    + " or " + MediaStore.Files.FileColumns.MIME_TYPE + " = ? "
                    + " or " + MediaStore.Files.FileColumns.MIME_TYPE + " = ? "
                    + " or " + MediaStore.Files.FileColumns.MIME_TYPE + " = ? ")
            val selectionArgs = arrayOf("text/plain", "application/msword", "application/pdf", "application/vnd.ms-powerpoint", "application/vnd.ms-excel")
            val cursor = mContentResolver!!.query(MediaStore.Files.getContentUri("external"), projection, selection, selectionArgs, MediaStore.Files.FileColumns.DATE_MODIFIED + " desc")
            var filePath: String
            while (cursor!!.moveToNext()) {
                filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA))
                if (!File(filePath).isHidden){
                    texts.add(FileBean(File(filePath)))
                }
            }
            cursor.close()
            return texts
        }

    /**
     * 获取压缩包类型文件
     */
    private val allZip: ArrayList<FileBean>
        get() {
            val zips = ArrayList<FileBean>()
            val projection = arrayOf(MediaStore.Files.FileColumns.DATA, MediaStore.Files.FileColumns.TITLE)
            val selection = MediaStore.Files.FileColumns.MIME_TYPE + "= ? "
            val selectionArgs = arrayOf("application/zip")
            val cursor = mContentResolver!!.query(MediaStore.Files.getContentUri("external"), projection, selection, selectionArgs, MediaStore.Files.FileColumns.DATE_MODIFIED + " desc")
            var filePath: String
            while (cursor!!.moveToNext()) {
                filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA))
                if (!File(filePath).isHidden){
                    zips.add(FileBean(File(filePath)))
                }
            }
            cursor.close()
            return zips
        }

    /**
     * 获取安装包类型文件
     */
    private val allApk: ArrayList<FileBean>
        get() {
            val apks = ArrayList<FileBean>()
            val projection = arrayOf(MediaStore.Files.FileColumns.DATA, MediaStore.Files.FileColumns.TITLE)
            val selection = MediaStore.Files.FileColumns.MIME_TYPE + "= ? "
            val selectionArgs = arrayOf("application/vnd.android.package-archive")
            val cursor = mContentResolver!!.query(MediaStore.Files.getContentUri("external"), projection, selection, selectionArgs, MediaStore.Files.FileColumns.DATE_MODIFIED + " desc")
            var filePath: String
            while (cursor!!.moveToNext()) {
                filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA))
                if (!File(filePath).isHidden){
                    apks.add(FileBean(File(filePath)))
                }
            }
            cursor.close()
            return apks
        }

    /**
     * 异步初始化数据
     */
    class AsyncSortTask(val handler: Handler) : AsyncTask<Context, Void, Int>() {
        override fun doInBackground(vararg params: Context): Int? {//子线程中执行的
            try {
                FileSortUtil().addAlltype(params.component1().contentResolver)
                return 1
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(result:Int) {//运行在主线程
            Log.i("filehandle","over")
            val msg = handler.obtainMessage()
            if (result != 1) {
                msg.what = 1
                msg.obj = result
            } else {
                msg.what = 2
            }
            handler.sendMessage(msg)
        }
    }

    //初始化全部分类文件
    fun addAlltype(contentResolver: ContentResolver){
        mContentResolver=contentResolver
        addFilePathsByType(FileType.text,allText)
        addFilePathsByType(FileType.download,allDownload)
        addFilePathsByType(FileType.music,allMusic)
        addFilePathsByType(FileType.photo,allPhoto)
        addFilePathsByType(FileType.video,allVideo)
        addFilePathsByType(FileType.zip,allZip)
        addFilePathsByType(FileType.apk,allApk)
    }

}
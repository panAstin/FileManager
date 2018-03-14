package com.example.filemanager.utils

import android.content.ContentResolver
import android.content.Context
import android.os.AsyncTask
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.util.ArrayMap
import android.util.Log
import com.example.filemanager.ExFile
import com.example.filemanager.FileType
import java.io.File


/**
 * Created by 11046 on 2017/9/23.
 * 分类相关
 */

class FileSortUtil{
    private var mContentResolver: ContentResolver? = null
    companion object {
        val mAllFiles = ArrayMap<FileType,ArrayList<ExFile>>()

        //删除
        fun deleteFile(file:File){
            val exFile = ExFile(file.path)
            if (file.parent == Environment.getExternalStorageDirectory().path+"/Download" ){
                var i=0
                while(i<mAllFiles[FileType.download]!!.size) {
                    if(exFile == mAllFiles[FileType.download]!![i]){
                        mAllFiles[FileType.download]!!.removeAt(i)
                        break
                    }
                    i++
                }
            }
            val type = when(exFile.getTypeID()){
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
                while(i<mAllFiles[type]!!.size) {
                    if(exFile == mAllFiles[type]!![i]){
                        mAllFiles[type]!!.removeAt(i)
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
    fun addFileByType(type: FileType?, exFile: ExFile?) {
        if (type == null || exFile == null) {
            return
        }
        var fileBeans = mAllFiles[type]

        if (fileBeans == null) {
            fileBeans = ArrayList()
            mAllFiles.put(type, fileBeans)
        }
        fileBeans.add(exFile)
    }

    /**
     * 添加文件
     * @param type 文件类型
     * @param fileBean 文件集合
     */
     private fun addFilesByType(type: FileType?, fileBean: ArrayList<ExFile>?) {
        if (type == null || fileBean == null) {
            return
        }
        var exFile = mAllFiles[type]

        if (exFile == null) {
            exFile = ArrayList()
            mAllFiles[type] = exFile
        }
        exFile.addAll(fileBean)
    }

    /**
     * 获取文件
     * @param fileType 文件类型
     * @return 文件集合
     */
    fun getFilesByType(fileType: FileType?): ArrayList<ExFile>? {
        return if (fileType == null) {
            null
        } else mAllFiles[fileType]

    }

    /**
     * 获取某类文件计数
     */
    fun getTypeCount(fileType: FileType)= mAllFiles[fileType]?.size ?: 0

    /**
     * 销毁分类集合
     */
    fun destory() {
        mAllFiles.clear()
    }

    /**
     * 获取图片类型文件
     */
    private //projection 是定义返回的数据，selection 通常的sql 语句，例如  selection=MediaStore.Images.ImageColumns.MIME_TYPE+"=? " 那么 selectionArgs=new String[]{"jpg"};
    val allPhoto: ArrayList<ExFile>
        get() {
            val photos = ArrayList<ExFile>()
            val projection = arrayOf(MediaStore.Images.ImageColumns.DATA, MediaStore.Images.ImageColumns.DISPLAY_NAME)
            val cursor = mContentResolver!!.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Images.ImageColumns.DATE_MODIFIED + "  desc")
            var filePath: String
            while (cursor!!.moveToNext()) {
                filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA))
                if (!File(filePath).isHidden){
                    photos.add(ExFile(filePath))
                }
            }
            cursor.close()
            return photos
        }

    /**
     * 获取音乐类型文件
     */
    private val allMusic: ArrayList<ExFile>
        get() {
            val musics = ArrayList<ExFile>()
            val projection = arrayOf(MediaStore.Audio.AudioColumns.DATA, MediaStore.Audio.AudioColumns.DISPLAY_NAME)
            val cursor = mContentResolver!!.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Audio.AudioColumns.DATE_MODIFIED + " desc")
            var filePath: String
            while (cursor!!.moveToNext()) {
                filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA))
                if (!File(filePath).isHidden){
                    musics.add(ExFile(filePath))
                }
            }
            cursor.close()
            return musics
        }

    /**
     * 获取下载文件夹中文件
     */
    private val allDownload: ArrayList<ExFile>
        get() {
            val downloads = ArrayList<ExFile>()
            val files = File(Environment.getExternalStorageDirectory().path+"/Download" ).listFiles()
            files
                    .filterNot { it.isHidden }
                    .mapTo(downloads) { ExFile(it.path) }
            return downloads
        }

    /**
     * 获取视频类型文件
     */
    private val allVideo: ArrayList<ExFile>
        get() {
            val videos = ArrayList<ExFile>()
            val projection = arrayOf(MediaStore.Video.VideoColumns.DATA, MediaStore.Video.VideoColumns.DISPLAY_NAME)
            val cursor = mContentResolver!!.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Video.VideoColumns.DATE_MODIFIED + " desc")
            var filePath: String
            while (cursor!!.moveToNext()) {
                filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATA))
                if (!File(filePath).isHidden){
                    videos.add(ExFile(filePath))
                }
            }
            cursor.close()
            return videos
        }

    /**
     * 获取文本类型文件
     */
    private val allText: ArrayList<ExFile>
        get() {
            val texts = ArrayList<ExFile>()
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
                    texts.add(ExFile(filePath))
                }
            }
            cursor.close()
            return texts
        }

    /**
     * 获取压缩包类型文件
     */
    private val allZip: ArrayList<ExFile>
        get() {
            val zips = ArrayList<ExFile>()
            val projection = arrayOf(MediaStore.Files.FileColumns.DATA, MediaStore.Files.FileColumns.TITLE)
            val selection = MediaStore.Files.FileColumns.MIME_TYPE + "= ? "
            val selectionArgs = arrayOf("application/zip")
            val cursor = mContentResolver!!.query(MediaStore.Files.getContentUri("external"), projection, selection, selectionArgs, MediaStore.Files.FileColumns.DATE_MODIFIED + " desc")
            var filePath: String
            while (cursor!!.moveToNext()) {
                filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA))
                if (!File(filePath).isHidden){
                    zips.add(ExFile(filePath))
                }
            }
            cursor.close()
            return zips
        }

    /**
     * 获取安装包类型文件
     */
    private val allApk: ArrayList<ExFile>
        get() {
            val apks = ArrayList<ExFile>()
            val projection = arrayOf(MediaStore.Files.FileColumns.DATA, MediaStore.Files.FileColumns.TITLE)
            val selection = MediaStore.Files.FileColumns.MIME_TYPE + "= ? "
            val selectionArgs = arrayOf("application/vnd.android.package-archive")
            val cursor = mContentResolver!!.query(MediaStore.Files.getContentUri("external"), projection, selection, selectionArgs, MediaStore.Files.FileColumns.DATE_MODIFIED + " desc")
            var filePath: String
            while (cursor!!.moveToNext()) {
                filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA))
                if (!File(filePath).isHidden){
                    apks.add(ExFile(filePath))
                }
            }
            cursor.close()
            return apks
        }

    /**
     * 异步初始化数据
     */
    class AsyncSortTask(private val handler: Handler) : AsyncTask<Context, Void, Int>() {
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
        addFilesByType(FileType.text,allText)
        addFilesByType(FileType.download,allDownload)
        addFilesByType(FileType.music,allMusic)
        addFilesByType(FileType.photo,allPhoto)
        addFilesByType(FileType.video,allVideo)
        addFilesByType(FileType.zip,allZip)
        addFilesByType(FileType.apk,allApk)
    }

}
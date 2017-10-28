package com.example.filemanager.utils

import android.text.TextUtils
import android.provider.MediaStore.Video
import android.provider.MediaStore.Audio
import android.provider.MediaStore.Images
import android.content.Context
import android.media.MediaScannerConnection
import android.media.MediaScannerConnection.OnScanCompletedListener
import android.content.Intent
import android.net.Uri
import android.util.Log
import java.io.File


/**
 * Created by 11046 on 2017/9/26.
 */
object MediaUtil {
    private val LOGTAG = "MediaUtils"

    /**
     * 通过广播进行多媒体库的扫描
     * 对方成功接收广播并处理条件  文件必须存在，文件路径必须以Environment.getExternalStorageDirectory().getPath() 的返回值开头
     */
    fun sendScanFileBroadcast(context: Context, filePath: String) {
        val file = File(filePath)
        var intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file))
        context.sendBroadcast(intent)
        intent = Intent("com.11046")
        context.sendBroadcast(intent)              //发送广播进行列表更新
    }

    /**
     *
     * @param context
     * @param paths 扫描的文件路径
     * @param mimeTypes 文件的minetype,可以为空
     * @param callback
     */
    @JvmOverloads
    fun scanFiles(context: Context, paths: Array<String>?, mimeTypes: Array<String>? = null, callback: OnScanCompletedListener? = null) {
        if (null != paths && paths.isNotEmpty()) {
            MediaScannerConnection.scanFile(context, paths, mimeTypes, callback)
        } else {
            Log.i(LOGTAG, "scanFiles paths = null or paths.length=0 paths=" + paths!!)
        }
    }

    /**
     * 从库中移除文件
     */
    private fun removeImageFromLib(context: Context, filePath: String): Int {
        return context.contentResolver.delete(Images.Media.EXTERNAL_CONTENT_URI,
                Images.Media.DATA + "=?", arrayOf(filePath))
    }

    private fun removeAudioFromLib(context: Context, filePath: String): Int {
        return context.contentResolver.delete(Audio.Media.EXTERNAL_CONTENT_URI,
                Audio.Media.DATA + "=?", arrayOf(filePath))
    }

    private fun removeVideoFromLib(context: Context, filePath: String): Int {
        return context.contentResolver.delete(Video.Media.EXTERNAL_CONTENT_URI,
                Video.Media.DATA + "=?", arrayOf(filePath))

    }

    fun removeMediaFromLib(context: Context, filePath: String): Int {
        var mimeType = FileUtil.getMIMEType(File(filePath))
        var affectedRows = 0
        if ("*/*" != mimeType) {
            mimeType = mimeType.toLowerCase()
            affectedRows = when{
                isImage(mimeType) -> removeImageFromLib(context, filePath)
                isAudio(mimeType) -> removeAudioFromLib(context, filePath)
                isVideo(mimeType) -> removeVideoFromLib(context, filePath)
                else -> 0
            }
        }
        sendScanFileBroadcast(context, filePath)
        return affectedRows
    }

    /**
     * 判断文件为何种多媒体类型
     */
    private fun isAudio(mimeType: String)=mimeType.startsWith("audio")

    private fun isImage(mimeType: String)=mimeType.startsWith("image")

    private fun isVideo(mimeType: String)=mimeType.startsWith("video")

    fun isMediaFile(filePath: String): Boolean {
        val mimeType = FileUtil.getMIMEType(File(filePath))
        return isMediaType(mimeType)
    }

    private fun isMediaType(mimeType: String): Boolean {
        var mimeType = mimeType
        var isMedia = false
        if (!TextUtils.isEmpty(mimeType)) {
            mimeType = mimeType.toLowerCase()
            isMedia = isImage(mimeType) || isAudio(mimeType) || isVideo(mimeType)
        }
        return isMedia
    }

    /**
     * 重命名媒体文件
     * @param context
     * @param srcPath
     * @param destPath
     * @return
     */
    fun renameMediaFile(context: Context, srcPath: String, destPath: String): Int {
        removeMediaFromLib(context, srcPath)
        sendScanFileBroadcast(context, destPath)
        return 0
    }

}

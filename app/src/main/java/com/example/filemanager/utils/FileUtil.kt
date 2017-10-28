package com.example.filemanager.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Handler
import android.support.v4.content.FileProvider
import android.util.Log

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.DecimalFormat

object FileUtil {
    /**
     * 调用此方法自动计算指定文件或指定文件夹的大小
     * @param filePath 文件路径
     * *
     * @return 计算好的带B、KB、MB、GB的字符串
     */
    fun getAutoFileOrFilesSize(filePath: String): String {
        val file = File(filePath)
        var blockSize: Long = 0
        try {
            blockSize = if (file.isDirectory) {
                getFileSizes(file)
            } else {
                getFileSize(file)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("获取文件大小", "获取失败!")
        }
        return formetFileSize(blockSize)
    }

    /**
     * 获取指定文件大小
     * @param file 指定文件
     */
    @Throws(Exception::class)
    private fun getFileSize(file: File): Long {
        var size: Long = 0
        if (file.exists()) {
            val fis = FileInputStream(file)
            size = fis.available().toLong()
            fis.close()
        } else {
            file.createNewFile()
            Log.e("获取文件大小", "文件不存在!")
        }
        return size
    }

    /**
     * 获取指定文件夹大小
     * @param f 指定文件夹
     */
    @Throws(Exception::class)
    private fun getFileSizes(f: File): Long {
        val flist = f.listFiles()
        return flist
                .map {
                    when (it.isDirectory) {
                        true -> getFileSizes(it)
                        false -> getFileSize(it)
                    }
                }
                .sum()
    }

    /**
     * 转换文件大小
     * @param fileS 文件大小
     */
    private fun formetFileSize(fileS: Long): String {
        val df = DecimalFormat("#.00")
        val fileSizeString: String
        val wrongSize = "0B"
        if (fileS == 0L) {
            return wrongSize
        }
        fileSizeString = when {
            fileS < 1024 -> df.format(fileS.toDouble()) + "B"
            fileS < 1048576 -> df.format(fileS.toDouble() / 1024) + "KB"
            fileS < 1073741824 -> df.format(fileS.toDouble() / 1048576) + "MB"
            else -> df.format(fileS.toDouble() / 1073741824) + "GB"
        }
        return fileSizeString
    }

    /**
     * 根据文件类型打开文件
     * @param context
     * @param file 文件
     */
     fun openFile(context:Context,file: File) {
        val intent = Intent()
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.action = android.content.Intent.ACTION_VIEW
        val type = getMIMEType(file)
        val data : Uri
        // 判断版本大于等于7.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // "com.11046.panAstin"即是在清单文件中配置的authorities
            data = FileProvider.getUriForFile(context, "com.11046.panAstin", file)
            // 给目标应用一个临时授权
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } else {
            data = Uri.fromFile(file)
        }
        intent.setDataAndType(data, type)
        try {
            context.startActivity(intent)
        } catch (e:Exception){
            Log.i("eee",e.message)
        }
    }

    /**
     * 删除文件
     * @param context
     * @param file 文件
     */
    fun deleteFile(context: Context,file: File):Boolean{
        if(file.isDirectory){  //文件夹
            val files = file.listFiles()
            for (mfile in files){
                deleteFile(context,mfile)
            }
            return file.delete()
        }
        if (file.isFile){
            FileSortUtil.deleteFile(file)
            MediaUtil.removeMediaFromLib(context, file.path)       //从多媒体库中移除
            file.delete()
            return !file.exists()
        }
        return false
    }

    /**
     * 重命名文件
     * @param context
     * @param oldfile 原文件
     * @param newfile 重命名后文件
     */
    fun renameFile(context: Context,oldfile: File,newfile:File):Boolean{
        if(oldfile.renameTo(newfile)){
            if (MediaUtil.isMediaFile(newfile.path)){
                MediaUtil.renameMediaFile(context, oldfile.path, newfile.path)  //修改多媒体库中文件名
            }
            return true
        }
        return false
    }

    /**
     * 新建文件夹
     * @param folderPath 文件夹路径
     */
     fun createMkdir(folderPath: String) {
        val folder = File(folderPath)
        if (!folder.exists()) {
            folder.mkdir()
        }
    }

    /**
     * 获取文件MIMETYPE
     * @param file 文件
     */
    internal fun getMIMEType(file: File): String {
        var type = "*/*"
        val name = file.name
        var end=""
        //文件扩展名
        if(name.lastIndexOf(".")>0){
            end = name.substring(name.lastIndexOf("."), name.length).toLowerCase()
        }
        if (end == "") {
            //如果无法直接打开，跳出列表由用户选择
            return type
        }
        type= MIME_MapTable[end].toString()
        return type
    }

    //(后缀名 to MIME类型)
    private val MIME_MapTable = mapOf(".3gp" to "video/3gpp",".apk" to "application/vnd.android.package-archive",".asf" to "video/x-ms-asf", ".avi" to "video/x-msvideo", ".bin" to "application/octet-stream",".bmp" to "image/bmp",".doc" to "application/msword",".docx" to "application/vnd.openxmlformats-officedocument.wordprocessingml.document",".xls" to "application/vnd.ms-excel",".xlsx" to "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" ,".gif" to "image/gif",".gtar" to "application/x-gtar",".gz" to "application/x-gzip",".htm" to "text/html",".html" to "text/html",".jpeg" to "image/jpeg",".jpg" to "image/jpeg",".js" to "application/x-javascript" ,".log" to "text/plain",".m3u" to "audio/x-mpegurl",".m4a" to "audio/mp4a-latm",".m4b" to "audio/mp4a-latm",".m4p" to "audio/mp4a-latm",".m4u" to "video/vnd.mpegurl",".m4v" to "video/x-m4v",".mov" to "video/quicktime",".mp2" to "audio/x-mpeg",".mp3" to "audio/x-mpeg",".mp4" to "video/mp4",".mpc" to "application/vnd.mpohun.certificate",".mpe" to "video/mpeg",".mpeg" to "video/mpeg" ,".mpg" to "video/mpeg",".mpg4" to "video/mp4",".mpga" to "audio/mpeg",".msg" to "application/vnd.ms-outlook",".ogg" to "audio/ogg",".pdf" to "application/pdf",".png" to "image/png",".pps" to "application/vnd.ms-powerpoint",".ppt" to "application/vnd.ms-powerpoint",".pptx" to "application/vnd.openxmlformats-officedocument.presentationml.presentation",".prop" to "text/plain",".rc" to "text/plain",".rmvb" to "audio/x-pn-realaudio",".rtf" to "application/rtf" ,".sh" to "text/plain",".tar" to "application/x-tar",".tgz" to "application/x-compressed",".txt" to "text/plain",".wav" to "audio/x-wav",".wma" to "audio/x-ms-wma",".wmv" to "audio/x-ms-wmv",".wps" to "application/vnd.ms-works",".xml" to "text/plain",".z" to "application/x-compress",".zip" to "application/x-zip-compressed","" to "*/*")

    /**
     * 文件复制
     * @param fromPath 待复制文件路径
     * @param toPath 目标路径
     */
    internal fun copy(fromPath: String, toPath: String) {
        val sfile = File(fromPath)
        if (sfile.isDirectory) {
            copyFolder(fromPath, toPath)
        } else {
            copyFile(fromPath, toPath)
        }
    }

    /**
     * 复制单个文件
     * @param fromPath String 原文件路径
     * @param toPath String 复制后路径
     */
    private fun copyFile(fromPath: String, toPath: String) {
        try {
            var bytesum = 0
            var byteread: Int
            val oldfile = File(fromPath)
            if (!oldfile.exists()) {
                return
            }
            if (!oldfile.isFile) {
                return
            }
            if (!oldfile.canRead()) {
                return
            }
            if (oldfile.exists()) { //文件存在时
                val inStream = FileInputStream(fromPath) //读入原文件
                val fs = FileOutputStream(toPath)
                val buffer = ByteArray(1024)
                byteread=inStream.read(buffer)
                while (byteread  != -1) {
                    bytesum += byteread //字节数 文件大小
                    println(bytesum)
                    fs.write(buffer, 0, byteread)
                    byteread=inStream.read(buffer)
                }
                inStream.close()
            }
        } catch (e: Exception) {
            println("复制单个文件操作出错")
            e.printStackTrace()
        }
    }

    /**
     * 复制整个文件夹内容
     * @param fromPath String 原文件路径
     * @param toPath String 复制后路径
     */
    private fun copyFolder(fromPath: String, toPath: String) {
        try {
            File(toPath).mkdirs() //如果文件夹不存在 则建立新文件夹
            val a = File(fromPath)
            val file = a.list()
            var temp: File
            for (f in file) {
                temp = when (fromPath.endsWith(File.separator)) {
                    true ->  File(fromPath + f)
                    false ->  File(fromPath + File.separator + f)
                }
                if (temp.isFile) {
                    val input = FileInputStream(temp)
                    val output = FileOutputStream(toPath + "/" +
                            temp.name)
                    val b = ByteArray(1024 * 5)
                    var len=input.read(b)
                    while (len  != -1) {
                        output.write(b, 0, len)
                        len=input.read(b)
                    }
                    output.flush()
                    output.close()
                    input.close()
                }
                if (temp.isDirectory) {//如果是子文件夹
                    copyFolder(fromPath + "/" + f, toPath + "/" + f)
                }
            }

        } catch (e: Exception) {
            println("复制整个文件夹内容操作出错")
            e.printStackTrace()
        }
    }

}

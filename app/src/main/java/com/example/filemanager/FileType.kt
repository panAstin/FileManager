package com.example.filemanager

/**
 * Created by 11046 on 2017/9/23.
 * 文件类型
 */
enum class FileType {
    text,
    download,
    music,
    photo,
    video,
    zip,
    apk;

    companion object {
        fun getFileTypeByOrdinal(ordinal: Int): FileType {
            return values().firstOrNull { it.ordinal == ordinal }
                    ?: text
        }
    }
}
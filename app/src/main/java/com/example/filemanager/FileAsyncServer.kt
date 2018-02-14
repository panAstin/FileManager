package com.example.filemanager

import java.io.IOException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.nio.charset.Charset


/**
 * Created by 11046 on 2018/2/14.
 * 文件同步服务端
 */
class FileAsyncServer(){
    @Throws(IOException::class)
    fun init() {
        val charset = Charset.forName("UTF-8")
        // 创建一个选择器，可用close()关闭，isOpen()表示是否处于打开状态，他不隶属于当前线程
        val selector = Selector.open()
        // 创建ServerSocketChannel，并把它绑定到指定端口上
        val server = ServerSocketChannel.open()
        server.socket().bind(InetSocketAddress(7777), 1024)
        // 设置为非阻塞模式, 这个非常重要
        server.configureBlocking(false)
        // 在选择器里面注册关注这个服务器套接字通道的accept事件
        // ServerSocketChannel只有OP_ACCEPT可用，OP_CONNECT,OP_READ,OP_WRITE用于SocketChannel
        server.register(selector, SelectionKey.OP_ACCEPT)


        while (true) {
            selector.select(1000)
            val keys = selector.selectedKeys()
            val it = keys.iterator()
            var key: SelectionKey? = null
            while (it.hasNext()) {
                //如果key对应的Channel包含客户端的链接请求
                // OP_ACCEPT 这个只有ServerSocketChannel才有可能触发
                key = it.next()
                // 由于select操作只管对selectedKeys进行添加，所以key处理后我们需要从里面把key去掉
                it.remove()
                if (key!!.isAcceptable) {
                    val ssc = key.channel() as ServerSocketChannel
                    // 得到与客户端的套接字通道
                    val channel = ssc.accept()
                    channel.configureBlocking(false)
                    channel.register(selector, SelectionKey.OP_READ)
                    //将key对应Channel设置为准备接受其他请求
                    key.interestOps(SelectionKey.OP_ACCEPT)
                }
                if (key.isReadable) {
                    val channel = key.channel() as SocketChannel
                    val byteBuffer = ByteBuffer.allocate(1024)
                    var content = ""
                    try {
                        val readBytes = channel.read(byteBuffer)
                        if (readBytes > 0) {
                            byteBuffer.flip() //为write()准备
                            val bytes = ByteArray(byteBuffer.remaining())
                            byteBuffer.get(bytes)
                            content += String(bytes)
                            println(content)
                            //回应客户端
                            doWrite(channel)
                        }
                        // 写完就把状态关注去掉，否则会一直触发写事件(改变自身关注事件)
                        key.interestOps(SelectionKey.OP_READ)
                    } catch (i: IOException) {
                        //如果捕获到该SelectionKey对应的Channel时出现了异常,即表明该Channel对于的Client出现了问题
                        //所以从Selector中取消该SelectionKey的注册
                        key.cancel()
                        if (key.channel() != null) {
                            key.channel().close()
                        }
                    }

                }
            }
        }
    }

    @Throws(IOException::class)
    private fun doWrite(sc: SocketChannel) {
        val req = "服务器已接受".toByteArray()
        val byteBuffer = ByteBuffer.allocate(req.size)
        byteBuffer.put(req)
        byteBuffer.flip()
        sc.write(byteBuffer)
        if (!byteBuffer.hasRemaining()) {
            println("Send 2 Service successed")
        }
    }
}
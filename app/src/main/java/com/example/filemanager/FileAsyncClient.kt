package com.example.filemanager

import android.util.Log
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel
import java.nio.charset.Charset
import java.util.concurrent.ArrayBlockingQueue

/**
 * Created by 11046 on 2018/2/14.
 * 文件同步客户端
 */
class FileAsyncClient(private var address: InetAddress){
    // 创建一个套接字通道，注意这里必须使用无参形式
    private var selector: Selector? = null
    //使用Map保存每个连接，当OP_READ就绪时，根据key找到对应的文件对其进行写入。若将其封装成一个类，作为值保存，可以再上传过程中显示进度等等
    var fileMap: Map<SelectionKey, FileChannel> = HashMap()
    var charset = Charset.forName("UTF-8")
    @Volatile
    private var stop = false
    var arrayQueue = ArrayBlockingQueue<String>(8)
    @Throws(IOException::class)
    fun init() {
        selector = Selector.open()
        val channel = SocketChannel.open()
        // 设置为非阻塞模式，这个方法必须在实际连接之前调用(所以open的时候不能提供服务器地址，否则会自动连接)
        channel.configureBlocking(false)
        if (channel.connect(InetSocketAddress(address, 7777))) {
            channel.register(selector, SelectionKey.OP_READ)
            //发送消息
            doWrite(channel, "66666666")
        } else {
            channel.register(selector, SelectionKey.OP_CONNECT)
        }
        Log.e("wifip2p","client")

        //启动一个接受服务器反馈的线程
        //  new Thread(new ReceiverInfo()).start();

        while (!stop) {
            selector!!.select(1000)
            val keys = selector!!.selectedKeys()
            val it = keys.iterator()
            var key: SelectionKey?
            while (it.hasNext()) {
                key = it.next()
                it.remove()
                val sc = key!!.channel() as SocketChannel
                // OP_CONNECT 两种情况，链接成功或失败这个方法都会返回true
                if (key.isConnectable) {
                    // 由于非阻塞模式，connect只管发起连接请求，finishConnect()方法会阻塞到链接结束并返回是否成功
                    // 另外还有一个isConnectionPending()返回的是是否处于正在连接状态(还在三次握手中)
                    if (channel.finishConnect()) {
                        /* System.out.println("准备发送数据");
                        // 链接成功了可以做一些自己的处理
                        channel.write(charset.encode("I am Coming"));
                        // 处理完后必须吧OP_CONNECT关注去掉，改为关注OP_READ
                        key.interestOps(SelectionKey.OP_READ);*/
                        sc.register(selector, SelectionKey.OP_READ)
                        //    new Thread(new DoWrite(channel)).start();
                        doWrite(channel, "66666666")
                    } else {
                        //链接失败，进程推出
                        System.exit(1)
                    }
                }
                if (key.isReadable) {
                    //读取服务端的响应
                    val buffer = ByteBuffer.allocate(1024)
                    val readBytes = sc.read(buffer)
                    var content = ""
                    if (readBytes > 0) {
                        buffer.flip()
                        val bytes = ByteArray(buffer.remaining())
                        buffer.get(bytes)
                        content += String(bytes)
                        stop = true
                    } else if (readBytes < 0) {
                        //对端链路关闭
                        key.channel()
                        sc.close()
                    }
                    println(content)
                    key.interestOps(SelectionKey.OP_READ)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun doWrite(sc: SocketChannel, data: String) {
        val req = data.toByteArray()
        val byteBuffer = ByteBuffer.allocate(req.size)
        byteBuffer.put(req)
        byteBuffer.flip()
        sc.write(byteBuffer)
        if (!byteBuffer.hasRemaining()) {
            println("Send successed")
        }
    }
}
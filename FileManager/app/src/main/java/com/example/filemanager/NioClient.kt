package com.example.filemanager

import android.util.Log
import com.example.filemanager.activities.MainActivity
import com.example.filemanager.utils.FileUtil
import com.example.filemanager.utils.HttpClientUtil
import com.example.filemanager.utils.ServerUtil
import org.json.JSONObject
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel
import java.nio.charset.Charset

/**
 * Created by 11046 on 2018/2/14.
 * NIOSocket客户端
 */
class NioClient(private var address: InetAddress){
    // 创建一个套接字通道，注意这里必须使用无参形式
    private var selector: Selector? = null
    var charset = Charset.forName("UTF-8")
    @Volatile
    private var stop = false
    @Throws(IOException::class)
    fun init() {
        selector = Selector.open()
        val channel = SocketChannel.open()
        // 设置为非阻塞模式，这个方法必须在实际连接之前调用(所以open的时候不能提供服务器地址，否则会自动连接)
        channel.configureBlocking(false)
        if (channel.connect(InetSocketAddress(address, 8081))) {
            channel.register(selector, SelectionKey.OP_READ)
            //发送消息
            //doWrite(channel, "66666666")
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
                        if(MainActivity.SERVER_STATU){
                            doWrite(channel, ServerUtil.ip + MainActivity.CONFIG["port"])
                        }else{
                            doWrite(channel,"getaddress")
                        }
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
                    if (content == "ok"){
                        channel.close()
                    }else if(ServerUtil.isIP(content)){
                        doWrite(channel,"ok")
                        channel.close()
                        FileUtil.doFileSync(content)
                    }
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
package org.itstack.naive.chat.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.nio.charset.Charset;

/**
 * 虫洞栈：https://bugstack.cn
 * 公众号：bugstack虫洞栈  ｛获取学习源码｝
 * Create by 小傅哥 on 2020
 */
public class MyChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel channel) {

        // 基于换行符号，遇到一个换行符，此换行符之前的数据被看作是一个数据包
        channel.pipeline().addLast(new LineBasedFrameDecoder(1024));

        // 解码转String，注意调整自己的编码格式GBK、UTF-8，因为在TCP协议中传递的二进制数据，我们这里是通过Netty自带的解码码包，将传递的二进制数据转换为String
        channel.pipeline().addLast(new StringDecoder(Charset.forName("GBK")));

        // 解码转String，注意调整自己的编码格式GBK、UTF-8，因为在TCP协议中传递的是二进制数据，当我们想给客户端发送信息的时候，需要使用Netty自带的编码包，将String转换为
        // 二进制数据
        channel.pipeline().addLast(new StringEncoder(Charset.forName("GBK")));


        // 在管道中添加我们自己的接收数据实现方法，这里面添加的是我们自己的对消息的处理
        channel.pipeline().addLast(new MyServerHandler());
    }

}

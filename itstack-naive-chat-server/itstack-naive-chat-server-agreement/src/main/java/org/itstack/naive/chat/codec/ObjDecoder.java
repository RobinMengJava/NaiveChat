package org.itstack.naive.chat.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.itstack.naive.chat.protocol.Packet;
import org.itstack.naive.chat.util.SerializationUtil;

import java.util.List;

/**
 * 这是我们自定义的解码器
 * 如果自定义解码器，需要继承netty提供的解码器抽象类ByteToMessageDecoder，并实现里面的decode方法
 *
 * 这里我们实现了自己的解码器，并且自己解决了粘包、半包问题
 *
 */
public class ObjDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (in.readableBytes() < 4) {
            return;
        }

        // 标记包头开始的位置
        in.markReaderIndex();
        // 读取一个int的长度，也就是要读取存放消息长度的字段的值
        int dataLength = in.readInt();
        // 如果剩下的可读的字节长度小于消息长度，则重置读指针至原来读取的开始位置，等待消息到达
        if (in.readableBytes() < dataLength) {
            in.resetReaderIndex();
            return;
        }

        // 如果剩下的可读的字节长度大于等于消息长度，则进行读取指令
        byte command = in.readByte();  //读取指令、

        // 读取完消息长度和指令后，剩下的就是消息体内容，创建一个存放下消息体长度的字节数据
        byte[] data = new byte[dataLength - 1]; //指令占了一位，剔除掉

        // 将消息体存放进字节数组中
        in.readBytes(data);

        // 将其反序列化为对象
        out.add(SerializationUtil.deserialize(data, Packet.get(command)));
    }

}

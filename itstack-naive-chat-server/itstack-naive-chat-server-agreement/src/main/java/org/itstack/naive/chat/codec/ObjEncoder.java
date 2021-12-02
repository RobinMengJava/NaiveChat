package org.itstack.naive.chat.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.itstack.naive.chat.protocol.Packet;
import org.itstack.naive.chat.util.SerializationUtil;

/**
 * 自定义解码器
 * 自定义解码器需要继承MessageToByteEncoder抽象类，并实现里面的encode方法
 *
 */
public class ObjEncoder extends MessageToByteEncoder<Packet> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet in, ByteBuf out) {
        // 将传入的对象序列化成字节数组
        byte[] data = SerializationUtil.serialize(in);
        // 写入消息长度
        out.writeInt(data.length + 1);
        // 添加指令
        out.writeByte(in.getCommand()); //添加指令
        // 写入字节数据信息
        out.writeBytes(data);
    }

}

package org.itstack.naive.chat.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.itstack.naive.chat.util.SerializationUtil;

import java.util.List;

/**
 * 虫洞栈：https://bugstack.cn
 * 公众号：bugstack虫洞栈  ｛关注获取学习源码｝
 * Create by 小傅哥 on 2020
 */
public class ObjDecoder extends ByteToMessageDecoder {

    private Class<?> genericClass;

    public ObjDecoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    /**
     * 这里面处理的情况包含整包、半包、粘包数据
     *
     * 如果读取到整包数据，则直接将数据读入到字节数据当中
     *
     * 如果读取到半包数据，则return掉，等待继续发送来的数据
     *
     * 如果读取到粘包数据，则读取前面数据长度字段所给的数据，剩下的数据不读取
     * */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        // 如果ByteBuf的可读字节的长度小于4，则return
        if (in.readableBytes() < 4) {
            return;
        }

        // 如果ByteBuf的可读字节的长度大于4，将readIndex即可读指针存到markReaderIndex属性中
        in.markReaderIndex();

        // 读取4个字节，根据我们定义的通信协议，前4个字节是数据包的长度
        int dataLength = in.readInt();

        // 这个时候可读字节如果不足数据长度的话，那也就意味着这个这个数据包是不全的，所以，要重置readerIndex指针为原来的位置，读取的内容要放弃掉
        if (in.readableBytes() < dataLength) {
            in.resetReaderIndex();
            return;
        }

        // 如果读取完数据长度大于4的话，说明可以读取到一个完整的数据包，这个时候我们将ByteBuf中的数据读取到data字节数组中
        byte[] data = new byte[dataLength];
        in.readBytes(data);

        // 然后将字节数据传入到序列化工具当中去，将字节数据序列化成对象
        out.add(SerializationUtil.deserialize(data, genericClass));
    }

}

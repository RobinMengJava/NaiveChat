package org.itstack.naive.chat.protocol;

/**
 * 虫洞栈：https://bugstack.cn
 * 公众号：bugstack虫洞栈  ｛关注获取学习源码｝
 *
 * 标识指令码
 *
 * 因为我们每个接口传输的协议都是不一样的，也就是说，一个接口一个协议
 *
 * 那么，我们如果根据接口来找到对应的协议进行解析呢，所以，我们在数据包中增加了一个字节，来标识是哪个接口的协议
 * 这个字节我们定义它为指令，这个类，就是一个指令的常量类
 *
 * Q:为什么不用一个常量类来定义，而是使用一个接口来定义呢
 *
 * A:
 *
 *
 */
public interface Command {

    Byte LoginRequest = 1;
    Byte LoginResponse = 2;

    Byte MsgRequest = 3;
    Byte MsgResponse = 4;

    Byte TalkNoticeRequest = 5;
    Byte TalkNoticeResponse = 6;

    Byte SearchFriendRequest = 7;
    Byte SearchFriendResponse = 8;

    Byte AddFriendRequest = 9;
    Byte AddFriendResponse = 10;

    Byte DelTalkRequest = 11;

    Byte MsgGroupRequest = 12;
    Byte MsgGroupResponse = 13;

    Byte ReconnectRequest = 14;

}

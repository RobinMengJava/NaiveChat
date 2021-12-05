package org.itstack.naive.chat.socket.handler;

import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
import org.itstack.naive.chat.application.UserService;
import org.itstack.naive.chat.infrastructure.common.SocketChannelUtil;
import org.itstack.naive.chat.protocol.login.ReconnectRequest;
import org.itstack.naive.chat.protocol.msg.MsgRequest;
import org.itstack.naive.chat.socket.MyBizHandler;

import java.util.List;

/**
 * 博  客：http://bugstack.cn
 * 公众号：bugstack虫洞栈 | 沉淀、分享、成长，让自己和他人都能有所收获！
 * create by 小傅哥 on @2020
 * <p>
 * 重连处理
 */
public class ReconnectHandler extends MyBizHandler<ReconnectRequest> {

    public ReconnectHandler(UserService userService) {
        super(userService);
    }

    @Override
    public void channelRead(Channel channel, ReconnectRequest msg) {
        logger.info("===========>>>>> 客户端断线重连处理入参=[{}]", JSON.toJSONString(msg));

        // 添加用户channel
        SocketChannelUtil.removeUserChannelByUserId(msg.getUserId());
        SocketChannelUtil.addChannel(msg.getUserId(), channel);

        // 添加群组channel
        List<String> groupIdList = userService.queryUserGroupsIdList(msg.getUserId());
        for (String groupId : groupIdList) {
            SocketChannelUtil.addChannelGroup(groupId, channel);
        }


//        logger.info("客户端断线重连处理。userId：{}", msg.getUserId());
//        // 添加用户Channel
//        SocketChannelUtil.removeUserChannelByUserId(msg.getUserId());
//        SocketChannelUtil.addChannel(msg.getUserId(), channel);
//        // 添加群组Channel，首先找到当前用户的所有群组id，然后将自己新的channel加入到群组的channel当中
//        List<String> groupsIdList = userService.queryTalkBoxGroupsIdList(msg.getUserId());
//        for (String groupsId : groupsIdList) {
//            SocketChannelUtil.addChannelGroup(groupsId, channel);
//        }
    }

}

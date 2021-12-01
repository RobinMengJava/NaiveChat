package org.itstack.naive.chat.socket.handler;

import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
import org.itstack.naive.chat.application.UserService;
import org.itstack.naive.chat.domain.user.model.UserInfo;
import org.itstack.naive.chat.infrastructure.common.SocketChannelUtil;
import org.itstack.naive.chat.infrastructure.po.UserFriend;
import org.itstack.naive.chat.protocol.friend.AddFriendRequest;
import org.itstack.naive.chat.protocol.friend.AddFriendResponse;
import org.itstack.naive.chat.socket.MyBizHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * 博  客：http://bugstack.cn
 * 公众号：bugstack虫洞栈 | 沉淀、分享、成长，让自己和他人都能有所收获！
 * create by 小傅哥 on @2020
 */
public class AddFriendHandler extends MyBizHandler<AddFriendRequest> {

    public AddFriendHandler(UserService userService) {
        super(userService);
    }

    @Override
    public void channelRead(Channel channel, AddFriendRequest msg) {
        logger.info("=====================>>>>>> 添加好友的请求=[{}]", JSON.toJSONString(msg));

        // 添加好友到数据库中，将好友添加到自己的好友表中，将自己添加到好友的好友列表中
        List<UserFriend> userFriendList = new ArrayList<>();
        userFriendList.add(new UserFriend(msg.getUserId(), msg.getFriendId()));
        userFriendList.add(new UserFriend(msg.getFriendId(), msg.getUserId()));
        userService.addUserFriend(userFriendList);

        // 推送好友添加完成消息 自己
        UserInfo userInfo = userService.queryUserInfo(msg.getFriendId());
        channel.writeAndFlush(new AddFriendResponse(userInfo.getUserId(), userInfo.getUserNickName(), userInfo.getUserHead()));

        // 推送好友添加完成消息 好友
        Channel friendChannel = SocketChannelUtil.getChannel(msg.getFriendId());
        if (friendChannel == null) return;
        UserInfo friendInfo = userService.queryUserInfo(msg.getUserId());
        friendChannel.writeAndFlush(new AddFriendResponse(friendInfo.getUserId(), friendInfo.getUserNickName(), friendInfo.getUserHead()));

//        // 1. 添加好友到数据库中[A->B B->A]
//        List<UserFriend> userFriendList = new ArrayList<>();
//        userFriendList.add(new UserFriend(msg.getUserId(), msg.getFriendId()));
//        userFriendList.add(new UserFriend(msg.getFriendId(), msg.getUserId()));
//        userService.addUserFriend(userFriendList);
//        // 2. 推送好友添加完成 A
//        UserInfo userInfo = userService.queryUserInfo(msg.getFriendId());
//        channel.writeAndFlush(new AddFriendResponse(userInfo.getUserId(), userInfo.getUserNickName(), userInfo.getUserHead()));
//        // 3. 推送好友添加完成 B
//        Channel friendChannel = SocketChannelUtil.getChannel(msg.getFriendId());
//        if (null == friendChannel) return;
//        UserInfo friendInfo = userService.queryUserInfo(msg.getUserId());
//        friendChannel.writeAndFlush(new AddFriendResponse(friendInfo.getUserId(), friendInfo.getUserNickName(), friendInfo.getUserHead()));
    }

}

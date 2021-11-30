package org.itstack.naive.chat.socket.handler;

import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
import org.itstack.naive.chat.application.UserService;
import org.itstack.naive.chat.domain.user.model.*;
import org.itstack.naive.chat.infrastructure.common.Constants;
import org.itstack.naive.chat.infrastructure.common.SocketChannelUtil;
import org.itstack.naive.chat.protocol.login.LoginRequest;
import org.itstack.naive.chat.protocol.login.LoginResponse;
import org.itstack.naive.chat.protocol.login.dto.ChatRecordDto;
import org.itstack.naive.chat.protocol.login.dto.ChatTalkDto;
import org.itstack.naive.chat.protocol.login.dto.GroupsDto;
import org.itstack.naive.chat.protocol.login.dto.UserFriendDto;
import org.itstack.naive.chat.socket.MyBizHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * 博  客：http://bugstack.cn
 * 公众号：bugstack虫洞栈 | 沉淀、分享、成长，让自己和他人都能有所收获！
 * create by 小傅哥 on @2020
 * <p>
 * 登陆请求处理
 */
public class LoginHandler extends MyBizHandler<LoginRequest> {

    public LoginHandler(UserService userService) {
        super(userService);
    }

    @Override
    public void channelRead(Channel channel, LoginRequest msg) {
        logger.info("登录请求处理,请求参数=[{}]", JSON.toJSONString(msg));

        // 判断用户名、密码是否正确
        boolean auth = userService.checkAuth(msg.getUserId(), msg.getUserPassword());
        if (!auth) {
            logger.info("用户登录校验失败...");
            // 发送错误消息
            channel.writeAndFlush(new LoginResponse(false));
            return;
        }

        // 登录成功绑定channel
        SocketChannelUtil.addChannel(msg.getUserId(), channel);

        // 组装消息包
        LoginResponse loginResponse = new LoginResponse();

        loginResponse.setSuccess(true);

        //  用户信息
        UserInfo userInfo = userService.queryUserInfo(msg.getUserId());
        loginResponse.setUserId(userInfo.getUserId());
        loginResponse.setUserNickName(userInfo.getUserNickName());
        loginResponse.setUserHead(userInfo.getUserHead());

        // 用户对话框
        List<TalkBoxInfo> talkBoxInfoList = userService.queryTalkBoxInfoList(msg.getUserId());
        if (null != talkBoxInfoList && talkBoxInfoList.size() > 0) {
            for (TalkBoxInfo talkBoxInfo : talkBoxInfoList) {
                ChatTalkDto chatTalkDto = new ChatTalkDto();
                chatTalkDto.setTalkId(talkBoxInfo.getTalkId());
                chatTalkDto.setTalkName(talkBoxInfo.getTalkName());
                chatTalkDto.setTalkType(talkBoxInfo.getTalkType());
                chatTalkDto.setTalkHead(talkBoxInfo.getTalkHead());
                chatTalkDto.setTalkSketch(talkBoxInfo.getTalkSketch());
                chatTalkDto.setTalkDate(talkBoxInfo.getTalkDate());
                loginResponse.getChatTalkList().add(chatTalkDto);

                // 聊天消息

                // 好友的
                if (Constants.TalkType.Friend.getCode().equals(talkBoxInfo.getTalkType())) {
                    // 声明一个用来存储聊天记录
                    List<ChatRecordDto> chatRecordDtoList = new ArrayList<ChatRecordDto>();

                    // 1. 查询好友类型的聊天记录
                    List<ChatRecordInfo> chatRecordFriendsList = userService.queryChatRecordInfoList(talkBoxInfo.getTalkId(), msg.getUserId(), talkBoxInfo.getTalkType());

                    // 2. 将聊天记录封装成DTO
                    if (null != chatRecordFriendsList && !chatRecordFriendsList.isEmpty()) {
                        for (ChatRecordInfo chatRecordInfo : chatRecordFriendsList) {
                            ChatRecordDto chatRecordDto = new ChatRecordDto();
                            chatRecordDto.setTalkId(talkBoxInfo.getTalkId());
                            boolean msgType = msg.getUserId().equals(chatRecordInfo.getUserId());
                            // 自己发的消息
                            if (msgType) {
                                // 填充自己的userId
                                chatRecordDto.setUserId(msg.getUserId());
                                // 填充消息类型为自己发送的
                                chatRecordDto.setMsgType(0);
                            }
                            // 别人发送的消息
                            else {
                                // 填充别人的userId
                                chatRecordDto.setUserId(chatRecordInfo.getFriendId());
                                // 填充消息类型为好友发送的
                                chatRecordDto.setMsgType(1);
                            }

                            chatRecordDto.setMsgContent(chatRecordInfo.getMsgContent());
                            chatRecordDto.setMsgType(chatRecordInfo.getMsgType());
                            chatRecordDto.setMsgDate(chatRecordInfo.getMsgDate());
                            chatRecordDtoList.add(chatRecordDto);
                        }
                    }
                    // 3. 加入聊天记录列表List
                    chatTalkDto.setChatRecordList(chatRecordDtoList);
                }

                else if (Constants.TalkType.Group.getCode().equals(talkBoxInfo.getTalkType())) {
                    // 声明一个变量，用于存储聊天记录
                    List<ChatRecordDto> chatRecordDtoList = new ArrayList<>();
                    // 1. 查询群组类型的聊天记录
                    List<ChatRecordInfo> chatRecordGroupList = userService.queryChatRecordInfoList(talkBoxInfo.getTalkId(), msg.getUserId(), talkBoxInfo.getTalkType());

                    // 2. 将群组的聊天记录封装成DTO
                    if (null != chatRecordGroupList && !chatRecordGroupList.isEmpty()) {
                        for (ChatRecordInfo chatRecordInfo : chatRecordGroupList) {
                            UserInfo memberInfo = userService.queryUserInfo(chatRecordInfo.getUserId());
                            ChatRecordDto chatRecordDto = new ChatRecordDto();
                            chatRecordDto.setTalkId(talkBoxInfo.getTalkId());
                            chatRecordDto.setUserId(memberInfo.getUserId());
                            chatRecordDto.setUserNickName(memberInfo.getUserNickName());
                            chatRecordDto.setUserHead(memberInfo.getUserHead());
                            chatRecordDto.setMsgContent(chatRecordInfo.getMsgContent());
                            chatRecordDto.setMsgDate(chatRecordInfo.getMsgDate());
                            boolean msgType = msg.getUserId().equals(chatRecordInfo.getUserId());
                            chatRecordDto.setMsgType(msgType ? 0 : 1);
                            chatRecordDto.setMsgType(chatRecordInfo.getMsgType());
                            chatRecordDtoList.add(chatRecordDto);

                        }

                        chatTalkDto.setChatRecordList(chatRecordDtoList);
                    }

                }
            }
        }

        // 群组
        List<GroupsInfo> groupsInfoList = userService.queryUserGroupInfoList(msg.getUserId());
        if (null != groupsInfoList && !groupsInfoList.isEmpty()) {
            for (GroupsInfo groupsInfo : groupsInfoList) {
                GroupsDto groupsDto = new GroupsDto();
                groupsDto.setGroupId(groupsInfo.getGroupId());
                groupsDto.setGroupName(groupsInfo.getGroupName());
                groupsDto.setGroupHead(groupsInfo.getGroupHead());
                loginResponse.getGroupsList().add(groupsDto);
            }
        }

        // 好友
        List<UserFriendInfo> userFriendInfoList = userService.queryUserFriendInfoList(msg.getUserId());
        if (null != userFriendInfoList && !userFriendInfoList.isEmpty()) {
            for (UserFriendInfo userFriendInfo : userFriendInfoList) {
                UserFriendDto userFriendDto = new UserFriendDto();
                userFriendDto.setFriendId(userFriendInfo.getFriendId());
                userFriendDto.setFriendName(userFriendInfo.getFriendName());
                userFriendDto.setFriendHead(userFriendInfo.getFriendHead());
                loginResponse.getUserFriendList().add(userFriendDto);
            }
        }

        channel.writeAndFlush(loginResponse);
    }

}

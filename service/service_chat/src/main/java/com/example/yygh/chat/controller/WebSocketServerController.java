package com.example.yygh.chat.controller;


import com.alibaba.fastjson.JSONObject;
import com.example.yygh.chat.service.WebSocketService;
import com.example.yygh.common.helper.JwtHelper;
import com.example.yygh.model.chat.MsgEntity;
import com.example.yygh.model.user.UserInfo;
import com.example.yygh.vo.chat.MsgVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@ServerEndpoint(value = "/api/chat/{sid}/{token}")
public class WebSocketServerController {

    private static ApplicationContext applicationContext;

    public static void setApplicationContext(ApplicationContext applicationContext) {
        WebSocketServerController.applicationContext = applicationContext;
    }

    /**
     * 房间号 -> 组成员信息
     */
    private static ConcurrentHashMap<String, List<Session>> groupMemberInfoMap = new ConcurrentHashMap<>();
    /**
     * 房间号 -> 在线人数
     */
    private static ConcurrentHashMap<String, Set<String>> onlineUserMap = new ConcurrentHashMap<>();

    /**
     * 收到消息调用的方法，群成员发送消息
     *
     * @param sid:房间号
     * @param token：用户token
     * @param message：发送消息
     */
    @OnMessage
    public void onMessage(@PathParam("sid") String sid, @PathParam("token") String token, String message) {
        // json字符串转对象
        MsgVO msg = JSONObject.parseObject(message, MsgVO.class);

        //新建一个聊天记录
        MsgEntity msgEntity = new MsgEntity();
        Long userId;
        if (token.equals("admin")) {
            userId = 0L;
        } else {
            userId = JwtHelper.getUserId(token);
        }
        msgEntity.setUserName(msg.getUsername());
        msgEntity.setMsg(msg.getMsg());
        msgEntity.setUserId(userId);
        WebSocketService webSocketService = applicationContext.getBean(WebSocketService.class);
        webSocketService.saveMsg(msgEntity);
        List<Session> sessionList = groupMemberInfoMap.get(sid);
        Set<String> onlineUserList = onlineUserMap.get(sid);
        // 先一个群组内的成员发送消息
        sessionList.forEach(item -> {
            try {
                msg.setCount(onlineUserList.size());
                // json对象转字符串
                String text = JSONObject.toJSONString(msg);
                item.getBasicRemote().sendText(text);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 建立连接调用的方法，群成员加入
     *
     * @param session
     * @param sid
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid, @PathParam("token") String token) {
        List<Session> sessionList = groupMemberInfoMap.computeIfAbsent(sid, k -> new ArrayList<>());
        Set<String> onlineUserList = onlineUserMap.computeIfAbsent(sid, k -> new HashSet<>());
        onlineUserList.add(token);
        sessionList.add(session);
        // 发送上线通知
        sendInfo(sid, token, onlineUserList.size(), "上线了~");
    }


    public void sendInfo(String sid, String token, Integer onlineSum, String info) {
        log.info(token);
        if (Objects.equals(token, "admin")){
            MsgVO msg = new MsgVO();
            msg.setToken(token);
            msg.setUsername("客服");
            msg.setCount(onlineSum);
            msg.setMsg("客服" + info);
            // json对象转字符串
            String text = JSONObject.toJSONString(msg);
            onMessage(sid, token, text);
            return;
        }
        Long userId = JwtHelper.getUserId(token);
        // 获取该连接用户信息
        WebSocketService webSocketService = applicationContext.getBean(WebSocketService.class);
        UserInfo userInfo = webSocketService.getUserInfo(userId);
        // 发送通知
        MsgVO msg = new MsgVO();
        msg.setToken(token);
        msg.setUsername(userInfo.getNickName());
        msg.setCount(onlineSum);
        msg.setMsg(userInfo.getNickName() + info);
        // json对象转字符串
        String text = JSONObject.toJSONString(msg);
        onMessage(sid, token, text);
    }

    /**
     * 关闭连接调用的方法，群成员退出
     *
     * @param session
     * @param sid
     */
    @OnClose
    public void onClose(Session session, @PathParam("sid") String sid, @PathParam("token") String token) {
        List<Session> sessionList = groupMemberInfoMap.get(sid);
        sessionList.remove(session);
        Set<String> onlineUserList = onlineUserMap.get(sid);
        onlineUserList.remove(token);
        // 发送离线通知
        sendInfo(sid, token, onlineUserList.size(), "下线了~");
    }

    /**
     * 传输消息错误调用的方法
     *
     * @param error
     */
    @OnError
    public void OnError(Throwable error) {
        log.info("Connection error");
    }
}
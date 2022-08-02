package com.example.yygh.chat.service;

import com.example.yygh.model.chat.MsgEntity;
import com.example.yygh.model.user.UserInfo;
import org.springframework.data.domain.Page;


public interface WebSocketService {

    UserInfo getUserInfo(Long id);

    void saveMsg(MsgEntity msgEntity);

    Page<MsgEntity> selectPage(Integer page, Integer limit, String userId);
}

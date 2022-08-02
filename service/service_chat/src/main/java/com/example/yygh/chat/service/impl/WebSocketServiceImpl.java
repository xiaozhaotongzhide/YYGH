package com.example.yygh.chat.service.impl;

import com.example.yygh.chat.repository.MsgRepository;
import com.example.yygh.chat.service.WebSocketService;
import com.example.yygh.model.chat.MsgEntity;
import com.example.yygh.model.user.UserInfo;
import com.example.yygh.user.client.PatientFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class WebSocketServiceImpl implements WebSocketService {

    @Autowired
    private PatientFeignClient patientFeignClient;

    @Autowired
    private MsgRepository msgRepository;

    @Override
    public UserInfo getUserInfo(Long id) {
        return patientFeignClient.getUserInfo(id);
    }

    @Override
    public void saveMsg(MsgEntity msgEntity) {
        msgEntity.setCreateTime(new Date());
        msgRepository.save(msgEntity);
    }

    @Override
    public Page<MsgEntity> selectPage(Integer page, Integer limit, String userName) {
        //创建Pageable对象
        Pageable pageable = PageRequest.of(page - 1, limit);
        //创建条件匹配器
        MsgEntity msgEntity = new MsgEntity();
        msgEntity.setUserName(userName);
        Example<MsgEntity> example = Example.of(msgEntity);
        return msgRepository.findAll(example, pageable);
    }
}

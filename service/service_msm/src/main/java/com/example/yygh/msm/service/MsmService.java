package com.example.yygh.msm.service;

import com.example.yygh.vo.msm.MsmVo;

public interface MsmService {
    void send(String phone, String code);

    //mq使用发送邮件接口
    void send(MsmVo msmVo);
}

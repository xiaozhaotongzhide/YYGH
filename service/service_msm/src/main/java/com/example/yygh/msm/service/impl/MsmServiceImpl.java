package com.example.yygh.msm.service.impl;

import com.example.yygh.msm.service.MsmService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Slf4j
public class MsmServiceImpl implements MsmService {

    @Autowired
    public JavaMailSenderImpl javaMailSenderImpl;

    //邮箱发送
    @Override
    @Async
    public void send(String phone, String code) {
        log.info(phone);
        //判断邮箱是否为空
        if (StringUtils.isEmpty(phone)) {
            return;
        }
        //1.创建一个简单的的消息邮件
        System.out.println(code);
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setSubject("正在登录YYGH系统");
        simpleMailMessage.setText("验证码:"+code);
        simpleMailMessage.setTo(phone);
        simpleMailMessage.setFrom("2590416618@qq.com");
        javaMailSenderImpl.send(simpleMailMessage);
    }
}

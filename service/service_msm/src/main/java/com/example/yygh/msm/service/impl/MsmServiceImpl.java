package com.example.yygh.msm.service.impl;

import com.example.yygh.msm.service.MsmService;
import com.example.yygh.vo.msm.MsmVo;
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
        log.info(code);
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setSubject("正在登录YYGH系统");
        simpleMailMessage.setText("验证码:" + code);
        simpleMailMessage.setTo(phone);
        simpleMailMessage.setFrom("2590416618@qq.com");
        javaMailSenderImpl.send(simpleMailMessage);
    }

    //关于mq发送短信的封装
    @Override
    @Async
    public void send(MsmVo msmVo) {
        log.info(msmVo.getPhone());
        //判断邮箱是否为空
        if (StringUtils.isEmpty(msmVo.getPhone())) {
            return;
        }
        //1.创建一个简单的的消息邮件
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setSubject(msmVo.getParam().get("title") + "预约成功");
        simpleMailMessage.setText(msmVo.getParam().get("name") + "的预约成功请于" + msmVo.getParam().get("reserveDate") + "到医院就诊" );
        simpleMailMessage.setTo(msmVo.getPhone());
        simpleMailMessage.setFrom("2590416618@qq.com");
        javaMailSenderImpl.send(simpleMailMessage);
    }
}

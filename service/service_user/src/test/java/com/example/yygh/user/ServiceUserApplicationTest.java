package com.example.yygh.user;


import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

@SpringBootTest
@Slf4j
public class ServiceUserApplicationTest {

    @Autowired
    public JavaMailSenderImpl javaMailSenderImpl;

    @Test
    public void contextLoads() {
        //1.创建一个简单的的消息邮件
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setSubject("通知");
        simpleMailMessage.setText("这是通过spring-boot进行邮件通知测试");
        simpleMailMessage.setTo("2590416618@qq.com");
        simpleMailMessage.setFrom("2590416618@qq.com");
        javaMailSenderImpl.send(simpleMailMessage);
    }

    @Test
    public void coding() throws UnsupportedEncodingException {
        String s = new String("æ²\u0089è¿·å\u00AD¦ä¹ !!!".getBytes("windows-1256"), StandardCharsets.UTF_8);
        log.info(s);
    }
}

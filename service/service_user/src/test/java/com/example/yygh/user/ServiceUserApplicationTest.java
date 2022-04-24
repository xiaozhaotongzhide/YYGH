package com.example.yygh.user;


import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@SpringBootTest
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
}

package com.example.yygh.chat;

import com.example.yygh.chat.controller.WebSocketServerController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@EnableDiscoveryClient//注册服务
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@ComponentScan(basePackages = "com.example") //swagger扫描文件
@EnableFeignClients(basePackages = "com.example")
public class ServiceChatApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(ServiceChatApplication.class, args);
        WebSocketServerController.setApplicationContext(applicationContext);
    }

}
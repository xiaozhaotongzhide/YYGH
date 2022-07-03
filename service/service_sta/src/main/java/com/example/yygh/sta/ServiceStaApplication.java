package com.example.yygh.sta;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@EnableDiscoveryClient//注册服务
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@ComponentScan(basePackages = "com.example") //swagger扫描文件
@EnableFeignClients(basePackages = "com.example")
public class ServiceStaApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceStaApplication.class,args);
    }
}

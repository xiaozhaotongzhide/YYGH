package com.example.yygh.user.client;

import com.example.yygh.model.user.Patient;
import com.example.yygh.model.user.UserInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "service-user")
@Repository
public interface PatientFeignClient {

    //根据就诊人id获取就诊人信息
    @GetMapping("/api/user/patient/inner/get/{id}")
    Patient getPatientOrder(@PathVariable("id") Long id);

    @GetMapping("/api/user/getUserInfo/{id}")
    UserInfo getUserInfo(@PathVariable("id") Long id);

}

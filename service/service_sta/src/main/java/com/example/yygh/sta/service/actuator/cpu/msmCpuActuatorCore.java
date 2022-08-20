package com.example.yygh.sta.service.actuator.cpu;

import com.alibaba.fastjson.JSON;
import com.example.yygh.model.sta.CpuEntity;
import com.example.yygh.msm.feign.MsmFeignClient;
import com.example.yygh.sta.service.actuator.cpuActuatorCore;
import com.example.yygh.vo.sta.ActuatorVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;

@Service
@Slf4j
public class msmCpuActuatorCore implements cpuActuatorCore {

    @Autowired
    private MsmFeignClient msmFeignClient;

    @Override
    public CpuEntity saveCpu() {
        String cpu = msmFeignClient.getCpu();
        log.info(cpu);
        ActuatorVo jsonObject = JSON.parseObject(cpu, ActuatorVo.class);
        BigDecimal bigDecimal = new BigDecimal(jsonObject.getMeasurements().get(0).getValue());
        Double cpuValue = bigDecimal.doubleValue() * 100;
        log.info(String.valueOf(cpuValue));
        CpuEntity cpuEntity = new CpuEntity();
        cpuEntity.setCpuSize(cpuValue);
        cpuEntity.setTime(new Date());
        cpuEntity.setServiceName("service_msm");
        return cpuEntity;
    }
}

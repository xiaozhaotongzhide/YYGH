package com.example.yygh.sta.service.actuator.ram;

import com.alibaba.fastjson.JSON;
import com.example.yygh.model.sta.CpuEntity;
import com.example.yygh.model.sta.RamEntity;
import com.example.yygh.sta.service.actuator.cpuActuatorCore;
import com.example.yygh.sta.service.actuator.ramActuatorCore;
import com.example.yygh.task.feign.taskFeignClient;
import com.example.yygh.vo.sta.ActuatorVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;

@Service
@Slf4j
public class taskRamActuatorCore implements ramActuatorCore {
    @Autowired
    private taskFeignClient taskFeignClient;

    @Override
    public RamEntity saveRam() {
        String ram = taskFeignClient.getRam();
        log.info(ram);
        ActuatorVo jsonObject = JSON.parseObject(ram, ActuatorVo.class);
        BigDecimal bigDecimal = new BigDecimal(jsonObject.getMeasurements().get(0).getValue());
        long ramValue = bigDecimal.longValue() / 1024 / 1024;
        log.info(String.valueOf(ramValue));
        RamEntity ramEntity = new RamEntity();
        ramEntity.setTime(new Date());
        ramEntity.setRamSize((int) ramValue);
        ramEntity.setServiceName("service_task");
        return ramEntity;
    }
}

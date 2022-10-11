package com.example.yygh.sta.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.yygh.model.sta.CpuEntity;
import com.example.yygh.rabbit.constant.MqConst;
import com.example.yygh.rabbit.service.RabbitService;
import com.example.yygh.sta.mapper.cpuMapper;
import com.example.yygh.sta.service.actuator.cpuActuatorCore;
import com.example.yygh.sta.service.cpuService;
import com.example.yygh.vo.msm.MsmVo;
import com.example.yygh.vo.sta.CpuVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class cpuServiceImpl extends ServiceImpl<cpuMapper, CpuEntity> implements cpuService {


    @Autowired
    private List<cpuActuatorCore> cpuActuatorCores;

    @Autowired
    private RabbitService rabbitService;

    @Override
    public void save() {
        cpuActuatorCores.forEach(i -> {
            CpuEntity cpuEntity = i.saveCpu();
            if (cpuEntity.getCpuSize() > 95) {
                MsmVo msmVo = new MsmVo();
                HashMap<String, Object> map = new HashMap<>();
                map.put("title", cpuEntity.getServiceName() + "cpu");
                map.put("service", cpuEntity.getServiceName() + "cpu");
                map.put("static", "actuator");
                msmVo.setParam(map);
                msmVo.setPhone("2590416618@qq.com");
                rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_MSM, MqConst.ROUTING_MSM_ITEM, msmVo);
            }
            baseMapper.insert(cpuEntity);
        });
    }

    @Cacheable(value = "cpu", keyGenerator = "keyGenerator")
    @Override
    public Map<String, Object> show() {
        List<CpuVo> cpuVos = baseMapper.selectActuator();
        Map<String, Object> map = new HashMap<>();
        List<String> servers = cpuVos.stream().map(CpuVo::getServiceName).collect(Collectors.toList());
        List<Double> dataList = cpuVos.stream().map(CpuVo::getCpuSize).collect(Collectors.toList());
        map.put("servers", servers);
        map.put("dataList", dataList);
        return map;
    }
}

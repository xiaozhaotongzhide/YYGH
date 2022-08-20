package com.example.yygh.sta.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.yygh.model.sta.RamEntity;
import com.example.yygh.rabbit.constant.MqConst;
import com.example.yygh.rabbit.service.RabbitService;
import com.example.yygh.sta.mapper.ramMapper;
import com.example.yygh.sta.service.actuator.ramActuatorCore;
import com.example.yygh.sta.service.ramService;
import com.example.yygh.vo.msm.MsmVo;
import com.example.yygh.vo.sta.CpuVo;
import com.example.yygh.vo.sta.RamVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ramServiceImpl extends ServiceImpl<ramMapper, RamEntity> implements ramService {

    @Autowired
    private List<ramActuatorCore> ramActuatorCores;

    @Autowired
    private RabbitService rabbitService;

    @Override
    public void save() {
        ramActuatorCores.forEach(i -> {
            RamEntity ramEntity = i.saveRam();
            if (ramEntity.getRamSize() > 1000) {
                MsmVo msmVo = new MsmVo();
                HashMap<String, Object> map = new HashMap<>();
                map.put("static", "actuator");
                map.put("title", ramEntity.getServiceName() + "ram");
                map.put("service", ramEntity.getServiceName() + "ram");
                msmVo.setParam(map);
                msmVo.setPhone("2590416618@qq.com");
                rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_MSM, MqConst.ROUTING_MSM_ITEM, msmVo);
            }
            baseMapper.insert(ramEntity);
        });
    }

    @Cacheable(value = "ram", keyGenerator = "keyGenerator")
    @Override
    public Map<String, Object> show() {
        List<RamVo> cpuVos = baseMapper.selectActuator();
        Map<String, Object> map = new HashMap<>();
        List<String> servers = cpuVos.stream().map(RamVo::getServiceName).collect(Collectors.toList());
        List<Integer> dataList = cpuVos.stream().map(RamVo::getRamSize).collect(Collectors.toList());
        map.put("servers", servers);
        map.put("dataList", dataList);
        return map;
    }
}

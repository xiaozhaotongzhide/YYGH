package com.example.yygh.sta.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.yygh.model.sta.CpuEntity;
import com.example.yygh.vo.sta.CpuVo;

import java.util.List;
import java.util.Map;

public interface cpuService extends IService<CpuEntity> {

    public void save();

    public Map<String, Object> show();
}

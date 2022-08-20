package com.example.yygh.sta.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.yygh.model.sta.RamEntity;

import java.util.Map;

public interface ramService extends IService<RamEntity> {
    public void save();

    public Map<String, Object> show();
}

package com.example.yygh.sta.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.yygh.model.sta.CpuEntity;
import com.example.yygh.vo.sta.CpuVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface cpuMapper extends BaseMapper<CpuEntity> {

    List<CpuVo> selectActuator();
}

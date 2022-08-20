package com.example.yygh.sta.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.yygh.model.sta.RamEntity;
import com.example.yygh.vo.sta.CpuVo;
import com.example.yygh.vo.sta.RamVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ramMapper extends BaseMapper<RamEntity> {
    List<RamVo> selectActuator();
}

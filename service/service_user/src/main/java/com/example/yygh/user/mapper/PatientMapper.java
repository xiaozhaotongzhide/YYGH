package com.example.yygh.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.yygh.model.user.Patient;
import com.example.yygh.model.user.UserInfo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface PatientMapper extends BaseMapper<Patient> {

}

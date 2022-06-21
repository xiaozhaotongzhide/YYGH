package com.example.yygh.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.yygh.model.user.Patient;

import java.util.List;

public interface PatientService extends IService<Patient> {
    //根据userid获取病人列表
    List<Patient> findAllByUserId(Long userId);

    Patient getPatientId(Long id);
}

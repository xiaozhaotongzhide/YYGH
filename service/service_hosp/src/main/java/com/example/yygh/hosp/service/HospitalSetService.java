package com.example.yygh.hosp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.yygh.hosp.repository.HospitalRepository;
import com.example.yygh.model.hosp.HospitalSet;
import org.springframework.beans.factory.annotation.Autowired;

public interface HospitalSetService extends IService<HospitalSet> {
    String getSingKey(String hoscode);

}

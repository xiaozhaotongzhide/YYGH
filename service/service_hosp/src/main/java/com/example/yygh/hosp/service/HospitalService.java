package com.example.yygh.hosp.service;

import com.example.yygh.model.hosp.Hospital;
import com.example.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface HospitalService {
    void save(Map<String, Object> stringObjectMap);

    Hospital getByHoscode(String hoscode);

    Page<Hospital> selectHospPage(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo);

    void updateHospStatus(String id, Integer status);

    Map<String, Object> selectHospDetail(String id);

    String getHospName(String hoscode);

    List<Hospital> findByHosname(String hosName);

    Map<String, Object> item(String hoscode);
}

package com.example.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.yygh.cmn.clent.DictFeignClient;
import com.example.yygh.hosp.repository.HospitalRepository;
import com.example.yygh.hosp.service.HospitalService;
import com.example.yygh.model.hosp.Hospital;
import com.example.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HospitalServiceImpl implements HospitalService {

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private DictFeignClient dictFeignClient;

    //保存医院接口
    @Override
    public void save(Map<String, Object> paramMap) {
        //把参数map集合转换对象 Hospital
        String mapString = JSONObject.toJSONString(paramMap);
        Hospital hospital = JSONObject.parseObject(mapString, Hospital.class);

        //判断是否存在数据
        String hoscode = hospital.getHoscode();
        Hospital hospitalExist = hospitalRepository.getHospitalByHoscode(hoscode);

        //如果存在，进行修改
        if (hospitalExist != null) {
            hospital.setStatus(hospitalExist.getStatus());
            hospital.setCreateTime(hospitalExist.getCreateTime());
            hospital.setUpdateTime(new Date());
            hospital.setIsDeleted(0);
            hospitalRepository.save(hospital);
        } else {//如果不存在，进行添加
            hospital.setStatus(0);
            hospital.setCreateTime(new Date());
            hospital.setUpdateTime(new Date());
            hospital.setIsDeleted(0);
            hospitalRepository.save(hospital);
        }
    }

    @Override
    public Hospital getByHoscode(String hoscode) {
        Hospital byHoscode = hospitalRepository.getHospitalByHoscode(hoscode);
        return byHoscode;
    }

    @Override
    public Page<Hospital> selectHospPage(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo) {
        //创建Pageable对象
        Pageable pageable = PageRequest.of(page - 1, limit);
        //创建条件匹配器
        //之所以能实现模糊查询是因为在这创建了条件匹配器
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true);
        //hospitalSetQueryVO转化为Hospital对象
        Hospital hospital = new Hospital();
        BeanUtils.copyProperties(hospitalQueryVo, hospital);
        //创建对象
        Example<Hospital> example = Example.of(hospital, matcher);
        Page<Hospital> all = hospitalRepository.findAll(example, pageable);
        //调用dictFeignClient
        //获取查询List集合,遍历进行医院等级封装
        all.getContent().stream().forEach(item -> {
            this.setHospitalHosType(item);
        });
        return all;
    }

    //更新医院状态
    @Override
    public void updateHospStatus(String id, Integer status) {
        //根据id查询医院信息
        Hospital hospital = hospitalRepository.findById(id).get();
        //修改值
        hospital.setStatus(status);
        hospital.setUpdateTime(new Date());
        hospitalRepository.save(hospital);
    }

    @Override
    public Map<String, Object> selectHospDetail(String id) {
        Hospital hospital = hospitalRepository.findById(id).get();
        Hospital hospital1 = this.setHospitalHosType(hospital);
        Map<String, Object> map = new HashMap<>();
        //医院基本信息
        map.put("hospital",hospital);
        //医院的预约信息
        map.put("bookingRule",hospital1.getBookingRule());
        return map;
    }

    @Override
    public String getHospName(String hoscode) {
        return hospitalRepository.getHospitalByHoscode(hoscode).getHosname();
    }

    @Override
    public List<Hospital> findByHosname(String hosName) {
        List<Hospital> hospitalByHosnameLike = hospitalRepository.findHospitalByHosnameLike(hosName);
        return hospitalByHosnameLike;
    }

    @Override
    public Map<String, Object> item(String hoscode) {
        HashMap<String, Object> map = new HashMap<>();
        //获取医院详情
        Hospital hospital = this.setHospitalHosType(this.getByHoscode(hoscode));
        map.put("hospital",hospital);
        //预约规则
        map.put("bookingRule",hospital.getBookingRule());
        hospital.setBookingRule(null);
        return map;
    }

    //获取查询的list集合,封装进行医院等级
    private Hospital setHospitalHosType(Hospital item) {
        //根据dictCode和value获取医院等级值
        String hostypeString = dictFeignClient.getName("Hostype", item.getHostype());
        //查询省 市 地区
        String province = dictFeignClient.getName(item.getProvinceCode());
        String client = dictFeignClient.getName(item.getCityCode());
        String district = dictFeignClient.getName(item.getDistrictCode());
        item.getParam().put("fullAddress", province + client + district);
        item.getParam().put("hostypeString", hostypeString);
        return item;
    }
}

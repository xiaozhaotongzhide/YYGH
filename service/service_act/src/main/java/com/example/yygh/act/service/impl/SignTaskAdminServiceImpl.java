package com.example.yygh.act.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.yygh.act.mapper.SignTaskAdminMapper;
import com.example.yygh.act.service.CouponAdminService;
import com.example.yygh.act.service.SignTaskAdminService;
import com.example.yygh.model.act.CouponAdmin;
import com.example.yygh.model.act.SignTaskAdmin;
import com.example.yygh.vo.act.SignTaskAdminVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.Date;

/**
 * @author 86157
 * @description 针对表【sign_task】的数据库操作Service实现
 */
@Service
public class SignTaskAdminServiceImpl extends ServiceImpl<SignTaskAdminMapper, SignTaskAdmin> implements SignTaskAdminService {

    @Autowired
    private CouponAdminService couponAdminService;

    //创建任务
    @Override
    public Boolean createTask(SignTaskAdminVo signTaskAdminVo) {
        Long couponAdminId = couponAdminService.createTask(signTaskAdminVo);
        SignTaskAdmin signTaskAdmin = new SignTaskAdmin();
        signTaskAdmin.setCouponId(couponAdminId);
        signTaskAdmin.setIsDeleted(0);
        signTaskAdmin.setCreateTime(new Date());
        signTaskAdmin.setUpdateTime(new Date());
        BeanUtils.copyProperties(signTaskAdminVo, signTaskAdmin);
        int insert = baseMapper.insert(signTaskAdmin);
        return insert > 0;
    }

    @Override
    @Transactional
    public Boolean updateTask(SignTaskAdminVo signTaskAdminVo) {
        SignTaskAdmin signTaskAdmin = baseMapper.selectById(signTaskAdminVo.getId());
        Long couponId = signTaskAdmin.getCouponId();
        CouponAdmin byId = couponAdminService.getById(couponId);
        if (!ObjectUtils.isEmpty(signTaskAdminVo.getInventory())) {
            byId.setInventory(signTaskAdminVo.getInventory());
        }
        if (!ObjectUtils.isEmpty(signTaskAdminVo.getStartTime())) {
            byId.setStartTime(signTaskAdminVo.getStartTime());
        }
        if (!ObjectUtils.isEmpty(signTaskAdminVo.getEndTime())) {
            byId.setEndTime(signTaskAdminVo.getEndTime());
        }
        if (!ObjectUtils.isEmpty(signTaskAdminVo.getDetails())) {
            byId.setDetails(signTaskAdminVo.getDetails());
        }
        couponAdminService.updateById(byId);
        if (!ObjectUtils.isEmpty(signTaskAdminVo.getInventory())) {
            signTaskAdmin.setInventory(signTaskAdminVo.getInventory());
        }
        if (!ObjectUtils.isEmpty(signTaskAdminVo.getStartTime())) {
            signTaskAdmin.setStartTime(signTaskAdminVo.getStartTime());
        }
        if (!ObjectUtils.isEmpty(signTaskAdminVo.getEndTime())) {
            signTaskAdmin.setEndTime(signTaskAdminVo.getEndTime());
        }
        if (!ObjectUtils.isEmpty(signTaskAdminVo.getDetails())) {
            signTaskAdmin.setDetails(signTaskAdminVo.getDetails());
        }
        int i = baseMapper.updateById(signTaskAdmin);
        if (i > 0) {
            return true;
        } else {
            return false;
        }
    }
}

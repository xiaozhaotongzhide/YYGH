package com.example.yygh.act.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.yygh.act.mapper.CouponAdminMapper;
import com.example.yygh.act.service.CouponAdminService;
import com.example.yygh.model.act.CouponAdmin;
import com.example.yygh.vo.act.CouponAdminVo;
import com.example.yygh.vo.act.SignTaskAdminVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;


/**
 * @author 86157
 * @description 针对表【coupon_admin】的数据库操作Service实现
 */
@Service
public class CouponAdminServiceImpl extends ServiceImpl<CouponAdminMapper, CouponAdmin> implements CouponAdminService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String SECKILL_STOCK_KEY ="seckill:stock:";

    @Override
    public Boolean update(CouponAdminVo couponAdminVo) {
        CouponAdmin couponAdmin = new CouponAdmin();
        BeanUtils.copyProperties(couponAdminVo, couponAdmin);
        int i = baseMapper.updateById(couponAdmin);
        if (i > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    @Transactional
    public Boolean saveCouponAdmin(CouponAdminVo couponAdminVo) {
        CouponAdmin couponAdmin = new CouponAdmin();
        BeanUtils.copyProperties(couponAdminVo, couponAdmin);
        couponAdmin.setCreateTime(new Date());
        couponAdmin.setUpdateTime(new Date());
        couponAdmin.setIsDeleted(0);
        int insert = baseMapper.insert(couponAdmin);
        stringRedisTemplate.opsForValue().set(SECKILL_STOCK_KEY + couponAdmin.getId(), couponAdmin.getInventory().toString());
        return insert > 0;
    }

    @Override
    public Long createTask(SignTaskAdminVo signTaskAdminVo) {
        CouponAdmin couponAdmin = new CouponAdmin();
        BeanUtils.copyProperties(signTaskAdminVo, couponAdmin);
        couponAdmin.setCreateTime(new Date());
        couponAdmin.setUpdateTime(new Date());
        couponAdmin.setIsDeleted(0);
        baseMapper.insert(couponAdmin);
        return couponAdmin.getId();
    }

}

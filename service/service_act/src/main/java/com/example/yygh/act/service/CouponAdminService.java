package com.example.yygh.act.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.yygh.model.act.CouponAdmin;
import com.example.yygh.vo.act.CouponAdminVo;
import com.example.yygh.vo.act.SignTaskAdminVo;

/**
* @author 86157
* @description 针对表【coupon_admin】的数据库操作Service
* @createDate 2022-10-01 12:48:25
*/
public interface CouponAdminService extends IService<CouponAdmin> {

    Boolean update(CouponAdminVo couponAdminVo);

    Boolean saveCouponAdmin(CouponAdminVo couponAdminVo);

    Long createTask(SignTaskAdminVo signTaskAdminVo);
}

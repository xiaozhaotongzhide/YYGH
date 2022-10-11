package com.example.yygh.act.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.example.yygh.common.result.Result;
import com.example.yygh.model.act.CouponUser;
import com.example.yygh.model.act.SignTask;
import com.example.yygh.vo.act.CouponUserVo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author 86157
 * @description 针对表【coupon_user】的数据库操作Service
 * @createDate 2022-10-01 12:48:39
 */
public interface CouponUserService extends IService<CouponUser> {

    Result seckillVoucher(CouponUserVo couponUserVo, HttpServletRequest request);

    Boolean useCoupon(HttpServletRequest request, CouponUserVo couponUserVo);

    List<CouponUser> getListByUser(HttpServletRequest request);

    //签到领取购物券接口
    Boolean saveSign(SignTask signTask);
}

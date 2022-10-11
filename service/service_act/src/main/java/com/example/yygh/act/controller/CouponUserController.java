package com.example.yygh.act.controller;


import com.example.yygh.act.service.CouponUserService;
import com.example.yygh.common.result.Result;
import com.example.yygh.model.act.CouponUser;
import com.example.yygh.vo.act.CouponUserVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("api/act")
public class CouponUserController {

    @Autowired
    private CouponUserService couponUserService;

    @PostMapping("/seckill/{id}")
    public Result seckillVoucher(@PathVariable("id") Long id, HttpServletRequest request) {
        CouponUserVo couponUserVo = new CouponUserVo();
        couponUserVo.setCouponId(id);
        return couponUserService.seckillVoucher(couponUserVo, request);
    }

    @GetMapping("/useCoupon/{id}")
    public Result useCoupon(HttpServletRequest request, @PathVariable("id") Long id) {
        CouponUserVo couponUserVo = new CouponUserVo();
        couponUserVo.setId(id);
        Boolean aBoolean = couponUserService.useCoupon(request, couponUserVo);
        if (aBoolean) {
            return Result.ok();
        } else {
            return Result.fail();
        }
    }

    //根据用户id展示
    @GetMapping("/getList")
    public Result getListByUser(HttpServletRequest request) {
        List<CouponUser> couponUserList = couponUserService.getListByUser(request);
        if (couponUserList.size() > 0) {
            return Result.ok(couponUserList);
        } else {
            return Result.fail("无可用优惠券");
        }
    }

    @GetMapping("/getListUser")
    public Result getListUser(){
        return Result.ok(couponUserService.list());
    }
}

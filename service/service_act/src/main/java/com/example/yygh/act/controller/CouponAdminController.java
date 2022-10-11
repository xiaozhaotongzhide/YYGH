package com.example.yygh.act.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.yygh.act.service.CouponAdminService;
import com.example.yygh.common.result.Result;
import com.example.yygh.model.act.CouponAdmin;
import com.example.yygh.vo.act.CouponAdminVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("admin/act")
public class CouponAdminController {

    @Autowired
    private CouponAdminService couponAdminService;

    @PostMapping("/saveCouponAdmin")
    public Result saveCouponAdmin(@RequestBody CouponAdminVo couponAdminVo) {
        return Result.ok(couponAdminService.saveCouponAdmin(couponAdminVo));
    }

    @DeleteMapping("/{couponAdminId}")
    public Result delete(@PathVariable Long couponAdminId) {
        QueryWrapper<CouponAdmin> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", couponAdminId);
        return Result.ok(couponAdminService.remove(queryWrapper));
    }

    @PostMapping("/updateCouponAdmin")
    public Result updateCouponAdmin(@RequestBody CouponAdminVo couponAdminVo) {
        return Result.ok(couponAdminService.update(couponAdminVo));
    }

    @GetMapping("/list")
    public Result list() {
        List<CouponAdmin> list = couponAdminService.list();
        HashMap<Object, Object> map = new HashMap<>();
        map.put("records",list);
        map.put("total",list.size());
        return Result.ok(map);
    }

    @GetMapping("/getCoupon/{id}")
    public Result getCoupon(@PathVariable Long id) {
        CouponAdmin coupon = couponAdminService.getById(id);
        return Result.ok(coupon);
    }
}

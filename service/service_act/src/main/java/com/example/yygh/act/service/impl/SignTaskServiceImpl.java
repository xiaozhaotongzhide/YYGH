package com.example.yygh.act.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.yygh.act.mapper.SignTaskMapper;
import com.example.yygh.act.service.CouponAdminService;
import com.example.yygh.act.service.CouponUserService;
import com.example.yygh.act.service.SignTaskAdminService;
import com.example.yygh.act.service.SignTaskService;
import com.example.yygh.common.helper.JwtHelper;
import com.example.yygh.common.result.Result;
import com.example.yygh.model.act.CouponAdmin;
import com.example.yygh.model.act.CouponUser;
import com.example.yygh.model.act.SignTask;
import com.example.yygh.model.act.SignTaskAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 86157
 * @description 针对表【sign_task】的数据库操作Service实现
 */
@Service
public class SignTaskServiceImpl extends ServiceImpl<SignTaskMapper, SignTask> implements SignTaskService {

    @Autowired
    private SignTaskAdminService signTaskAdminService;

    @Autowired
    private CouponAdminService couponAdminService;

    @Autowired
    private CouponUserService couponUserService;

    @Override
    public Result signTask(HttpServletRequest request) {
        String token = request.getHeader("token");
        Long userId = JwtHelper.getUserId(token);
        QueryWrapper<SignTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        SignTask signTask = baseMapper.selectOne(queryWrapper);
        //第一次签到
        if (ObjectUtils.isEmpty(signTask)) {
            return first(userId);
        }
        //非第一次
        else {
            //0.检查之前有没有领取过
            if (signTask.getReceiveNumber() != 0) {
                return Result.fail("已经领取过了");
            }

            //1.判断最后一次签到是不是昨天
            Date lastTime = signTask.getLastTime();
            //每月更新
            LocalDateTime lastLocalTime = lastTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            int dayOfYearLast = lastLocalTime.getDayOfMonth();
            LocalDateTime now = LocalDateTime.now();
            int dayOfYearNow = now.getDayOfMonth();
            if (dayOfYearNow - dayOfYearLast == 1) {
                //是昨天
                int i = signTask.getNumber() + 1;
                if (i == 3) {
                    //发放购物券
                    signTask.setNumber(0);
                    signTask.setReceiveNumber(1);
                    signTask.setReceiveLastTime(new Date());
                    signTask.setLastTime(new Date());
                    signTask.setUpdateTime(new Date());
                    baseMapper.updateById(signTask);
                    //通知user发放购物券
                    couponUserService.saveSign(signTask);
                    return Result.ok("发放成功");
                } else {
                    //签到成功
                    signTask.setLastTime(new Date());
                    signTask.setReceiveNumber(signTask.getNumber() + 1);
                    signTask.setUpdateTime(new Date());
                    baseMapper.updateById(signTask);
                    return Result.ok("签到成功");
                }
            } else {
                //不是昨天
                signTask.setNumber(1);
                signTask.setUpdateTime(new Date());
                signTask.setLastTime(new Date());
                baseMapper.updateById(signTask);
                return Result.ok("签到成功");
            }
        }
    }

    //第一次签到
    private Result first(Long userId) {
        //1.查询所有合格的签到任务
        List<SignTaskAdmin> signTaskAdminList = signTaskAdminService.list();
        //2.获取最后一个结束时间最后的任务
        List<SignTaskAdmin> collect = signTaskAdminList.stream().filter(i -> i.getStartTime().before(new Date())).filter(i -> i.getEndTime().after(new Date()))
                .sorted((x, y) -> (int) (x.getEndTime().getTime() - y.getEndTime().getTime())).collect(Collectors.toList());
        SignTaskAdmin signTaskAdmin = collect.get(0);
        //3.更新订单,订单库存减一
        signTaskAdmin.setInventory(signTaskAdmin.getInventory() - 1);
        signTaskAdminService.updateById(signTaskAdmin);
        CouponAdmin couponAdmin = couponAdminService.getById(signTaskAdmin.getCouponId());
        couponAdmin.setInventory(couponAdmin.getInventory() - 1);
        QueryWrapper<CouponAdmin> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", couponAdmin.getId());
        couponAdminService.update(couponAdmin, queryWrapper);
        //4.创建签到任务
        SignTask signTask1 = new SignTask();
        signTask1.setCouponId(signTaskAdmin.getCouponId());
        signTask1.setUserId(userId);
        signTask1.setLastTime(new Date());
        signTask1.setNumber(1);
        signTask1.setIsDeleted(0);
        signTask1.setReceiveNumber(0);
        signTask1.setCreateTime(new Date());
        signTask1.setUpdateTime(new Date());
        baseMapper.insert(signTask1);
        return Result.ok("签到成功");
    }


}

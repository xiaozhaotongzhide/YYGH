package com.example.yygh.act.service.impl;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.yygh.act.component.RedisIdWorker;
import com.example.yygh.act.mapper.CouponUserMapper;
import com.example.yygh.act.service.CouponAdminService;
import com.example.yygh.act.service.CouponUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.example.yygh.common.helper.JwtHelper;
import com.example.yygh.common.result.Result;
import com.example.yygh.model.act.CouponAdmin;
import com.example.yygh.model.act.CouponUser;
import com.example.yygh.model.act.SignTask;
import com.example.yygh.vo.act.CouponUserVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

/**
 * @author 86157
 * @description 针对表【coupon_user】的数据库操作Service实现
 */
@Slf4j
@Service
public class CouponUserServiceImpl extends ServiceImpl<CouponUserMapper, CouponUser> implements CouponUserService {

    @Autowired
    private CouponAdminService couponAdminService;

    @Autowired
    private RedisIdWorker redisIdWorker;

    @Override
    @Transactional
    @SentinelResource(
            value = "message",
            blockHandler = "blockHandler",//指定发生BlockException时进入的方法
            fallback = "fallback"//指定发生Throwable时进入的方法
    )
    public Result seckillVoucher(CouponUserVo couponUserVo, HttpServletRequest request) {
        //1. 查询优惠卷
        CouponAdmin couponAdmin = couponAdminService.getById(couponUserVo.getCouponId());
        //2. 判断秒杀是否开始 开始时间大于当前时间表示未开始抢购

        if (couponAdmin.getStartTime().after(new Date())) {
            return Result.fail("秒杀尚未开始！");
        }
        //3. 判断秒杀是否结束
        if (couponAdmin.getEndTime().before(new Date())) {
            return Result.fail("秒杀已经结束！");
        }
        //4. 判断库存是否充足
        if (couponAdmin.getInventory() < 1) {
            return Result.fail("库存不足！");
        }
        String token = request.getHeader("token");
        Long userId = JwtHelper.getUserId(token);
        //5. 查询订单
        //5.1 查询订单
        int count = query().eq("user_id", userId).eq("coupon_id", couponUserVo.getCouponId()).count();
        //5.2 判断并返回
        if (count > 0) {
            return Result.fail("用户已经购买过！");
        }

        //6. 扣减库存
        /**
         * .eq("voucher_id", voucherId).update().gt("stock",0)
         */
        boolean success = couponAdminService.update().setSql("inventory = inventory -1").eq("id", couponUserVo.getCouponId()).gt("inventory", 0).update();
        if (!success) {
            return Result.fail("库存不足！");
        }

        //7. 创建订单
        CouponUser voucherOrder = new CouponUser();
        long orderId = redisIdWorker.nextId("order");
        log.info(String.valueOf(orderId));
        BeanUtils.copyProperties(couponAdmin, voucherOrder);
        voucherOrder.setId(orderId);
        voucherOrder.setUserId(userId);
        voucherOrder.setCouponId(couponAdmin.getId());
        voucherOrder.setState(0);
        save(voucherOrder);
        //8. 返回订单id
        return Result.ok(orderId);

    }

    //BlockException时进入的方法
    public Result blockHandler(BlockException ex) {
        log.error("{}", ex);
        return Result.fail("接口被限流或者降级了...");
    }

    //Throwable时进入的方法
    public Result fallback(Throwable throwable) {
        log.error("{}", throwable);
        return Result.fail("接口发生异常了...");
    }

    @Override
    public Boolean useCoupon(HttpServletRequest request, CouponUserVo couponUserVo) {
        String token = request.getHeader("token");
        Long userId = JwtHelper.getUserId(token);
        CouponUser couponUser = baseMapper.selectById(couponUserVo.getId());
        if (couponUser.getStartTime().before(new Date()) && couponUser.getEndTime().after(new Date())) {
            return this.update().setSql("state = 1").eq("state", 0).eq("user_id", userId).eq("id", couponUserVo.getId()).update();
        } else {
            return false;
        }
    }

    @Override
    public List<CouponUser> getListByUser(HttpServletRequest request) {
        String token = request.getHeader("token");
        Long userId = JwtHelper.getUserId(token);
        QueryWrapper<CouponUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public Boolean saveSign(SignTask signTask) {
        CouponUser couponUser = new CouponUser();
        CouponAdmin couponAdmin = couponAdminService.getById(signTask.getCouponId());
        couponUser.setCouponId(signTask.getCouponId());
        couponUser.setUserId(signTask.getUserId());
        couponUser.setStartTime(couponAdmin.getStartTime());
        couponUser.setEndTime(couponAdmin.getEndTime());
        couponUser.setState(0);
        couponUser.setCreateTime(new Date());
        couponUser.setUpdateTime(new Date());
        //也要使用全局id生成器
        couponUser.setId(redisIdWorker.nextId("order"));
        couponUser.setIsDeleted(0);
        return baseMapper.insert(couponUser) > 0;
    }

}

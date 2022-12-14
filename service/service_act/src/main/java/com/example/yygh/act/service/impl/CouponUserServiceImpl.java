package com.example.yygh.act.service.impl;

import cn.hutool.core.bean.BeanUtil;
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
import com.example.yygh.common.utils.AuthContextHolder;
import com.example.yygh.enums.OrderStatusEnum;
import com.example.yygh.model.act.CouponAdmin;
import com.example.yygh.model.act.CouponUser;
import com.example.yygh.model.act.SignTask;
import com.example.yygh.vo.act.CouponUserVo;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;

    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("luaFile/seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    //异步处理线程池
    private static final ExecutorService SECKILL_ORDER_EXECUTOR = Executors.newSingleThreadExecutor();
    //在类初始化之后执行，因为当这个类初始化好了之后，随时都是有可能要执行的

    @PostConstruct
    private void init() {
        SECKILL_ORDER_EXECUTOR.submit(new VoucherOrderHandler());
    }

    // 用于线程池处理的任务
    // 当初始化完毕后，就会去从对列中去拿信息
    private class VoucherOrderHandler implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    // 1.获取消息队列中的订单信息 XREADGROUP GROUP g1 c1 COUNT 1 BLOCK 2000 STREAMS s1 >
                    List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                            Consumer.from("g1", "c1"),
                            StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
                            StreamOffset.create("stream.orders", ReadOffset.lastConsumed())
                    );
                    // 2.判断订单信息是否为空
                    if (list == null || list.isEmpty()) {
                        // 如果为null，说明没有消息，继续下一次循环
                        continue;
                    }
                    // 解析数据
                    MapRecord<String, Object, Object> record = list.get(0);
                    Map<Object, Object> value = record.getValue();
                    CouponUser voucherOrder = BeanUtil.fillBeanWithMap(value, new CouponUser(), true);
                    // 3.创建订单
                    createVoucherOrder(voucherOrder);
                    // 4.确认消息 XACK
                    stringRedisTemplate.opsForStream().acknowledge("s1", "g1", record.getId());
                } catch (Exception e) {
                    log.error("处理订单异常", e);
                    handlePendingList();
                }
            }
        }

        private void handlePendingList() {
            while (true) {
                try {
                    // 1.获取pending-list中的订单信息 XREADGROUP GROUP g1 c1 COUNT 1 BLOCK 2000 STREAMS s1 0
                    List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                            Consumer.from("g1", "c1"),
                            StreamReadOptions.empty().count(1),
                            StreamOffset.create("stream.orders", ReadOffset.from("0"))
                    );
                    // 2.判断订单信息是否为空
                    if (list == null || list.isEmpty()) {
                        // 如果为null，说明没有异常消息，结束循环
                        break;
                    }
                    // 解析数据
                    MapRecord<String, Object, Object> record = list.get(0);
                    Map<Object, Object> value = record.getValue();
                    CouponUser voucherOrder = BeanUtil.fillBeanWithMap(value, new CouponUser(), true);
                    // 3.创建订单
                    createVoucherOrder(voucherOrder);
                    // 4.确认消息 XACK
                    stringRedisTemplate.opsForStream().acknowledge("s1", "g1", record.getId());
                } catch (Exception e) {
                    log.error("处理订单异常", e);
                }
            }
        }
    }

    @Transactional
    public void createVoucherOrder(CouponUser voucherOrder) {
        Long userId = voucherOrder.getUserId();
        Long couponId = voucherOrder.getCouponId();
        // 创建锁对象
        RLock redisLock = redissonClient.getLock("lock:order:" + userId);
        // 尝试获取锁
        boolean isLock = redisLock.tryLock();
        // 判断
        if (!isLock) {
            // 获取锁失败，直接返回失败或者重试
            log.error("不允许重复下单！");
            return;
        }

        try {
            // 5.1.查询订单
            int count = query().eq("user_id", userId).eq("coupon_id", couponId).count();
            // 5.2.判断是否存在
            if (count > 0) {
                // 用户已经购买过了
                log.error("不允许重复下单！");
                return;
            }

            // 6.扣减库存
            boolean success = couponAdminService.update()
                    .setSql("inventory = inventory - 1") // set inventory = inventory - 1
                    .eq("id", couponId).gt("inventory", 0) // where id = ? and inventory > 0
                    .update();
            if (!success) {
                // 扣减失败
                log.error("库存不足！");
                return;
            }
            QueryWrapper<CouponAdmin> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("id",couponId);
            CouponAdmin couponAdmin = couponAdminService.getOne(queryWrapper);
            voucherOrder.setEndTime(couponAdmin.getEndTime());
            voucherOrder.setStartTime(couponAdmin.getStartTime());
            voucherOrder.setState(OrderStatusEnum.UNPAID.getStatus());
            voucherOrder.setCreateTime(new Date());
            voucherOrder.setUpdateTime(new Date());
            voucherOrder.setIsDeleted(0);
            // 7.创建订单
            save(voucherOrder);
        } finally {
            // 释放锁
            redisLock.unlock();
        }
    }

    @Override
    @Transactional
    @SentinelResource(
            value = "message",
            blockHandler = "blockHandler",//指定发生BlockException时进入的方法
            fallback = "fallback"//指定发生Throwable时进入的方法
    )
    public Result seckillVoucher(CouponUserVo couponUserVo, HttpServletRequest request) {
        //获取用户
        Long userId = AuthContextHolder.getUserId(request);
        long orderId = redisIdWorker.nextId("order");
        // 1.执行lua脚本
        Long result = stringRedisTemplate.execute(
                SECKILL_SCRIPT,
                Collections.emptyList(),
                couponUserVo.getCouponId().toString(), userId.toString(), String.valueOf(orderId)
        );
        int r = result.intValue();
        // 2.判断结果是否为0
        if (r != 0) {
            // 2.1.不为0 ，代表没有购买资格
            return Result.fail(r == 1 ? "库存不足" : "不能重复下单");
        }
        // 3.返回订单id
        return Result.ok(orderId);
    }

/*    @Override
    @Transactional
    @SentinelResource(
            value = "message",
            blockHandler = "blockHandler",//指定发生BlockException时进入的方法
            fallback = "fallback"//指定发生Throwable时进入的方法
    )
    public Result seckillVoucher(CouponUserVo couponUserVo, HttpServletRequest request) {
        //这样做每次秒杀都会查询太慢了
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

        return createVoucherOrder(couponUserVo, couponAdmin, userId);


    }*/


/*    private Result<? extends Serializable> createVoucherOrder(CouponUserVo couponUserVo, CouponAdmin couponAdmin, Long userId) {
        //一人一单判断
        // 创建锁对象
        SimpleRedisLock redisLock = new SimpleRedisLock("order:" + userId, stringRedisTemplate);
        // 尝试获取锁
        boolean isLock = redisLock.tryLock(10);
        // 判断
        if(!isLock){
            // 获取锁失败，直接返回失败或者重试
            return Result.fail("不允许重复下单！");
        }

        try {
            //5. 查询订单
            //5.1 查询订单
            int count = query().eq("user_id", userId).eq("coupon_id", couponUserVo.getCouponId()).count();
            //5.2 判断并返回
            if (count > 0) {
                return Result.fail("用户已经购买过！");
            }

            //6. 扣减库存
            boolean success = couponAdminService.update().setSql("inventory = inventory -1").eq("id", couponUserVo.getCouponId()).gt("inventory", 0).update();
            if (!success) {
                return Result.fail("库存不足！");
            }

            //7.保存
            return save(couponAdmin, userId);
        } finally {
            // 释放锁
            redisLock.unlock();
        }
    }*/



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

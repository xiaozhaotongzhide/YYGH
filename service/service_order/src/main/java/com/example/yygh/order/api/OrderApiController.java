package com.example.yygh.order.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.yygh.common.result.Result;
import com.example.yygh.common.utils.AuthContextHolder;
import com.example.yygh.enums.OrderStatusEnum;
import com.example.yygh.model.order.OrderInfo;
import com.example.yygh.model.user.UserInfo;
import com.example.yygh.order.service.OrderService;
import com.example.yygh.vo.order.OrderQueryVo;
import com.example.yygh.vo.user.UserInfoQueryVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/order/orderInfo")
public class OrderApiController {

    @Autowired
    private OrderService orderService;

    //生成挂号订单
    @PostMapping("auth/submitOrder/{scheduleId}/{patientId}")
    public Result saveOrder(@PathVariable String scheduleId,
                            @PathVariable Long patientId) {
        Long orderId = orderService.saveOrder(scheduleId,patientId);
        return Result.ok(orderId);
    }

    //根据订单id查询订单详情
    @GetMapping("auth/getOrders/{orderId}")
    public Result getOrders(@PathVariable Long orderId) {
        OrderInfo orderInfo = orderService.getOrders(orderId);
        return Result.ok(orderInfo);
    }

    //订单列表(条件查询带分页)
    @GetMapping("auth/{page}/{limit}")
    public Result list(@PathVariable Long page,
                       @PathVariable Long limit,
                       OrderQueryVo orderQueryVo,
                       HttpServletRequest httpServletRequest) {
        orderQueryVo.setUserId(AuthContextHolder.getUserId(httpServletRequest));
        Page<OrderInfo> pageParams = new Page<>(page, limit);
        IPage<OrderInfo> pageModel = orderService.selectPage(pageParams, orderQueryVo);
        return Result.ok(pageModel);
    }

    @ApiOperation(value = "获取订单状态")
    @GetMapping("auth/getStatusList")
    public Result getStatusList() {
        return Result.ok(OrderStatusEnum.getStatusList());
    }

}

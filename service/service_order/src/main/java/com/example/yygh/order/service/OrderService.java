package com.example.yygh.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.yygh.model.order.OrderInfo;
import com.example.yygh.model.user.UserInfo;
import com.example.yygh.vo.order.OrderQueryVo;

public interface OrderService extends IService<OrderInfo> {
    Long saveOrder(String scheduleId, Long patientId);

    OrderInfo getOrders(Long orderId);

    IPage<OrderInfo> selectPage(Page<OrderInfo> pageParams, OrderQueryVo orderQueryVo);
}

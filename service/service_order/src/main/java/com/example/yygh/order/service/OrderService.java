package com.example.yygh.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.yygh.model.order.OrderInfo;

public interface OrderService extends IService<OrderInfo> {
    Long saveOrder(String scheduleId, Long patientId);
}

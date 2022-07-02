package com.example.yygh.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.yygh.model.order.OrderInfo;
import com.example.yygh.model.order.PaymentInfo;

import java.util.Map;

public interface PaymentService extends IService<PaymentInfo> {
    void savePaymentInfo(OrderInfo orderInfo, Integer status);

    void paySuccess(String tradeNo, Map<String, String> resultMap);

    PaymentInfo getPaymentInfo(Long orderId, Integer paymentType);
}

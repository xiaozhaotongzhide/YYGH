package com.example.yygh.order.service;

import java.util.Map;

public interface WeixinService {
    //生成二维码
    Map createNative(Long orderId);

    //查询订单状态
    Map<String, String> queryPayStatus(Long orderId);

    //退款方法
    Boolean refund(Long orderId);

    Boolean cancelOrder(Long orderId);
}

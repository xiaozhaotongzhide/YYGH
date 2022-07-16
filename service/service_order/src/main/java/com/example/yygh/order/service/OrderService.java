package com.example.yygh.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.yygh.model.order.OrderInfo;
import com.example.yygh.model.oss.Download;
import com.example.yygh.model.user.UserInfo;
import com.example.yygh.vo.download.DownloadCountQueryVo;
import com.example.yygh.vo.order.OrderCountQueryVo;
import com.example.yygh.vo.order.OrderQueryVo;

import java.util.List;
import java.util.Map;

public interface OrderService extends IService<OrderInfo> {
    Long saveOrder(String scheduleId, Long patientId);

    OrderInfo getOrders(Long orderId);

    IPage<OrderInfo> selectPage(Page<OrderInfo> pageParams, OrderQueryVo orderQueryVo);

    void patientTips();
    /**
     * 订单统计
     */
    Map<String, Object> getCountMap(OrderCountQueryVo orderCountQueryVo);

    Map<String, Object> getAmount(OrderCountQueryVo orderCountQueryVo);

    List<OrderInfo> getDownload(Download download);
}

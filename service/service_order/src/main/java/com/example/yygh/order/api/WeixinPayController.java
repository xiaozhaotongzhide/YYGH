package com.example.yygh.order.api;

import com.example.yygh.common.result.Result;
import com.example.yygh.order.service.PaymentService;
import com.example.yygh.order.service.WeixinService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/order/weixin")
@Slf4j
public class WeixinPayController {

    @Autowired
    private WeixinService weixinService;

    @Autowired
    private PaymentService paymentService;

    //生成微信支付扫描的二维码
    @GetMapping("/createNative/{orderId}")
    public Result createNative(@PathVariable Long orderId) {
        log.info("开始创建二维码" + orderId);
        Map aNative = weixinService.createNative(orderId);
        return Result.ok(aNative);
    }

    //查询支付状态
    @GetMapping("/queryPayStatus/{orderId}")
    public Result queryPayStatus(@PathVariable Long orderId) {
        log.info("查询支付订单状态");
        //调用微信接口实现支付状态查询
        Map<String, String> resultMap = weixinService.queryPayStatus(orderId);
        //判断
        if (resultMap == null) {
            return Result.fail().message("支付出错");
        }
        if ("SUCCESS".equals(resultMap.get("trade_state"))) {
            //支付成功,更新订单状态
            String tradeNo = resultMap.get("out_trade_no");
            paymentService.paySuccess(tradeNo,resultMap);
            return Result.ok().message("支付成功");
        }
        return Result.ok().message("支付中");
    }

    //取消预约
    @GetMapping("auth/cancelOrder/{orderId}")
    public Result cancelOrder(@PathVariable Long orderId) {
        Boolean isOrder = weixinService.cancelOrder(orderId);
        return Result.ok(isOrder);
    }
}

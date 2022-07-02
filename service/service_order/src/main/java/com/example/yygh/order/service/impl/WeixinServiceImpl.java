package com.example.yygh.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.yygh.common.exception.YyghException;
import com.example.yygh.common.helper.HttpRequestHelper;
import com.example.yygh.common.result.ResultCodeEnum;
import com.example.yygh.enums.OrderStatusEnum;
import com.example.yygh.enums.PaymentTypeEnum;
import com.example.yygh.enums.RefundStatusEnum;
import com.example.yygh.hosp.client.HospitalFeignClient;
import com.example.yygh.model.order.OrderInfo;
import com.example.yygh.model.order.PaymentInfo;
import com.example.yygh.model.order.RefundInfo;
import com.example.yygh.order.service.OrderService;
import com.example.yygh.order.service.PaymentService;
import com.example.yygh.order.service.RefundInfoService;
import com.example.yygh.order.service.WeixinService;
import com.example.yygh.order.utils.ConstantPropertiesUtils;
import com.example.yygh.order.utils.HttpClient;
import com.example.yygh.rabbit.constant.MqConst;
import com.example.yygh.rabbit.service.RabbitService;
import com.example.yygh.vo.msm.MsmVo;
import com.example.yygh.vo.order.OrderMqVo;
import com.example.yygh.vo.order.SignInfoVo;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class WeixinServiceImpl implements WeixinService {

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RefundInfoService refundInfoService;

    @Autowired
    private HospitalFeignClient hospitalFeignClient;

    @Autowired
    private RabbitService rabbitService;

    //生成二维码
    @Override
    public Map createNative(Long orderId) {
        try {
            //先尝试在redis中获取数据
            Map payMap = (Map) redisTemplate.opsForValue().get(orderId.toString());
            if (payMap != null) {
                return payMap;
            }
            //1.根据orderId获取订单信息
            OrderInfo orderInfo = orderService.getById(orderId);
            //2.向支付记录表添加信息
            paymentService.savePaymentInfo(orderInfo, PaymentTypeEnum.WEIXIN.getStatus());
            //3.设置参数,调用微信生成二维码的接口
            //把参数转换成xml格式,使用商户key进行加密
            Map paramMap = getMap(orderInfo);
            //4.调用HttpClient
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            //设置参数
            client.setXmlParam(WXPayUtil.generateSignedXml(paramMap, ConstantPropertiesUtils.PARTNERKEY));
            client.setHttps(true);
            client.post();
            //5.返回相关数据
            String xml = client.getContent();
            //转换成map集合
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);
            //4、封装返回结果集
            Map map = new HashMap<>();
            map.put("orderId", orderId);
            map.put("totalFee", orderInfo.getAmount());
            map.put("resultCode", resultMap.get("result_code"));
            map.put("codeUrl", resultMap.get("code_url")); //二维码地址
            if (null != resultMap.get("result_code")) {
                //微信支付二维码2小时过期，可采取2小时未支付取消订单
                redisTemplate.opsForValue().set(orderId.toString(), map, 1000, TimeUnit.MINUTES);
            }
            return map;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, String> queryPayStatus(Long orderId) {
        try {
            //1.根据orderId获取订单信息
            OrderInfo orderInfo = orderService.getById(orderId);

            //2.封装提交参数
            Map paramMap = new HashMap();
            paramMap.put("appid", ConstantPropertiesUtils.APPID);
            paramMap.put("mch_id", ConstantPropertiesUtils.PARTNER);
            paramMap.put("out_trade_no", orderInfo.getOutTradeNo());
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());

            //3.设置请求内容
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            client.setXmlParam(WXPayUtil.generateSignedXml(paramMap, ConstantPropertiesUtils.PARTNERKEY));
            client.setHttps(true);
            client.post();
            //3、返回第三方的数据，转成Map
            String xml = client.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);
            //4、返回
            return resultMap;
        } catch (Exception e) {
            return null;
        }
    }

    //微信退款
    @Override
    public Boolean refund(Long orderId) {
        try {
            log.info("开始退款" + orderId);
            //获取支付记录信息
            PaymentInfo paymentInfo = paymentService.getPaymentInfo(orderId, PaymentTypeEnum.WEIXIN.getStatus());
            //添加信息到退款信息
            RefundInfo refundInfo = refundInfoService.saveRefundInfo(paymentInfo);
            //如果当前订单已经退款
            if (refundInfo.getRefundStatus().intValue() == RefundStatusEnum.REFUND.getStatus().intValue()) {
                return true;
            }
            //调用微信接口实现退款
            //封装需要的参数
            Map<String, String> paramMap = new HashMap<>(8);
            paramMap.put("appid", ConstantPropertiesUtils.APPID);       //公众账号ID
            paramMap.put("mch_id", ConstantPropertiesUtils.PARTNER);   //商户编号
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            paramMap.put("transaction_id", paymentInfo.getTradeNo()); //微信订单号
            paramMap.put("out_trade_no", paymentInfo.getOutTradeNo()); //商户订单编号
            paramMap.put("out_refund_no", "tk" + paymentInfo.getOutTradeNo()); //商户退款单号
//       paramMap.put("total_fee",paymentInfoQuery.getTotalAmount().multiply(new BigDecimal("100")).longValue()+"");
//       paramMap.put("refund_fee",paymentInfoQuery.getTotalAmount().multiply(new BigDecimal("100")).longValue()+"");
            paramMap.put("total_fee", "1");
            paramMap.put("refund_fee", "1");
            String paramXml = WXPayUtil.generateSignedXml(paramMap, ConstantPropertiesUtils.PARTNERKEY);
            //设置接口内容
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/secapi/pay/refund");
            client.setXmlParam(paramXml);
            client.setHttps(true);
            //设置证书信息
            client.setCert(true);
            client.setCertPassword(ConstantPropertiesUtils.PARTNER);
            client.post();
            //接受返回数据
            String xml = client.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);
            if (!ObjectUtils.isEmpty(resultMap) && WXPayConstants.SUCCESS.equalsIgnoreCase(resultMap.get("result_code"))) {
                refundInfo.setCallbackTime(new Date());
                refundInfo.setTradeNo(resultMap.get("refund_id"));
                refundInfo.setRefundStatus(RefundStatusEnum.REFUND.getStatus());
                refundInfo.setCallbackContent(JSONObject.toJSONString(resultMap));
                refundInfoService.updateById(refundInfo);
            }
            return true;
        } catch (Exception e) {
            return false;
        }

    }

    @Override
    public Boolean cancelOrder(Long orderId) {
        //1.根据订单id得到订单信息
        OrderInfo orderInfo = orderService.getById(orderId);
        //2.判断是否取消
        DateTime quitTime = new DateTime(orderInfo.getQuitTime());
        //校验时间,对比最后时间与现在
        /*if (quitTime.isBeforeNow()) {
            throw new YyghException(ResultCodeEnum.CANCEL_ORDER_NO);
        }*/
        //3.调用医院接口实现预约取消
        SignInfoVo signInfoVo = hospitalFeignClient.getSignInfoVo(orderInfo.getHoscode());
        if (null == signInfoVo) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }

        Map<String, Object> reqMap = new HashMap<>();
        reqMap.put("hoscode", orderInfo.getHoscode());
        reqMap.put("hosRecordId", orderInfo.getHosRecordId());
        reqMap.put("timestamp", HttpRequestHelper.getTimestamp());
        String sign = HttpRequestHelper.getSign(reqMap, signInfoVo.getSignKey());
        reqMap.put("sign", sign);

        JSONObject result = HttpRequestHelper.sendRequest(reqMap, signInfoVo.getApiUrl() + "/order/updateCancelStatus");

        //根据医院接口返回数据
        if (result.getInteger("code") != 200) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        } else {
            //判断当前订单是否已支付
            if (orderInfo.getOrderStatus().intValue() == OrderStatusEnum.PAID.getStatus().intValue()) {
                Boolean refund = this.refund(orderId);
                if (!refund) {
                    throw new YyghException(ResultCodeEnum.CANCEL_ORDER_FAIL);
                }
                //更新订单状态
                orderInfo.setOrderStatus(OrderStatusEnum.CANCLE.getStatus());
                orderService.updateById(orderInfo);

                //剩余数量+1
                //发送mq信息更新预约数 我们与下单成功更新预约数使用相同的mq信息，不设置可预约数与剩余预约数，接收端可预约数减1即可
                rabbitMq(orderInfo);
            }
            return true;
        }
    }

    private void rabbitMq(OrderInfo orderInfo) {
        OrderMqVo orderMqVo = new OrderMqVo();
        orderMqVo.setScheduleId(orderInfo.getScheduleId());
        //短信提示
        MsmVo msmVo = new MsmVo();
        msmVo.setPhone(orderInfo.getPatientPhone());
        msmVo.setTemplateCode("SMS_194640722");
        String reserveDate = new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd") + (orderInfo.getReserveTime()==0 ? "上午": "下午");
        Map<String,Object> param = new HashMap<String,Object>(){{
            put("title", orderInfo.getHosname()+"|"+ orderInfo.getDepname()+"|"+ orderInfo.getTitle());
            put("reserveDate", reserveDate);
            put("name", orderInfo.getPatientName());
        }};
        msmVo.setParam(param);
        orderMqVo.setMsmVo(msmVo);
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER, MqConst.ROUTING_ORDER, orderMqVo);
    }

    private Map getMap(OrderInfo orderInfo) {
        Map paramMap = new HashMap<>();
        paramMap.put("appid", ConstantPropertiesUtils.APPID);
        paramMap.put("mch_id", ConstantPropertiesUtils.PARTNER);
        paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
        String body = orderInfo.getReserveDate() + "就诊" + orderInfo.getDepname();
        paramMap.put("body", body);
        paramMap.put("out_trade_no", orderInfo.getOutTradeNo());
        //paramMap.put("total_fee", order.getAmount().multiply(new BigDecimal("100")).longValue()+"");
        paramMap.put("total_fee", "1"); //测试时同一设置成1分钱
        paramMap.put("spbill_create_ip", "127.0.0.1");
        paramMap.put("notify_url", "http://guli.shop/api/order/weixinPay/weixinNotify");
        paramMap.put("trade_type", "NATIVE");
        return paramMap;
    }
}

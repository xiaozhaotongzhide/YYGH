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

    //???????????????
    @Override
    public Map createNative(Long orderId) {
        try {
            //????????????redis???????????????
            Map payMap = (Map) redisTemplate.opsForValue().get(orderId.toString());
            if (payMap != null) {
                return payMap;
            }
            //1.??????orderId??????????????????
            OrderInfo orderInfo = orderService.getById(orderId);
            //2.??????????????????????????????
            paymentService.savePaymentInfo(orderInfo, PaymentTypeEnum.WEIXIN.getStatus());
            //3.????????????,????????????????????????????????????
            //??????????????????xml??????,????????????key????????????
            Map paramMap = getMap(orderInfo);
            //4.??????HttpClient
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            //????????????
            client.setXmlParam(WXPayUtil.generateSignedXml(paramMap, ConstantPropertiesUtils.PARTNERKEY));
            client.setHttps(true);
            client.post();
            //5.??????????????????
            String xml = client.getContent();
            //?????????map??????
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);
            //4????????????????????????
            Map map = new HashMap<>();
            map.put("orderId", orderId);
            map.put("totalFee", orderInfo.getAmount());
            map.put("resultCode", resultMap.get("result_code"));
            map.put("codeUrl", resultMap.get("code_url")); //???????????????
            if (null != resultMap.get("result_code")) {
                //?????????????????????2????????????????????????2???????????????????????????
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
            //1.??????orderId??????????????????
            OrderInfo orderInfo = orderService.getById(orderId);

            //2.??????????????????
            Map paramMap = new HashMap();
            paramMap.put("appid", ConstantPropertiesUtils.APPID);
            paramMap.put("mch_id", ConstantPropertiesUtils.PARTNER);
            paramMap.put("out_trade_no", orderInfo.getOutTradeNo());
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());

            //3.??????????????????
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            client.setXmlParam(WXPayUtil.generateSignedXml(paramMap, ConstantPropertiesUtils.PARTNERKEY));
            client.setHttps(true);
            client.post();
            //3????????????????????????????????????Map
            String xml = client.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);
            //4?????????
            return resultMap;
        } catch (Exception e) {
            return null;
        }
    }

    //????????????
    @Override
    public Boolean refund(Long orderId) {
        try {
            log.info("????????????" + orderId);
            //????????????????????????
            PaymentInfo paymentInfo = paymentService.getPaymentInfo(orderId, PaymentTypeEnum.WEIXIN.getStatus());
            //???????????????????????????
            RefundInfo refundInfo = refundInfoService.saveRefundInfo(paymentInfo);
            //??????????????????????????????
            if (refundInfo.getRefundStatus().intValue() == RefundStatusEnum.REFUND.getStatus().intValue()) {
                return true;
            }
            //??????????????????????????????
            //?????????????????????
            Map<String, String> paramMap = new HashMap<>(8);
            paramMap.put("appid", ConstantPropertiesUtils.APPID);       //????????????ID
            paramMap.put("mch_id", ConstantPropertiesUtils.PARTNER);   //????????????
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            paramMap.put("transaction_id", paymentInfo.getTradeNo()); //???????????????
            paramMap.put("out_trade_no", paymentInfo.getOutTradeNo()); //??????????????????
            paramMap.put("out_refund_no", "tk" + paymentInfo.getOutTradeNo()); //??????????????????
//       paramMap.put("total_fee",paymentInfoQuery.getTotalAmount().multiply(new BigDecimal("100")).longValue()+"");
//       paramMap.put("refund_fee",paymentInfoQuery.getTotalAmount().multiply(new BigDecimal("100")).longValue()+"");
            paramMap.put("total_fee", "1");
            paramMap.put("refund_fee", "1");
            String paramXml = WXPayUtil.generateSignedXml(paramMap, ConstantPropertiesUtils.PARTNERKEY);
            //??????????????????
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/secapi/pay/refund");
            client.setXmlParam(paramXml);
            client.setHttps(true);
            //??????????????????
            client.setCert(true);
            client.setCertPassword(ConstantPropertiesUtils.PARTNER);
            client.post();
            //??????????????????
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
        //1.????????????id??????????????????
        OrderInfo orderInfo = orderService.getById(orderId);
        //2.??????????????????
        DateTime quitTime = new DateTime(orderInfo.getQuitTime());
        //????????????,???????????????????????????
        /*if (quitTime.isBeforeNow()) {
            throw new YyghException(ResultCodeEnum.CANCEL_ORDER_NO);
        }*/
        //3.????????????????????????????????????
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

        //??????????????????????????????
        if (result.getInteger("code") != 200) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        } else {
            //?????????????????????????????????
            if (orderInfo.getOrderStatus().intValue() == OrderStatusEnum.PAID.getStatus().intValue()) {
                Boolean refund = this.refund(orderId);
                if (!refund) {
                    throw new YyghException(ResultCodeEnum.CANCEL_ORDER_FAIL);
                }
                //??????????????????
                orderInfo.setOrderStatus(OrderStatusEnum.CANCLE.getStatus());
                orderService.updateById(orderInfo);

                //????????????+1
                //??????mq????????????????????? ???????????????????????????????????????????????????mq???????????????????????????????????????????????????????????????????????????1??????
                rabbitMq(orderInfo);
            }
            return true;
        }
    }

    private void rabbitMq(OrderInfo orderInfo) {
        OrderMqVo orderMqVo = new OrderMqVo();
        orderMqVo.setScheduleId(orderInfo.getScheduleId());
        //????????????
        MsmVo msmVo = new MsmVo();
        msmVo.setPhone(orderInfo.getPatientPhone());
        msmVo.setTemplateCode("SMS_194640722");
        String reserveDate = new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd") + (orderInfo.getReserveTime()==0 ? "??????": "??????");
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
        String body = orderInfo.getReserveDate() + "??????" + orderInfo.getDepname();
        paramMap.put("body", body);
        paramMap.put("out_trade_no", orderInfo.getOutTradeNo());
        //paramMap.put("total_fee", order.getAmount().multiply(new BigDecimal("100")).longValue()+"");
        paramMap.put("total_fee", "1"); //????????????????????????1??????
        paramMap.put("spbill_create_ip", "127.0.0.1");
        paramMap.put("notify_url", "http://guli.shop/api/order/weixinPay/weixinNotify");
        paramMap.put("trade_type", "NATIVE");
        return paramMap;
    }
}

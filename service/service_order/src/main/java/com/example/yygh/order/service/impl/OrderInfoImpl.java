package com.example.yygh.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.yygh.common.exception.YyghException;
import com.example.yygh.common.helper.HttpRequestHelper;
import com.example.yygh.common.result.ResultCodeEnum;
import com.example.yygh.enums.OrderStatusEnum;
import com.example.yygh.hosp.client.HospitalFeignClient;
import com.example.yygh.model.order.OrderInfo;
import com.example.yygh.model.user.Patient;
import com.example.yygh.model.user.UserInfo;
import com.example.yygh.order.mapper.OrderMapper;
import com.example.yygh.order.service.OrderService;
import com.example.yygh.rabbit.constant.MqConst;
import com.example.yygh.rabbit.service.RabbitService;
import com.example.yygh.user.client.PatientFeignClient;
import com.example.yygh.vo.hosp.ScheduleOrderVo;
import com.example.yygh.vo.msm.MsmVo;
import com.example.yygh.vo.order.OrderMqVo;
import com.example.yygh.vo.order.OrderQueryVo;
import com.example.yygh.vo.order.SignInfoVo;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class OrderInfoImpl extends ServiceImpl<OrderMapper, OrderInfo> implements OrderService {

    @Autowired
    private PatientFeignClient patientFeignClient;

    @Autowired
    private HospitalFeignClient hospitalFeignClient;

    @Autowired
    private RabbitService rabbitService;

    @Override
    public Long saveOrder(String scheduleId, Long patientId) {
        //获取就诊人信息
        Patient patient = patientFeignClient.getPatientOrder(patientId);
        //获取排班相关信息
        ScheduleOrderVo scheduleOrderVo = hospitalFeignClient.getScheduleOrderVo(scheduleId);
        //判断时间是否可以预约
        /*if (new DateTime(scheduleOrderVo.getStartTime()).isAfterNow() ||
                new DateTime(scheduleOrderVo.getEndTime()).isBeforeNow()) {
            throw new YyghException(ResultCodeEnum.TIME_NO);
        }*/
        //获取签名信息
        SignInfoVo signInfoVo = hospitalFeignClient.getSignInfoVo(scheduleOrderVo.getHoscode());

        //添加到订单表
        OrderInfo orderInfo = new OrderInfo();
        BeanUtils.copyProperties(scheduleOrderVo, orderInfo);
        String outTradeNo = System.currentTimeMillis() + "" + new Random().nextInt(100);
        orderInfo.setOutTradeNo(outTradeNo);
        orderInfo.setScheduleId(scheduleId);
        orderInfo.setUserId(patient.getUserId());
        orderInfo.setPatientId(patientId);
        orderInfo.setPatientName(patient.getName());
        orderInfo.setPatientPhone(patient.getPhone());
        orderInfo.setOrderStatus(OrderStatusEnum.UNPAID.getStatus());
        baseMapper.insert(orderInfo);

        //调用医院接口,实现预约挂号操作
        //先设置调用医院接口需要参数,添加放到map集合
        Map<String, Object> paramMap = hospParameter(patient, signInfoVo, orderInfo);

        //请求医院系统接口
        JSONObject result = HttpRequestHelper.sendRequest(paramMap, signInfoVo.getApiUrl() + "/order/submitOrder");

        //如果成功
        if (result.getInteger("code") == 200) {
            updateOrder(orderInfo, result, scheduleId);
        } else {
            throw new YyghException(result.getString("message"), ResultCodeEnum.FAIL.getCode());
        }
        return orderInfo.getId();
    }

    @Override
    public OrderInfo getOrders(Long orderId) {
        OrderInfo orderInfo = baseMapper.selectById(orderId);
        return packOrderInfo(orderInfo);
    }

    @Override
    public IPage<OrderInfo> selectPage(Page<OrderInfo> pageParams, OrderQueryVo orderQueryVo) {
        //orderQueryVo获取条件值
        String name = orderQueryVo.getKeyword(); //医院名称
        Long patientId = orderQueryVo.getPatientId(); //就诊人名称
        String orderStatus = orderQueryVo.getOrderStatus(); //订单状态
        String reserveDate = orderQueryVo.getReserveDate();//安排时间
        String createTimeBegin = orderQueryVo.getCreateTimeBegin();
        String createTimeEnd = orderQueryVo.getCreateTimeEnd();


        //对条件值进行非空判断
        QueryWrapper<OrderInfo> wrapper = new QueryWrapper<>();
        if(!StringUtils.isEmpty(name)) {
            wrapper.like("hosname",name);
        }
        if(!StringUtils.isEmpty(patientId)) {
            wrapper.eq("patient_id",patientId);
        }
        if(!StringUtils.isEmpty(orderStatus)) {
            wrapper.eq("order_status",orderStatus);
        }
        if(!StringUtils.isEmpty(reserveDate)) {
            wrapper.ge("reserve_date",reserveDate);
        }
        if(!StringUtils.isEmpty(createTimeBegin)) {
            wrapper.ge("create_time",createTimeBegin);
        }
        if(!StringUtils.isEmpty(createTimeEnd)) {
            wrapper.le("create_time",createTimeEnd);
        }
        //调用mapper的方法
        IPage<OrderInfo> pages = baseMapper.selectPage(pageParams, wrapper);
        //编号变成对应值封装
        pages.getRecords().stream().forEach(item -> {
            this.packOrderInfo(item);
        });
        return pages;
    }

    private OrderInfo packOrderInfo(OrderInfo orderInfo) {
        orderInfo.getParam().put("orderStatusString", OrderStatusEnum.getStatusNameByStatus(orderInfo.getOrderStatus()));
        return orderInfo;
    }

    private void updateOrder(OrderInfo orderInfo, JSONObject result, String scheduleId) {
        JSONObject jsonObject = result.getJSONObject("data");
        //预约记录唯一标识（医院预约记录主键）
        String hosRecordId = jsonObject.getString("hosRecordId");
        //预约序号
        Integer number = jsonObject.getInteger("number");
        //取号时间
        String fetchTime = jsonObject.getString("fetchTime");
        //取号地址
        String fetchAddress = jsonObject.getString("fetchAddress");
        //更新订单
        orderInfo.setHosRecordId(hosRecordId);
        orderInfo.setNumber(number);
        orderInfo.setFetchTime(fetchTime);
        orderInfo.setFetchAddress(fetchAddress);
        baseMapper.updateById(orderInfo);
        //排班可预约数
        Integer reservedNumber = jsonObject.getInteger("reservedNumber");
        //排班剩余预约数
        Integer availableNumber = jsonObject.getInteger("availableNumber");

        //发送mq信息更新号源和短信通知
        //发送mq进行号源更新

        OrderMqVo orderMqVo = new OrderMqVo();
        orderMqVo.setScheduleId(scheduleId);
        orderMqVo.setReservedNumber(reservedNumber);
        orderMqVo.setAvailableNumber(availableNumber);

        //邮件提示
        MsmVo msmVo = new MsmVo();
        msmVo.setPhone(orderInfo.getPatientPhone());
        String reserveDate =
                new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd")
                        + (orderInfo.getReserveTime() == 0 ? "上午" : "下午");
        Map<String, Object> param = new HashMap<String, Object>() {{
            put("title", orderInfo.getHosname() + "|" + orderInfo.getDepname() + "|" + orderInfo.getTitle());
            put("amount", orderInfo.getAmount());
            put("reserveDate", reserveDate);
            put("name", orderInfo.getPatientName());
            put("quitTime", new DateTime(orderInfo.getQuitTime()).toString("yyyy-MM-dd HH:mm"));
        }};
        msmVo.setParam(param);
        orderMqVo.setMsmVo(msmVo);
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER, MqConst.ROUTING_ORDER, orderMqVo);

    }

    //设置医院请求参数
    private Map<String, Object> hospParameter(Patient patient, SignInfoVo signInfoVo, OrderInfo orderInfo) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("hoscode", orderInfo.getHoscode());
        paramMap.put("depcode", orderInfo.getDepcode());
        paramMap.put("hosScheduleId", orderInfo.getScheduleId());
        paramMap.put("reserveDate", new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd"));
        paramMap.put("reserveTime", orderInfo.getReserveTime());
        paramMap.put("amount", orderInfo.getAmount());
        paramMap.put("name", patient.getName());
        paramMap.put("certificatesType", patient.getCertificatesType());
        paramMap.put("certificatesNo", patient.getCertificatesNo());
        paramMap.put("sex", patient.getSex());
        paramMap.put("birthdate", patient.getBirthdate());
        paramMap.put("phone", patient.getPhone());
        paramMap.put("isMarry", patient.getIsMarry());
        paramMap.put("provinceCode", patient.getProvinceCode());
        paramMap.put("cityCode", patient.getCityCode());
        paramMap.put("districtCode", patient.getDistrictCode());
        paramMap.put("address", patient.getAddress());
        //联系人
        paramMap.put("contactsName", patient.getContactsName());
        paramMap.put("contactsCertificatesType", patient.getContactsCertificatesType());
        paramMap.put("contactsCertificatesNo", patient.getContactsCertificatesNo());
        paramMap.put("contactsPhone", patient.getContactsPhone());
        paramMap.put("timestamp", HttpRequestHelper.getTimestamp());
        String sign = HttpRequestHelper.getSign(paramMap, signInfoVo.getSignKey());
        paramMap.put("sign", sign);
        return paramMap;
    }
}

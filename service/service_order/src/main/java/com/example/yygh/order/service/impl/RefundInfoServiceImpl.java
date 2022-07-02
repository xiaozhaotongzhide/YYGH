package com.example.yygh.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.yygh.enums.RefundStatusEnum;
import com.example.yygh.model.order.PaymentInfo;
import com.example.yygh.model.order.RefundInfo;
import com.example.yygh.order.mapper.RefundInfoMapper;
import com.example.yygh.order.service.RefundInfoService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Date;

@Service
public class RefundInfoServiceImpl extends ServiceImpl<RefundInfoMapper, RefundInfo> implements RefundInfoService {

    @Override
    public RefundInfo saveRefundInfo(PaymentInfo paymentInfo) {
        //判断是否重复
        QueryWrapper<RefundInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", paymentInfo.getOrderId());
        queryWrapper.eq("payment_type", paymentInfo.getPaymentType());
        RefundInfo refundInfo = baseMapper.selectOne(queryWrapper);
        //有相同的数据,并且支付状态不能是已经退款
        if (!ObjectUtils.isEmpty(refundInfo)) {
            return refundInfo;
        }
        //添加记录
        RefundInfo info = new RefundInfo();
        BeanUtils.copyProperties(paymentInfo, info);
        info.setCallbackTime(new Date());
        info.setRefundStatus(RefundStatusEnum.UNREFUND.getStatus());
        baseMapper.insert(info);
        return info;
    }
}

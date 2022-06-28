package com.example.yygh.hosp.receiver;

import com.example.yygh.hosp.service.ScheduleService;
import com.example.yygh.model.hosp.Schedule;
import com.example.yygh.rabbit.constant.MqConst;
import com.example.yygh.rabbit.service.RabbitService;
import com.example.yygh.vo.msm.MsmVo;
import com.example.yygh.vo.order.OrderMqVo;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component
@Slf4j
public class HospitalReceiver {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private RabbitService rabbitService;


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_ORDER, durable = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_ORDER),
            key = {MqConst.ROUTING_ORDER}
    ))
    public void receive(OrderMqVo orderMqVo, Message message, Channel channel) {
        log.info("接受到rabbitmq的消息开始更新下单数" + orderMqVo.toString());
        //下单成功更新预约数
        Schedule schedule = scheduleService.getSchedule(orderMqVo.getScheduleId());
        schedule.setAvailableNumber(orderMqVo.getAvailableNumber());
        schedule.setReservedNumber(orderMqVo.getReservedNumber());
        scheduleService.update(schedule);
        //发送邮件
        MsmVo msmVo = orderMqVo.getMsmVo();
        if (!ObjectUtils.isEmpty(msmVo)) {
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_MSM, MqConst.ROUTING_MSM_ITEM, msmVo);
        }
    }
}

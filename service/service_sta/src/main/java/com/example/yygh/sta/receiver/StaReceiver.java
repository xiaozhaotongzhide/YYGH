package com.example.yygh.sta.receiver;

import com.example.yygh.rabbit.constant.MqConst;
import com.example.yygh.sta.service.cpuService;
import com.example.yygh.sta.service.ramService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class StaReceiver {

    @Autowired
    private cpuService cpuService;

    @Autowired
    private ramService ramService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_TASK_6, durable = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_TASK),
            key = {MqConst.ROUTING_TASK_6}
    ))
    public void startActuator(String str) {
        log.info("收到信息" + str);
        cpuService.save();
        ramService.save();
    }
}

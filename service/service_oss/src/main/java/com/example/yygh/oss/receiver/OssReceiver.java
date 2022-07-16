package com.example.yygh.oss.receiver;

import com.example.yygh.oss.service.DownloadService;
import com.example.yygh.rabbit.constant.MqConst;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OssReceiver {

    @Autowired
    private DownloadService downloadService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_TASK_7, durable = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_TASK),
            key = {MqConst.ROUTING_TASK_7}
    ))
    public void patientTips(String str) {
        log.info("收到信息" + str);
        downloadService.startDownload();
    }
}

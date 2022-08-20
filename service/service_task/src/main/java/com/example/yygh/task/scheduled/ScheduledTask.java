package com.example.yygh.task.scheduled;

import com.example.yygh.rabbit.constant.MqConst;
import com.example.yygh.rabbit.service.RabbitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@EnableScheduling
@Slf4j
public class ScheduledTask {

    @Autowired
    private RabbitService rabbitService;

    //每天八点执行方法,就医提醒
    //0 0 8 * * ?
    //每第30s开始执行
    @Scheduled(cron = "0 0 8 * * ?")
    public void taskPatient() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        log.info("定时任务开始执行" + sdf.format(date));
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_TASK, MqConst.ROUTING_TASK_8, "startPatient");
    }

    /**
     * 定时下载任务
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void taskDownload() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        log.info("定时任务开始执行" + sdf.format(date));
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_TASK, MqConst.ROUTING_TASK_7, "startDownload");
    }

    @Scheduled(cron = "0 */1 * * * ?")
    public void taskActuator() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        log.info("定时任务开始执行" + sdf.format(date));
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_TASK, MqConst.ROUTING_TASK_6, "startActuator");
    }
}

package com.example.yygh.hosp.client;

import com.example.yygh.vo.hosp.ScheduleOrderVo;
import com.example.yygh.vo.order.SignInfoVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
@FeignClient(value = "service-hosp")
@Repository
public interface HospitalFeignClient {

    /**
     * 根据排班id获取预约下单数据
     */
    @GetMapping("/api/hosp/hospital/inner/getScheduleOrderVo/{scheduleId}")
    ScheduleOrderVo getScheduleOrderVo(@PathVariable("scheduleId") String scheduleId);

    /**
     * 获取医院签名信息
     */
    @GetMapping("/api/hosp/hospital/inner/getSignInfoVo/{hoscode}")
    SignInfoVo getSignInfoVo(@PathVariable("hoscode") String hoscode);

    /**
     * 获取医院编号
     */
    @GetMapping("/api/hosp/hospital/inner/getHoscode")
    public String getHoscode(String hosname);

    /**
     * hosp模块堆内存占用情况
     */
    @GetMapping("/actuator/metrics/jvm.memory.used?tag=area:heap")
    public String getRam();

    /**
     * hosp模块内存占用率
     * @return
     */
    @GetMapping("/actuator/metrics/system.cpu.usage")
    public String getCpu();
}

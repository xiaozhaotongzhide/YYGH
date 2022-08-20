package com.example.yygh.chat.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(value = "service-chat")
public interface chatFeignClient {

    /**
     * msm模块堆内存占用情况
     */
    @GetMapping("/actuator/metrics/jvm.memory.used?tag=area:heap")
    public String getRam();

    /**
     * msm模块内存占用率
     * @return
     */
    @GetMapping("/actuator/metrics/system.cpu.usage")
    public String getCpu();
}

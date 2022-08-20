package com.example.yygh.order.client;

import com.example.yygh.model.order.OrderInfo;
import com.example.yygh.model.oss.Download;
import com.example.yygh.vo.download.DownloadCountQueryVo;
import com.example.yygh.vo.order.OrderCountQueryVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;


@FeignClient("service-order")
public interface OrderFeignClient {
    /**
     * 统计每天的预约数
     * @param orderCountQueryVo
     * @return
     */
    @PostMapping("/api/order/orderInfo/inner/getCountMap")
    public Map<String, Object> getCountMap(@RequestBody OrderCountQueryVo orderCountQueryVo);

    /**
     * 统计每天的挂号金额
     * @param orderCountQueryVo
     * @return
     */
    @PostMapping("/api/order/orderInfo/inner/getAmount")
    public Map<String, Object> getAmunt(@RequestBody OrderCountQueryVo orderCountQueryVo);

    /**
     * oss模块下载的数据
     * @param download
     * @return
     */
    @PostMapping("/api/order/orderInfo/inner/getDownload")
    public List<OrderInfo> getDownload(@RequestBody Download download);

    /**
     * order模块堆内存占用情况
     */
    @GetMapping("/actuator/metrics/jvm.memory.used?tag=area:heap")
    public String getRam();

    /**
     * order模块内存占用率
     * @return
     */
    @GetMapping("/actuator/metrics/system.cpu.usage")
    public String getCpu();
}

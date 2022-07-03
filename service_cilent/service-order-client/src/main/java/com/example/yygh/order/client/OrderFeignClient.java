package com.example.yygh.order.client;

import com.example.yygh.vo.order.OrderCountQueryVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

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

}

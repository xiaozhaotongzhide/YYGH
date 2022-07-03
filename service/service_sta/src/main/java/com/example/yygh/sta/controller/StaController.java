package com.example.yygh.sta.controller;

import com.example.yygh.common.result.Result;
import com.example.yygh.order.client.OrderFeignClient;
import com.example.yygh.vo.order.OrderCountQueryVo;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/sta")
public class StaController {
    @Autowired
    private OrderFeignClient orderFeignClient;

    @ApiOperation(value = "获取订单统计数据")
    @GetMapping("getCountMap")
    public Result getCountMap(@ApiParam(name = "orderCountQueryVo", value = "查询对象", required = false) OrderCountQueryVo orderCountQueryVo) {
        return Result.ok(orderFeignClient.getCountMap(orderCountQueryVo));
    }

    @ApiOperation(value = "获取金额统计数据")
    @GetMapping("getAmount")
    public Result getAmount(@ApiParam(name = "orderCountQueryVo", value = "查询对象", required = false) OrderCountQueryVo orderCountQueryVo) {
        return Result.ok(orderFeignClient.getAmunt(orderCountQueryVo));
    }
}

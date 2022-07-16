package com.example.yygh.order.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.yygh.common.result.Result;
import com.example.yygh.common.utils.AuthContextHolder;
import com.example.yygh.enums.OrderStatusEnum;
import com.example.yygh.model.order.OrderInfo;
import com.example.yygh.model.oss.Download;
import com.example.yygh.model.user.UserInfo;
import com.example.yygh.order.service.OrderService;
import com.example.yygh.vo.download.DownloadCountQueryVo;
import com.example.yygh.vo.order.OrderCountQueryVo;
import com.example.yygh.vo.order.OrderQueryVo;
import com.example.yygh.vo.user.UserInfoQueryVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order/orderInfo")
public class OrderApiController {

    @Autowired
    private OrderService orderService;

    //生成挂号订单
    @PostMapping("auth/submitOrder/{scheduleId}/{patientId}")
    public Result saveOrder(@PathVariable String scheduleId,
                            @PathVariable Long patientId) {
        Long orderId = orderService.saveOrder(scheduleId, patientId);
        return Result.ok(orderId);
    }

    //根据订单id查询订单详情
    @GetMapping("auth/getOrders/{orderId}")
    public Result getOrders(@PathVariable Long orderId) {
        OrderInfo orderInfo = orderService.getOrders(orderId);
        return Result.ok(orderInfo);
    }

    //订单列表(条件查询带分页)
    @GetMapping("auth/{page}/{limit}")
    public Result list(@PathVariable Long page,
                       @PathVariable Long limit,
                       OrderQueryVo orderQueryVo,
                       HttpServletRequest httpServletRequest) {
        orderQueryVo.setUserId(AuthContextHolder.getUserId(httpServletRequest));
        Page<OrderInfo> pageParams = new Page<>(page, limit);
        IPage<OrderInfo> pageModel = orderService.selectPage(pageParams, orderQueryVo);
        return Result.ok(pageModel);
    }

    @ApiOperation(value = "获取订单状态")
    @GetMapping("auth/getStatusList")
    public Result getStatusList() {
        return Result.ok(OrderStatusEnum.getStatusList());
    }

    /**
     * 统计每天的预约数
     *
     * @param orderCountQueryVo
     * @return
     */
    @PostMapping("inner/getCountMap")
    public Map<String, Object> getCountMap(@RequestBody OrderCountQueryVo orderCountQueryVo) {
        return orderService.getCountMap(orderCountQueryVo);
    }

    /**
     * 统计每天的金额数
     *
     * @param orderCountQueryVo
     * @return
     */
    @PostMapping("inner/getAmount")
    public Map<String, Object> getAmount(@RequestBody OrderCountQueryVo orderCountQueryVo) {
        return orderService.getAmount(orderCountQueryVo);
    }

    @PostMapping("inner/getDownload")
    public List<OrderInfo> getDownload(@RequestBody Download download) {
        List<OrderInfo> orderInfoList = orderService.getDownload(download);
        return orderInfoList;
    }
}

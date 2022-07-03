package com.example.yygh.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.yygh.model.order.OrderInfo;
import com.example.yygh.vo.order.OrderAmountVo;
import com.example.yygh.vo.order.OrderCountQueryVo;
import com.example.yygh.vo.order.OrderCountVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * SELECT
 * 	reserve_date AS reserveDate,
 * 	sum( amount ) AS amount
 * FROM
 * 	order_info
 * GROUP BY
 * 	reserve_date
 * ORDER BY
 * 	reserve_date
 */
@Mapper
public interface OrderMapper extends BaseMapper<OrderInfo> {
    List<OrderCountVo> selectOrderCount(@Param("vo") OrderCountQueryVo orderCountQueryVo);

    List<OrderAmountVo> selectOrderAmount(@Param("vo") OrderCountQueryVo orderCountQueryVo);
}

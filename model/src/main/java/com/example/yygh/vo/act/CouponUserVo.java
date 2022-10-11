package com.example.yygh.vo.act;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
public class CouponUserVo {

    @ApiModelProperty(value = "订单id")
    private Long id;

    @ApiModelProperty(value = "购物券id")
    private Long couponId;

    /**
     * 用户id
     */
    @ApiModelProperty(value = "用户id")
    private Long userId;

}

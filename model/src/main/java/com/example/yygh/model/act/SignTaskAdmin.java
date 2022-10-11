package com.example.yygh.model.act;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.example.yygh.model.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 
 * @TableName coupon_admin
 */
@Data
@ApiModel(description = "SignTaskAdmin")
@TableName("sign_task_admin")
public class SignTaskAdmin extends BaseEntity {

    @ApiModelProperty(value = "优惠券id")
    @TableField("coupon_id")
    private Long couponId;

    /**
     * 库存
     */
    @ApiModelProperty(value = "库存")
    @TableField("inventory")
    private Long inventory;

    /**
     * 详情
     */
    @ApiModelProperty(value = "详情")
    @TableField("details")
    private String details;

    /**
     * 优惠券开始时间
     */
    @ApiModelProperty(value = "优惠券开始时间")
    @TableField("start_time")
    private Date startTime;

    /**
     * 优惠券结束时间
     */
    @ApiModelProperty(value = "优惠券结束时间")
    @TableField("end_time")
    private Date endTime;

}
package com.example.yygh.model.act;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.yygh.model.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 
 * @TableName sign_task
 */
@Data
@ApiModel(description = "SignTask")
@TableName("sign_task")
public class SignTask extends BaseEntity {


    /**
     * 优惠券id
     */
    @ApiModelProperty(value = "购物券id")
    @TableField("coupon_id")
    private Long couponId;

    /**
     * 用户id
     */
    @ApiModelProperty(value = "用户id")
    @TableField("user_id")
    private Long userId;

    /**
     * 最后签到时间
     */
    @ApiModelProperty(value = "最后签到时间")
    @TableField("last_time")
    private Date lastTime;

    /**
     * 签到次数
     */
    @ApiModelProperty(value = "签到次数")
    @TableField("number")
    private Integer number;

    /**
     * 收到优惠券次数
     */
    @ApiModelProperty(value = "收到优惠券次数")
    @TableField("receive_number")
    private Integer receiveNumber;

    /**
     * 收到优惠券最后时间
     */
    @ApiModelProperty(value = "收到优惠券最后时间")
    @TableField("receive_last_time")
    private Date receiveLastTime;


}
package com.example.yygh.model.act;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @TableName coupon_user
 */
@Data
@ApiModel(description = "CouponUser")
@TableName("coupon_user")
public class CouponUser implements Serializable {

    @ApiModelProperty(value = "id")
    @TableField("id")
    private Long id;
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

    /**
     * 0未使用,1已使用,2已过期
     */
    @ApiModelProperty(value = "标记")
    @TableField("state")
    private Integer state;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    @TableField("update_time")
    private Date updateTime;

    @ApiModelProperty(value = "逻辑删除(1:已删除，0:未删除)")
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;

    @ApiModelProperty(value = "其他参数")
    @TableField(exist = false)
    private Map<String, Object> param = new HashMap<>();
}
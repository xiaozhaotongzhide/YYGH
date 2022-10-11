package com.example.yygh.model.act;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.example.yygh.model.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @TableName coupon_admin
 */
@Data
@ApiModel(description = "CouponAdmin")
@TableName("coupon_admin")
public class CouponAdmin extends BaseEntity {


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


    @ApiModelProperty(value = "乐观锁")
    @TableField(value = "voucher_id", fill = FieldFill.INSERT)
    @Version
    private Integer voucherId;

}
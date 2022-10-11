package com.example.yygh.vo.act;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
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
public class SignTaskAdminVo {

    @ApiModelProperty(value = "id")
    private Long id;

    /**
     * 库存
     */
    @ApiModelProperty(value = "库存")
    private Long inventory;

    /**
     * 详情
     */
    @ApiModelProperty(value = "详情")
    private String details;

    /**
     * 优惠券开始时间
     */
    @ApiModelProperty(value = "优惠券开始时间")
    private Date startTime;

    /**
     * 优惠券结束时间
     */
    @ApiModelProperty(value = "优惠券结束时间")
    private Date endTime;

}
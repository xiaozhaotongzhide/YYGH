package com.example.yygh.vo.act;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class CouponAdminVo {

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
    @JsonFormat(pattern = "yyyy-MM-dd",timezone = "GMT+8")
    @ApiModelProperty(value = "优惠券开始时间")
    private Date startTime;

    /**
     * 优惠券结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd",timezone = "GMT+8")
    @ApiModelProperty(value = "优惠券结束时间")
    private Date endTime;

}

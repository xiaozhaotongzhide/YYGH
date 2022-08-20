package com.example.yygh.vo.sta;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class RamVo {
    @ApiModelProperty(value = "时间")
    private String time;

    @ApiModelProperty(value = "占用内存大小")
    private int ramSize;

    @ApiModelProperty(value = "服务名称")
    private String serviceName;
}

package com.example.yygh.vo.sta;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class CpuVo {

    @ApiModelProperty(value = "时间")
    private String time;

    @ApiModelProperty(value = "cpu占用率")
    private double cpuSize;

    @ApiModelProperty(value = "服务名称")
    private String serviceName;
}

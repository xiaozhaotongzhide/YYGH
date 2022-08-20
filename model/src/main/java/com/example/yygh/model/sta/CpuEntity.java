package com.example.yygh.model.sta;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.yygh.model.base.BaseEntity;
import lombok.Data;

import java.util.Date;

@TableName("cpu")
@Data
public class CpuEntity extends BaseEntity {

    @TableField("service_name")
    private String serviceName;

    @TableField("cpu_size")
    private Double cpuSize;

    @TableField("time")
    private Date time;

}

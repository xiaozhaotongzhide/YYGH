package com.example.yygh.model.sta;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.yygh.model.base.BaseEntity;
import lombok.Data;

import java.util.Date;

@TableName("ram")
@Data
public class RamEntity extends BaseEntity {


    @TableField("service_name")
    private String serviceName;

    @TableField("ram_size")
    private int ramSize;

    @TableField("time")
    private Date time;

}

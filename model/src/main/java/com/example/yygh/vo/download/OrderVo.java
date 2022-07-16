package com.example.yygh.vo.download;

import com.alibaba.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class OrderVo {

    @ExcelProperty(index = 0, value = "医院编号")
    private String hoscode;

    @ExcelProperty(index = 1, value = "医院名称")
    private String hosname;

    @ExcelProperty(index = 2, value = "科室编号")
    private String depcode;

    @ExcelProperty(index = 3, value = "科室名称")
    private String depname;

    @ExcelProperty(index = 4, value = "医生职称")
    private String title;

    @ExcelProperty(index = 5, value = "安排日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date reserveDate;

    @ExcelProperty(index = 6, value = "就诊人名称")
    private String patientName;

    @ExcelProperty(index = 7, value = "就诊人手机")
    private String patientPhone;

    @ExcelProperty(index = 8, value = "建议取号时间")
    private String fetchTime;

    @ExcelProperty(index = 9, value = "取号地点")
    private String fetchAddress;

    @ExcelProperty(index = 10, value = "医事服务费")
    private BigDecimal amount;

}

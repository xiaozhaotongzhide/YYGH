package com.example.yygh.vo.order;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel(description = "OrderCountVo")
public class OrderAmountVo {
	
	@ApiModelProperty(value = "安排日期")
	private String reserveDate;

	@ApiModelProperty(value = "挂号金额")
	private BigDecimal amount;

}


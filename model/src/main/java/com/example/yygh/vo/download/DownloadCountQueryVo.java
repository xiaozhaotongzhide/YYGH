package com.example.yygh.vo.download;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
public class DownloadCountQueryVo {
	
	@ApiModelProperty(value = "医院编号")
	private String hoscode;

	@ApiModelProperty(value = "医院名称")
	private String hosname;

	@ApiModelProperty(value = "安排日期")
	private String downloadDateBegin;
	private String downloadDateEnd;

}


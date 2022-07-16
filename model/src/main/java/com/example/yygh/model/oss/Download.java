package com.example.yygh.model.oss;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.yygh.model.base.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 
 * @TableName download
 */
@Data
@TableName("download")
public class Download extends BaseEntity {

    @TableField("hoscode")
    private String hoscode;

    @TableField("hosname")
    private String hosname;

    @TableField("download_date_begin")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date downloadDateBegin;

    @TableField("download_date_end")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date downloadDateEnd;

    @TableField("file_url")
    private String fileUrl;

    //0为还没有上传,1为上传成功,2为上传失败
    @TableField("status")
    private Integer status;

}
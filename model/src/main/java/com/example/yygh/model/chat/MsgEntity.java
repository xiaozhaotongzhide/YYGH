package com.example.yygh.model.chat;

import com.example.yygh.model.base.BaseMongoEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document("Msg")
public class MsgEntity extends BaseMongoEntity {

    @ApiModelProperty(value = "用户ID")
    private Long userId;

    @ApiModelProperty(value = "用户名")
    private String userName;
 
    @ApiModelProperty(value = "消息")
    private String msg;

}
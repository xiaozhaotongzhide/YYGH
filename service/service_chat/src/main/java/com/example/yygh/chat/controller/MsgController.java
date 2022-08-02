package com.example.yygh.chat.controller;


import com.example.yygh.chat.service.WebSocketService;
import com.example.yygh.common.result.Result;
import com.example.yygh.model.chat.MsgEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("admin/chat")
public class MsgController {

    @Autowired
    private WebSocketService webSocketService;

    @GetMapping("list/{page}/{limit}")
    public Result list(@PathVariable("page") Integer page,
                       @PathVariable("limit") Integer limit,
                       String userName) {
        Page<MsgEntity> list = webSocketService.selectPage(page, limit, userName);
        return Result.ok(list);
    }
}

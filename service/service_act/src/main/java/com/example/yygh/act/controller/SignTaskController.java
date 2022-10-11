package com.example.yygh.act.controller;

import com.example.yygh.act.service.SignTaskService;
import com.example.yygh.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("api/act")
public class SignTaskController {

    @Autowired
    private SignTaskService signTaskService;

    //签到功能
    @PostMapping("/signTask")
    public Result signTask(HttpServletRequest request){
        return signTaskService.signTask(request);
    }
}

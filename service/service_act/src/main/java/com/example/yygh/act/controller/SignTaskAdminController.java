package com.example.yygh.act.controller;

import com.example.yygh.act.service.SignTaskAdminService;
import com.example.yygh.common.result.Result;
import com.example.yygh.vo.act.SignTaskAdminVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("admin/act")
public class SignTaskAdminController {

    @Autowired
    private SignTaskAdminService signTaskAdminService;

    @PostMapping("/createTask")
    public Result createTask(@RequestBody SignTaskAdminVo signTaskAdminVo){
        Boolean task = signTaskAdminService.createTask(signTaskAdminVo);
        if(task){
            return Result.ok();
        }else {
            return Result.fail();
        }
    }

    @PostMapping("/updateTask")
    public Result updateTask(@RequestBody SignTaskAdminVo signTaskAdminVo){
        signTaskAdminService.updateTask(signTaskAdminVo);
        return Result.ok();
    }

    @GetMapping("/getList")
    public Result getList(){
        return Result.ok(signTaskAdminService.list());
    }
}

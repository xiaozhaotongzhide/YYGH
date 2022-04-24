package com.example.yygh.user.Controller;

import com.example.yygh.common.result.Result;
import com.example.yygh.user.Service.UserInfoService;
import com.example.yygh.vo.user.LoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserInfoApiController {
    @Autowired
    private UserInfoService userInfoService;
    //用户手机号登录接口

    //用手机号登录接口
    @PostMapping("login")
    public Result login(@RequestBody LoginVo loginVo){
        Map<String,Object> info = userInfoService.login(loginVo);
        return Result.ok(info);
    }
}

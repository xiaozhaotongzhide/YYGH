package com.example.yygh.user.api;

import com.example.yygh.common.result.Result;
import com.example.yygh.common.utils.AuthContextHolder;
import com.example.yygh.model.user.UserInfo;
import com.example.yygh.user.service.UserInfoService;
import com.example.yygh.vo.user.LoginVo;
import com.example.yygh.vo.user.UserAuthVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserInfoApiController {
    @Autowired
    private UserInfoService userInfoService;
    //用户手机号登录接口

    //用手机号登录接口
    @PostMapping("login")
    public Result login(@RequestBody LoginVo loginVo) {
        Map<String, Object> info = userInfoService.login(loginVo);
        return Result.ok(info);
    }

    //用户认证接口
    @PostMapping("auth/authAuth")
    public Result userAuth(@RequestBody UserAuthVo userAuthVo, HttpServletRequest request) {
        //在方法中传入两个参数,用户id和认证数据的ovid
        userInfoService.userAuth(AuthContextHolder.getUserId(request), userAuthVo);
        return Result.ok();
    }

    //获取用户id信息接口
    @GetMapping("auth/getUserInfo")
    public Result getUserInfo(HttpServletRequest request) {
        //在方法中传入两个参数,用户id和认证数据的ovid
        UserInfo byId = userInfoService.getById(AuthContextHolder.getUserId(request));
        return Result.ok(byId);
    }

    @Cacheable(value = "userInfo",keyGenerator = "keyGenerator")
    @GetMapping("getUserInfo/{id}")
    public UserInfo getUserInfo(@PathVariable("id") Long id){
        return userInfoService.getById(id);
    }
}

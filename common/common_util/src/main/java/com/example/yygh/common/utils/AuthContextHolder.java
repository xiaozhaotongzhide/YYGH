package com.example.yygh.common.utils;

import com.example.yygh.common.helper.JwtHelper;

import javax.servlet.http.HttpServletRequest;

//获取当前用户信息的工具类
public class AuthContextHolder {

    //获取当前用户的id
    public static Long getUserId(HttpServletRequest request){
        String token = request.getHeader("token");
        //从jwt中获取userid
        Long userId = JwtHelper.getUserId(token);
        return userId;
    }

    //获取当前用户名称
    public static String getUserName(HttpServletRequest request){
        String token = request.getHeader("token");
        //从jwt中获取userid
        String userName = JwtHelper.getUserName(token);
        return userName;
    }
}

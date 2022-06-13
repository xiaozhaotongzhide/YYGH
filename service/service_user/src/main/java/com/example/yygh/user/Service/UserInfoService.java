package com.example.yygh.user.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.yygh.model.user.UserInfo;
import com.example.yygh.vo.user.LoginVo;

import java.util.Map;

public interface UserInfoService extends IService<UserInfo> {


    Map<String, Object> login(LoginVo loginVo);

    UserInfo selectByOpenid(String openid);
}

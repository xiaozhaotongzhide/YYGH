package com.example.yygh.user.Service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.yygh.common.exception.YyghException;
import com.example.yygh.common.helper.JwtHelper;
import com.example.yygh.common.result.ResultCodeEnum;
import com.example.yygh.model.user.UserInfo;
import com.example.yygh.user.Service.UserInfoService;
import com.example.yygh.user.mapper.UserInfoMapper;
import com.example.yygh.vo.user.LoginVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {

    @Autowired
    public JavaMailSenderImpl javaMailSenderImpl;

    @Autowired
    public RedisTemplate<String, String> redisTemplate;


    //手机登录接口
    @Override
    public Map<String, Object> login(LoginVo loginVo) {
        //从loginVo获取输入的手机号和验证码
        String phone = loginVo.getPhone();
        String code = loginVo.getCode();
        //判断手机号和验证码是否未空
        if (StringUtils.isEmpty(phone) || StringUtils.isEmpty(code)) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        //判断手机验证码和输入的验证码是否一致
        //手机验证码,这里我们用邮箱代替
        String s = redisTemplate.opsForValue().get(phone);
        log.info("====验证码====", phone);
        if (!code.equals(s)) {
            throw new YyghException(ResultCodeEnum.CODE_ERROR);
        }
        //绑定手机号
        UserInfo userInfo = null;
        if (!StringUtils.isEmpty(loginVo.getOpenid())) {
            userInfo = this.selectByOpenid(loginVo.getOpenid());
            if (null != userInfo) {
                userInfo.setPhone(loginVo.getPhone());
                this.updateById(userInfo);
            } else {
                throw new YyghException(ResultCodeEnum.DATA_ERROR);
            }
        }
        if (userInfo == null) {
            log.info(phone);
            //判断是否是第一次登录
            QueryWrapper<UserInfo> qw = new QueryWrapper<>();
            qw.eq("phone", phone);
            //是第一次登录
            userInfo = baseMapper.selectOne(qw);
            if (ObjectUtils.isEmpty(userInfo)) {
                //说明是第一次使用这个手机号登录
                userInfo = new UserInfo();
                userInfo.setName("");
                userInfo.setPhone(phone);
                userInfo.setStatus(1);
                baseMapper.insert(userInfo);
            }
        }
        //如果该用户被禁用就返回这个异常
        if (userInfo.getStatus() == 0) {
            throw new YyghException(ResultCodeEnum.LOGIN_DISABLED_ERROR);
        }
        //不是第一次登录直接登录
        Map<String, Object> map = new HashMap<>();
        //返回登录信息
        //返回登录用户名
        //返回token信息
        String name = userInfo.getName();
        if (StringUtils.isEmpty(name)) {
            name = userInfo.getNickName();
        }
        if (StringUtils.isEmpty(name)) {
            name = userInfo.getPhone();
        }
        map.put("name", name);
        //token生成,JWT工具
        String token = JwtHelper.createToken(userInfo.getId(), name);
        map.put("token", token);
        return map;

    }

    @Override
    public UserInfo selectByOpenid(String openid) {
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("openid", openid);
        UserInfo userInfo = baseMapper.selectOne(queryWrapper);
        return userInfo;
    }
}

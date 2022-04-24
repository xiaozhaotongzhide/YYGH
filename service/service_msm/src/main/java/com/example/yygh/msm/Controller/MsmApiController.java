package com.example.yygh.msm.Controller;

import com.example.yygh.common.result.Result;
import com.example.yygh.msm.Service.MsmService;
import com.example.yygh.msm.Util.RandomUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/msm")
@Slf4j
public class MsmApiController {

    @Autowired
    private MsmService msmService;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    //发送手机号
    @GetMapping("send/{phone}")
    public Result sendCode(@PathVariable(value = "phone") String phone){
        log.info(phone);
        //从redis获取验证码,如果获取到返回ok
        /*String code = redisTemplate.opsForValue().get(phone);
        if (!StringUtils.isEmpty(code)){
            return Result.ok();
        }*/
        //如果从redis取不到
        //生成验证码,通过整合短信服务进行发送
        String code = RandomUtil.getFourBitRandom();

        msmService.send(phone,code);

        redisTemplate.opsForValue().set(phone, code,2, TimeUnit.MINUTES);
        return Result.ok();
    }
}

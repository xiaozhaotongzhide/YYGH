package com.example.yygh.user.api;

import com.alibaba.fastjson.JSONObject;
import com.example.yygh.common.helper.JwtHelper;
import com.example.yygh.common.result.Result;
import com.example.yygh.model.user.UserInfo;
import com.example.yygh.user.Service.UserInfoService;
import com.example.yygh.user.utils.ConstantWxPropertiesUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/api/ucenter/wx")
@Slf4j
public class weixinApiController {

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    UserInfoService userInfoService;

    //0.微信扫描后回调的方法
    @GetMapping("callback")
    public String callback(String code, String state) throws UnsupportedEncodingException {
        log.info("code为" + code);
        log.info("state为" + state);
        //获取code
        //拿着code和微信id和密钥,请求微信的固定地址
        String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid={appid}&secret={secret}&code={code}&grant_type=authorization_code";
        String accesstokenInfo = restTemplate.getForObject(url, String.class, ConstantWxPropertiesUtils.WX_OPEN_APP_ID, ConstantWxPropertiesUtils.WX_OPEN_APP_SECRET, code);
        log.info("accesstokenInfo为" + accesstokenInfo);
        //把accesstokenInfo封装成一个json对象
        JSONObject jsonObject = JSONObject.parseObject(accesstokenInfo);
        String accessToken = jsonObject.getString("access_token");
        String openid = jsonObject.getString("openid");
        UserInfo userInfo = userInfoService.selectByOpenid(openid);
        //判断现在是否已经有了这个微信用户
        if (userInfo == null) {
            //拿着accessToken,openid请求微信地址得到扫描人的信息
            String userInfoUrl = "https://api.weixin.qq.com/sns/userinfo?access_token={access_token}&openid={openid}";
            String userVoInfo = restTemplate.getForObject(userInfoUrl, String.class, accessToken, openid);
            log.info(userVoInfo);
            JSONObject userVoObject = JSONObject.parseObject(userVoInfo);
            //用户昵称
            String nickname = userVoObject.getString("nickname");
            //用于昵称是windows1252格式转换为utf-8
            nickname = new String(nickname.getBytes("windows-1256"),"UTF-8");
            log.info("nickname为" + nickname);
            //用户头像
            String headimgurl = jsonObject.getString("headimgurl");
            userInfo = new UserInfo();
            userInfo.setNickName(nickname);
            userInfo.setOpenid(openid);

            //返回name和token字符串
            userInfo.setStatus(1);
            userInfoService.save(userInfo);
        }

        Map<String, Object> map = new HashMap<>();
        String name = userInfo.getName();
        if (StringUtils.isEmpty(name)) {
            name = userInfo.getNickName();
        }
        if (StringUtils.isEmpty(name)) {
            name = userInfo.getPhone();
        }
        map.put("name", name);

        //判断是否有手机号,如果手机号为空,返回openid
        //如果手机号不为空返回openid值为空字符串
        //前端判断,如果openid不为空需要绑定手机号,如果openid为空不需要绑定手机号
        if (StringUtils.isEmpty(userInfo.getPhone())) {
            map.put("openid", userInfo.getOpenid());
        } else {
            map.put("openid", "");
        }

        //使用jwt生成token
        String token = JwtHelper.createToken(userInfo.getId(), name);
        map.put("token", token);
        return "redirect:" + ConstantWxPropertiesUtils.YYGH_BASE_URL + "/weixin/callback?token=" + map.get("token") + "&openid=" + map.get("openid") + "&name=" + URLEncoder.encode((String) map.get("name"), "utf-8");
    }

    //1.生成微信二维码
    //返回生成二维码需要的参数
    @GetMapping("getLoginParam")
    @ResponseBody
    public Result getQrConnect() {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("appid", ConstantWxPropertiesUtils.WX_OPEN_APP_ID);
            map.put("scope", "snsapi_login");
            String wxOpenRedirectUrl = ConstantWxPropertiesUtils.WX_OPEN_REDIRECT_URL;
            wxOpenRedirectUrl = URLEncoder.encode(wxOpenRedirectUrl, "utf-8");
            map.put("redirectUri", wxOpenRedirectUrl);
            map.put("state", System.currentTimeMillis() + "");
            return Result.ok(map);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
    //2.回调的方法得到,扫描人信息

}

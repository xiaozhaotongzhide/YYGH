package com.example.yygh.oss.confing;

import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class qiNiuYunConfig implements InitializingBean {

    @Value("${qiniuyun.access-key}")
    private String accessKey;

    @Value("${qiniuyun.secret-key}")
    private String secretKey;

    @Value("${qiniuyun.bucket}")
    private String Bucket;

    @Value("${qiniuyun.domain}")
    private String Domain;

    public static String ACCESS_KET;
    public static String SECRET_KET;
    public static String BUCKET;
    public static String DOMAIN;
    @Override
    public void afterPropertiesSet() throws Exception {
        ACCESS_KET = accessKey;
        SECRET_KET = secretKey;
        BUCKET = Bucket;
        DOMAIN = Domain;
    }
}

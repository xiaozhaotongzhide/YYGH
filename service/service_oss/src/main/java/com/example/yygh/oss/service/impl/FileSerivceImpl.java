package com.example.yygh.oss.service.impl;

import com.example.yygh.oss.confing.qiNiuYunConfig;
import com.example.yygh.oss.service.FileService;
import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

@Service
@Slf4j
public class FileSerivceImpl implements FileService {
    @Autowired
    private qiNiuYunConfig qiniuyun;

    @Override
    public String uplodad(MultipartFile file) {

        //构造一个带指定 Region 对象的配置类
        Configuration cfg = new Configuration(Region.region1());
        UploadManager uploadManager = new UploadManager(cfg);
        //默认不指定key的情况下，以文件内容的hash值作为文件名
        String key = null;
        try {
            Auth auth = Auth.create(qiniuyun.getAccessKey(), qiniuyun.getSecretKey());
            String upToken = auth.uploadToken(qiniuyun.getBucket());

            try {
                log.info(file.getOriginalFilename());
                String uuid = UUID.randomUUID().toString().replace("-", "");
                String fileName = uuid + file.getOriginalFilename();
                String timeUrl = new DateTime().toString("yyyy/MM/dd");
                fileName = timeUrl + "/" + fileName;
                Response response = uploadManager.put(file.getInputStream(), fileName, upToken, null, null);
                //解析上传成功的结果
                DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
                log.info("http://" + qiniuyun.getDomain() + "/" + putRet.key);
                return "http://" + qiniuyun.getDomain() + "/" + putRet.key;
            } catch (QiniuException ex) {
                Response r = ex.response;
                System.err.println(r.toString());
                try {
                    System.err.println(r.bodyString());
                } catch (QiniuException ex2) {
                    //ignore
                }
            }
        } catch (UnsupportedEncodingException ex) {
            //ignore
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return null;
    }
}

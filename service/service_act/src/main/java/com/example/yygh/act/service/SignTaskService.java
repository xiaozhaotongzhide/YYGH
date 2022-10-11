package com.example.yygh.act.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.yygh.common.result.Result;
import com.example.yygh.model.act.SignTask;

import javax.servlet.http.HttpServletRequest;

/**
* @author 86157
* @description 针对表【sign_task】的数据库操作Service
* @createDate 2022-10-01 12:48:44
*/
public interface SignTaskService extends IService<SignTask> {

    Result signTask(HttpServletRequest request);
}

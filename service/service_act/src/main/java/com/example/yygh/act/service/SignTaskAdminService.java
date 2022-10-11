package com.example.yygh.act.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.yygh.model.act.SignTaskAdmin;
import com.example.yygh.vo.act.SignTaskAdminVo;

/**
* @author 86157
* @description 针对表【sign_task】的数据库操作Service
* @createDate 2022-10-01 12:48:44
*/
public interface SignTaskAdminService extends IService<SignTaskAdmin> {

    Boolean createTask(SignTaskAdminVo signTaskAdminVo);

    Boolean updateTask(SignTaskAdminVo signTaskAdminVo);
}

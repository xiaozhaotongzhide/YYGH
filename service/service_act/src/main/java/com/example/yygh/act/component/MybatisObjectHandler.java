package com.example.yygh.act.component;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class MybatisObjectHandler implements MetaObjectHandler {
    //MetaObject元数据,代表当前操作的对象,
    @Override
    public void insertFill(MetaObject metaObject) {
        metaObject.setValue("voucherId", 0);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
    }
}


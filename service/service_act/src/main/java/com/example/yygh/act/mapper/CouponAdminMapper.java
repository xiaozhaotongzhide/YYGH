package com.example.yygh.act.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.yygh.model.act.CouponAdmin;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface CouponAdminMapper extends BaseMapper<CouponAdmin> {
}

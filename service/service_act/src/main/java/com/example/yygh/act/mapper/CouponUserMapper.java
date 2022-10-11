package com.example.yygh.act.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.yygh.model.act.CouponUser;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface CouponUserMapper extends BaseMapper<CouponUser> {
}

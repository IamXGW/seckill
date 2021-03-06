package com.iamxgw.secskill.dao;

import com.iamxgw.secskill.domain.SeckillUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SeckillUserDao {
    @Select("select * from seckill_user where phone = #{mobile}")
    public SeckillUser getById(@Param("mobile") long mobile);
}

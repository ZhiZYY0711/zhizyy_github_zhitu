package com.zhitu.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhitu.auth.entity.SysRefreshToken;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysRefreshTokenMapper extends BaseMapper<SysRefreshToken> {
}

package com.music.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.music.auth.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}

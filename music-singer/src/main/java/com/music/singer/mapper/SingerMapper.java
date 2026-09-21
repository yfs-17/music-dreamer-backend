package com.music.singer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.music.singer.entity.Singer;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SingerMapper extends BaseMapper<Singer> {
}

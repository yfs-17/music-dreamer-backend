package com.music.list.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.music.list.entity.Playlist;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PlaylistMapper extends BaseMapper<Playlist> {
}

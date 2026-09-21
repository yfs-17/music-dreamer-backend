package com.music.list.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.music.list.entity.PlaylistSong;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PlaylistSongMapper extends BaseMapper<PlaylistSong> {
}

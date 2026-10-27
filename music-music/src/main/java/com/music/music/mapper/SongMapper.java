package com.music.music.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.music.music.entity.Song;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface SongMapper extends BaseMapper<Song> {

    /** 看板用：所有未删除歌曲的累计播放量 */
    @Select("SELECT COALESCE(SUM(play_count), 0) FROM t_song WHERE is_deleted = 0")
    Long sumPlayCount();

    /** 按歌手统计上架作品数，用于同步 t_singer.song_count */
    @Select("SELECT singer_id, COUNT(*) AS cnt FROM t_song "
            + "WHERE status = 1 AND is_deleted = 0 GROUP BY singer_id")
    List<Map<String, Object>> countBySinger();
}

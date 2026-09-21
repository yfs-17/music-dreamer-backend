package com.music.list.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 歌单歌曲关联表
 */
@Data
@TableName("t_playlist_song")
public class PlaylistSong implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 歌单ID */
    private Long playlistId;

    /** 歌曲ID（逻辑关联音乐库） */
    private Long songId;

    /** 歌曲名（冗余） */
    private String songName;

    /** 歌手名（冗余） */
    private String singerName;

    /** 封面 URL（冗余） */
    private String coverUrl;

    /** 音频 URL（冗余） */
    private String audioUrl;

    /** 时长（冗余） */
    private Integer duration;

    /** 排序 */
    private Integer sort;

    private LocalDateTime createTime;
}

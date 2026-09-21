package com.music.music.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 歌曲表
 */
@Data
@TableName("t_song")
public class Song implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 歌曲ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 歌曲名 */
    private String songName;

    /** 歌手ID（逻辑关联歌手库） */
    private Long singerId;

    /** 歌手名（冗余字段） */
    private String singerName;

    /** 专辑ID */
    private Long albumId;

    /** 专辑名（冗余字段） */
    private String albumName;

    /** 封面 URL */
    private String coverUrl;

    /** 音频文件 URL */
    private String audioUrl;

    /** 歌词文件 URL（.lrc） */
    private String lyricUrl;

    /** 歌词解析后文本 */
    private String lyricText;

    /** 时长（秒） */
    private Integer duration;

    /** 标签，逗号分隔 */
    private String tags;

    /** 播放次数 */
    private Integer playCount;

    /** 收藏数（冗余） */
    private Integer likeCount;

    /** 评论数 */
    private Integer commentCount;

    /** 状态：1正常 0下架 */
    private Integer status;

    /** 逻辑删除 */
    @TableLogic
    private Integer isDeleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

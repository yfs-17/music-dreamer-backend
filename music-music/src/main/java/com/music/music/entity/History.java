package com.music.music.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 收听历史表
 */
@Data
@TableName("t_history")
public class History implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID（逻辑关联认证库） */
    private Long userId;

    /** 歌曲ID */
    private Long songId;

    /** 歌曲名（冗余字段） */
    private String songName;

    /** 歌手名（冗余字段） */
    private String singerName;

    /** 封面 URL（冗余字段） */
    private String coverUrl;

    /** 收听时间 */
    private LocalDateTime playTime;

    /** 本次收听时长（秒） */
    private Integer playDuration;
}

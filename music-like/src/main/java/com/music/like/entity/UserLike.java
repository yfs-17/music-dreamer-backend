package com.music.like.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 收藏表
 */
@Data
@TableName("t_like")
public class UserLike implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID（逻辑关联认证库） */
    private Long userId;

    /** 收藏目标ID */
    private Long targetId;

    /** 类型：1歌曲 2歌单 3歌手 */
    private Integer targetType;

    /** 目标名称（冗余，便于展示） */
    private String targetName;

    /** 目标封面（冗余，便于展示） */
    private String targetCover;

    private LocalDateTime createTime;
}

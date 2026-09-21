package com.music.list.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 歌单表
 */
@Data
@TableName("t_playlist")
public class Playlist implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 歌单ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 创建者用户ID（逻辑关联认证库） */
    private Long userId;

    /** 创建者昵称（冗余字段） */
    private String creatorNickname;

    /** 歌单标题 */
    private String title;

    /** 封面 URL */
    private String coverUrl;

    /** 描述 */
    private String description;

    /** 歌曲数（冗余） */
    private Integer songCount;

    /** 播放次数 */
    private Integer playCount;

    /** 收藏数（冗余） */
    private Integer likeCount;

    /** 是否公开：1公开 0私密 */
    private Integer isPublic;

    /** 逻辑删除 */
    @TableLogic
    private Integer isDeleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

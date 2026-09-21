package com.music.singer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 歌手表
 */
@Data
@TableName("t_singer")
public class Singer implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 歌手ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联用户ID（逻辑关联，不建外键） */
    private Long userId;

    /** 歌手名 */
    private String singerName;

    /** 头像 URL */
    private String avatarUrl;

    /** 简介 */
    private String intro;

    /** 地区 */
    private String region;

    /** 关注数 */
    private Integer followCount;

    /** 作品数（冗余字段） */
    private Integer songCount;

    /** 状态：1正常 0下架 */
    private Integer status;

    /** 逻辑删除 */
    @TableLogic
    private Integer isDeleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

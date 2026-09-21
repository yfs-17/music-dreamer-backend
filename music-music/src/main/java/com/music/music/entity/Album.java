package com.music.music.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 专辑表
 */
@Data
@TableName("t_album")
public class Album implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 专辑ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 专辑名 */
    private String albumName;

    /** 歌手ID（逻辑关联歌手库） */
    private Long singerId;

    /** 歌手名（冗余字段，避免跨库 JOIN） */
    private String singerName;

    /** 封面 URL */
    private String coverUrl;

    /** 简介 */
    private String intro;

    /** 发行日期 */
    private LocalDate releaseDate;

    /** 逻辑删除 */
    @TableLogic
    private Integer isDeleted;

    private LocalDateTime createTime;
}

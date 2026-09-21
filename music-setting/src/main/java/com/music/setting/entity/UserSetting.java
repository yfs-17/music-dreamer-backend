package com.music.setting.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户设置表
 */
@Data
@TableName("t_user_setting")
public class UserSetting implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 播放模式：0顺序 1单曲 2随机 */
    private Integer playMode;

    /** 音量 0-100 */
    private Integer volume;

    /** 歌词字号 */
    private Integer lyricFontSize;

    /** 主题 */
    private String theme;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

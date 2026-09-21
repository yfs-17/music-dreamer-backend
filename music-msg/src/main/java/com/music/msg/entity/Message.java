package com.music.msg.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息通知表
 */
@Data
@TableName("t_message")
public class Message implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 接收用户ID（逻辑关联认证库） */
    private Long userId;

    /** 消息标题 */
    private String title;

    /** 消息内容 */
    private String content;

    /** 类型：1系统通知 2歌曲审核 3互动消息 */
    private Integer type;

    /** 是否已读：0未读 1已读 */
    private Integer isRead;

    private LocalDateTime createTime;
}

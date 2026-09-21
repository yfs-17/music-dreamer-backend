package com.music.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 公告表
 */
@Data
@TableName("t_notice")
public class Notice implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 发布管理员ID（逻辑关联认证库） */
    private Long adminId;

    /** 管理员名（冗余字段） */
    private String adminName;

    /** 公告标题 */
    private String title;

    /** 公告内容 */
    private String content;

    /** 状态：1发布 0草稿 */
    private Integer status;

    /** 逻辑删除 */
    @TableLogic
    private Integer isDeleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

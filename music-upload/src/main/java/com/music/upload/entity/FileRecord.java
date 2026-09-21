package com.music.upload.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文件记录表
 */
@Data
@TableName("t_file")
public class FileRecord implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 原始文件名 */
    private String fileName;

    /** 访问 URL */
    private String fileUrl;

    /** 类型：1音频 2封面 3歌词 4头像 */
    private Integer fileType;

    /** 文件大小（字节） */
    private Long fileSize;

    /** 上传者ID（逻辑关联认证库） */
    private Long uploaderId;

    private LocalDateTime createTime;
}

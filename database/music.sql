-- ============================================================
-- Music Dreamer 悦享音乐平台 数据库表结构脚本（拆库版）
-- 版本：v1.0
-- 说明：每个微服务一个独立数据库，不含外键，不含初始化数据
-- 字符集：utf8mb4
-- ============================================================


-- ============================================================
-- 1. 认证服务数据库 music_auth_db
-- 微服务：Mod_Login
-- ============================================================
DROP DATABASE IF EXISTS `music_auth_db`;
CREATE DATABASE `music_auth_db`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;
USE `music_auth_db`;

DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user` (
                          `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
                          `username` VARCHAR(50) NOT NULL COMMENT '用户名',
                          `password` VARCHAR(100) NOT NULL COMMENT '密码（BCrypt加密）',
                          `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
                          `avatar_url` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
                          `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
                          `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
                          `gender` TINYINT DEFAULT 0 COMMENT '性别：0未知 1男 2女',
                          `birthday` DATE DEFAULT NULL COMMENT '生日',
                          `signature` VARCHAR(255) DEFAULT NULL COMMENT '个性签名',
                          `role` TINYINT NOT NULL DEFAULT 1 COMMENT '角色：1普通用户 2歌手 3管理员',
                          `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1正常 0禁用',
                          `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删 1已删',
                          `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                          PRIMARY KEY (`id`),
                          UNIQUE KEY `uk_username` (`username`),
                          KEY `idx_role` (`role`),
                          KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';


-- ============================================================
-- 2. 设置服务数据库 music_setting_db
-- 微服务：Mod_Setting
-- ============================================================
DROP DATABASE IF EXISTS `music_setting_db`;
CREATE DATABASE `music_setting_db`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;
USE `music_setting_db`;

DROP TABLE IF EXISTS `t_user_setting`;
CREATE TABLE `t_user_setting` (
                                  `id` BIGINT NOT NULL AUTO_INCREMENT,
                                  `user_id` BIGINT NOT NULL COMMENT '用户ID',
                                  `play_mode` TINYINT DEFAULT 0 COMMENT '播放模式：0顺序 1单曲 2随机',
                                  `volume` INT DEFAULT 50 COMMENT '音量 0-100',
                                  `lyric_font_size` INT DEFAULT 14 COMMENT '歌词字号',
                                  `theme` VARCHAR(20) DEFAULT 'light' COMMENT '主题',
                                  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                  PRIMARY KEY (`id`),
                                  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户设置表';


-- ============================================================
-- 3. 歌手服务数据库 music_singer_db
-- 微服务：Mod_Singer
-- ============================================================
DROP DATABASE IF EXISTS `music_singer_db`;
CREATE DATABASE `music_singer_db`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;
USE `music_singer_db`;

DROP TABLE IF EXISTS `t_singer`;
CREATE TABLE `t_singer` (
                            `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '歌手ID',
                            `user_id` BIGINT DEFAULT NULL COMMENT '关联用户ID（逻辑关联，不建外键）',
                            `singer_name` VARCHAR(100) NOT NULL COMMENT '歌手名',
                            `avatar_url` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
                            `intro` TEXT DEFAULT NULL COMMENT '简介',
                            `region` VARCHAR(50) DEFAULT NULL COMMENT '地区',
                            `follow_count` INT NOT NULL DEFAULT 0 COMMENT '关注数',
                            `song_count` INT NOT NULL DEFAULT 0 COMMENT '作品数（冗余字段）',
                            `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1正常 0下架',
                            `is_deleted` TINYINT NOT NULL DEFAULT 0,
                            `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            PRIMARY KEY (`id`),
                            KEY `idx_user_id` (`user_id`),
                            KEY `idx_singer_name` (`singer_name`),
                            KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='歌手表';


-- ============================================================
-- 4. 音乐服务数据库 music_music_db
-- 微服务：Mod_Music
-- 包含：专辑表、歌曲表、收听历史表
-- ============================================================
DROP DATABASE IF EXISTS `music_music_db`;
CREATE DATABASE `music_music_db`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;
USE `music_music_db`;

-- 4.1 专辑表
DROP TABLE IF EXISTS `t_album`;
CREATE TABLE `t_album` (
                           `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '专辑ID',
                           `album_name` VARCHAR(150) NOT NULL COMMENT '专辑名',
                           `singer_id` BIGINT NOT NULL COMMENT '歌手ID（逻辑关联歌手库）',
                           `singer_name` VARCHAR(100) DEFAULT NULL COMMENT '歌手名（冗余字段，避免跨库JOIN）',
                           `cover_url` VARCHAR(255) DEFAULT NULL COMMENT '封面URL',
                           `intro` TEXT DEFAULT NULL COMMENT '简介',
                           `release_date` DATE DEFAULT NULL COMMENT '发行日期',
                           `is_deleted` TINYINT NOT NULL DEFAULT 0,
                           `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           PRIMARY KEY (`id`),
                           KEY `idx_singer_id` (`singer_id`),
                           KEY `idx_album_name` (`album_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专辑表';

-- 4.2 歌曲表
DROP TABLE IF EXISTS `t_song`;
CREATE TABLE `t_song` (
                          `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '歌曲ID',
                          `song_name` VARCHAR(150) NOT NULL COMMENT '歌曲名',
                          `singer_id` BIGINT NOT NULL COMMENT '歌手ID（逻辑关联歌手库）',
                          `singer_name` VARCHAR(100) DEFAULT NULL COMMENT '歌手名（冗余字段，避免跨库JOIN）',
                          `album_id` BIGINT DEFAULT NULL COMMENT '专辑ID',
                          `album_name` VARCHAR(150) DEFAULT NULL COMMENT '专辑名（冗余字段）',
                          `cover_url` VARCHAR(255) DEFAULT NULL COMMENT '封面URL',
                          `audio_url` VARCHAR(255) NOT NULL COMMENT '音频文件URL',
                          `lyric_url` VARCHAR(255) DEFAULT NULL COMMENT '歌词文件URL（.lrc）',
                          `lyric_text` TEXT DEFAULT NULL COMMENT '歌词解析后文本（可选）',
                          `duration` INT DEFAULT 0 COMMENT '时长（秒）',
                          `tags` VARCHAR(255) DEFAULT NULL COMMENT '标签，逗号分隔',
                          `play_count` INT NOT NULL DEFAULT 0 COMMENT '播放次数',
                          `like_count` INT NOT NULL DEFAULT 0 COMMENT '收藏数（冗余）',
                          `comment_count` INT NOT NULL DEFAULT 0 COMMENT '评论数（可选）',
                          `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1正常 0下架',
                          `is_deleted` TINYINT NOT NULL DEFAULT 0,
                          `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          PRIMARY KEY (`id`),
                          KEY `idx_singer_id` (`singer_id`),
                          KEY `idx_album_id` (`album_id`),
                          KEY `idx_song_name` (`song_name`),
                          KEY `idx_play_count` (`play_count` DESC),
                          KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='歌曲表';

-- 4.3 收听历史表
DROP TABLE IF EXISTS `t_history`;
CREATE TABLE `t_history` (
                             `id` BIGINT NOT NULL AUTO_INCREMENT,
                             `user_id` BIGINT NOT NULL COMMENT '用户ID（逻辑关联认证库）',
                             `song_id` BIGINT NOT NULL COMMENT '歌曲ID',
                             `song_name` VARCHAR(150) DEFAULT NULL COMMENT '歌曲名（冗余字段）',
                             `singer_name` VARCHAR(100) DEFAULT NULL COMMENT '歌手名（冗余字段）',
                             `cover_url` VARCHAR(255) DEFAULT NULL COMMENT '封面URL（冗余字段）',
                             `play_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收听时间',
                             `play_duration` INT DEFAULT 0 COMMENT '本次收听时长（秒）',
                             PRIMARY KEY (`id`),
                             KEY `idx_user_time` (`user_id`, `play_time` DESC),
                             KEY `idx_song_id` (`song_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收听历史表';


-- ============================================================
-- 5. 歌单服务数据库 music_list_db
-- 微服务：Mod_List
-- 包含：歌单表、歌单歌曲关联表
-- ============================================================
DROP DATABASE IF EXISTS `music_list_db`;
CREATE DATABASE `music_list_db`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;
USE `music_list_db`;

-- 5.1 歌单表
DROP TABLE IF EXISTS `t_playlist`;
CREATE TABLE `t_playlist` (
                              `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '歌单ID',
                              `user_id` BIGINT NOT NULL COMMENT '创建者用户ID（逻辑关联认证库）',
                              `creator_nickname` VARCHAR(50) DEFAULT NULL COMMENT '创建者昵称（冗余字段）',
                              `title` VARCHAR(150) NOT NULL COMMENT '歌单标题',
                              `cover_url` VARCHAR(255) DEFAULT NULL COMMENT '封面URL',
                              `description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
                              `song_count` INT NOT NULL DEFAULT 0 COMMENT '歌曲数（冗余）',
                              `play_count` INT NOT NULL DEFAULT 0 COMMENT '播放次数',
                              `like_count` INT NOT NULL DEFAULT 0 COMMENT '收藏数（冗余）',
                              `is_public` TINYINT NOT NULL DEFAULT 1 COMMENT '是否公开：1公开 0私密',
                              `is_deleted` TINYINT NOT NULL DEFAULT 0,
                              `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                              PRIMARY KEY (`id`),
                              KEY `idx_user_id` (`user_id`),
                              KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='歌单表';

-- 5.2 歌单-歌曲关联表（冗余歌曲信息，避免跨库JOIN）
DROP TABLE IF EXISTS `t_playlist_song`;
CREATE TABLE `t_playlist_song` (
                                   `id` BIGINT NOT NULL AUTO_INCREMENT,
                                   `playlist_id` BIGINT NOT NULL COMMENT '歌单ID',
                                   `song_id` BIGINT NOT NULL COMMENT '歌曲ID（逻辑关联音乐库）',
                                   `song_name` VARCHAR(150) DEFAULT NULL COMMENT '歌曲名（冗余）',
                                   `singer_name` VARCHAR(100) DEFAULT NULL COMMENT '歌手名（冗余）',
                                   `cover_url` VARCHAR(255) DEFAULT NULL COMMENT '封面URL（冗余）',
                                   `audio_url` VARCHAR(255) DEFAULT NULL COMMENT '音频URL（冗余，可选）',
                                   `duration` INT DEFAULT 0 COMMENT '时长（冗余，可选）',
                                   `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
                                   `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   PRIMARY KEY (`id`),
                                   UNIQUE KEY `uk_playlist_song` (`playlist_id`, `song_id`),
                                   KEY `idx_song_id` (`song_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='歌单歌曲关联表';


-- ============================================================
-- 6. 收藏服务数据库 music_like_db
-- 微服务：Mod_Like
-- ============================================================
DROP DATABASE IF EXISTS `music_like_db`;
CREATE DATABASE `music_like_db`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;
USE `music_like_db`;

DROP TABLE IF EXISTS `t_like`;
CREATE TABLE `t_like` (
                          `id` BIGINT NOT NULL AUTO_INCREMENT,
                          `user_id` BIGINT NOT NULL COMMENT '用户ID（逻辑关联认证库）',
                          `target_id` BIGINT NOT NULL COMMENT '收藏目标ID',
                          `target_type` TINYINT NOT NULL COMMENT '类型：1歌曲 2歌单 3歌手',
                          `target_name` VARCHAR(150) DEFAULT NULL COMMENT '目标名称（冗余，便于展示）',
                          `target_cover` VARCHAR(255) DEFAULT NULL COMMENT '目标封面（冗余，便于展示）',
                          `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          PRIMARY KEY (`id`),
                          UNIQUE KEY `uk_user_target` (`user_id`, `target_id`, `target_type`),
                          KEY `idx_target` (`target_id`, `target_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';


-- ============================================================
-- 7. 消息服务数据库 music_msg_db
-- 微服务：Mod_Msg
-- ============================================================
DROP DATABASE IF EXISTS `music_msg_db`;
CREATE DATABASE `music_msg_db`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;
USE `music_msg_db`;

DROP TABLE IF EXISTS `t_message`;
CREATE TABLE `t_message` (
                             `id` BIGINT NOT NULL AUTO_INCREMENT,
                             `user_id` BIGINT NOT NULL COMMENT '接收用户ID（逻辑关联认证库）',
                             `title` VARCHAR(150) NOT NULL COMMENT '消息标题',
                             `content` TEXT DEFAULT NULL COMMENT '消息内容',
                             `type` TINYINT NOT NULL DEFAULT 1 COMMENT '类型：1系统通知 2歌曲审核 3互动消息',
                             `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读：0未读 1已读',
                             `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             PRIMARY KEY (`id`),
                             KEY `idx_user_read` (`user_id`, `is_read`),
                             KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知表';


-- ============================================================
-- 8. 管理服务数据库 music_admin_db
-- 微服务：Mod_Admin
-- ============================================================
DROP DATABASE IF EXISTS `music_admin_db`;
CREATE DATABASE `music_admin_db`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;
USE `music_admin_db`;

DROP TABLE IF EXISTS `t_notice`;
CREATE TABLE `t_notice` (
                            `id` BIGINT NOT NULL AUTO_INCREMENT,
                            `admin_id` BIGINT NOT NULL COMMENT '发布管理员ID（逻辑关联认证库）',
                            `admin_name` VARCHAR(50) DEFAULT NULL COMMENT '管理员名（冗余字段）',
                            `title` VARCHAR(150) NOT NULL COMMENT '公告标题',
                            `content` TEXT NOT NULL COMMENT '公告内容',
                            `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1发布 0草稿',
                            `is_deleted` TINYINT NOT NULL DEFAULT 0,
                            `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            PRIMARY KEY (`id`),
                            KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';


-- ============================================================
-- 9. 上传服务数据库 music_upload_db
-- 微服务：Mod_Upload
-- ============================================================
DROP DATABASE IF EXISTS `music_upload_db`;
CREATE DATABASE `music_upload_db`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;
USE `music_upload_db`;

DROP TABLE IF EXISTS `t_file`;
CREATE TABLE `t_file` (
                          `id` BIGINT NOT NULL AUTO_INCREMENT,
                          `file_name` VARCHAR(255) NOT NULL COMMENT '原始文件名',
                          `file_url` VARCHAR(255) NOT NULL COMMENT '访问URL',
                          `file_type` TINYINT NOT NULL COMMENT '类型：1音频 2封面 3歌词 4头像',
                          `file_size` BIGINT DEFAULT 0 COMMENT '文件大小（字节）',
                          `uploader_id` BIGINT DEFAULT NULL COMMENT '上传者ID（逻辑关联认证库）',
                          `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          PRIMARY KEY (`id`),
                          KEY `idx_uploader` (`uploader_id`),
                          KEY `idx_type` (`file_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件记录表';


-- ============================================================
-- 拆库完成
-- ============================================================
SELECT 'Music Dreamer 拆库版表结构创建完成！共 9 个数据库。' AS message;
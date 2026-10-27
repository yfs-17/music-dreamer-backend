music-dreamer-backend/
├── pom.xml                          # 父工程 POM，统一管理依赖版本
├── music-common/                    # 公共模块（工具类、常量、通用返回体）
│   ├── pom.xml
│   └── src/main/java/com/music/common/
│       ├── core/                    # 统一返回结果、分页封装
│       ├── exception/               # 全局异常定义与处理
│       ├── utils/                   # 工具类（JWT、日期、文件等）
│       └── constants/               # 常量枚举
│
├── music-gateway/                   # 网关服务（Gateway）
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/music/gateway/
│       │   ├── config/              # 路由配置、跨域配置
│       │   └── filter/              # 全局过滤器（鉴权、日志）
│       └── resources/
│           └── application.yml      # 网关路由、Nacos 配置
│
├── music-auth/                      # 认证服务（Mod_Login）
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/music/auth/
│       │   ├── controller/          # 登录、注册、Token 刷新接口
│       │   ├── service/             # 认证业务逻辑
│       │   ├── mapper/              # MyBatis Mapper
│       │   └── entity/              # 用户实体
│       └── resources/
│           ├── mapper/              # XML 映射文件
│           └── application.yml
│
├── music-music/                     # 音乐服务（Mod_Music）
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/music/music/
│       │   ├── controller/          # 歌曲、歌单、推荐接口
│       │   ├── service/
│       │   ├── mapper/
│       │   └── entity/
│       └── resources/
│           ├── mapper/
│           └── application.yml
│
├── music-singer/                    # 歌手服务（Mod_Singer）
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/music/singer/
│       │   ├── controller/          # 歌手管理、歌手作品接口
│       │   ├── service/
│       │   ├── mapper/
│       │   └── entity/
│       └── resources/
│           ├── mapper/
│           └── application.yml
│
├── music-admin/                     # 管理服务（Mod_Admin）
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/music/admin/
│       │   ├── controller/          # 用户管理、歌曲管理、公告管理
│       │   ├── service/
│       │   ├── mapper/
│       │   └── entity/
│       └── resources/
│           ├── mapper/
│           └── application.yml
│
├── music-setting/                   # 用户设置服务（Mod_Setting）
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/music/setting/
│       │   ├── controller/          # 用户信息设置、头像上传
│       │   ├── service/
│       │   ├── mapper/
│       │   └── entity/
│       └── resources/
│           ├── mapper/
│           └── application.yml
│
├── music-msg/                       # 消息服务（Mod_Msg）
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/music/msg/
│       │   ├── controller/          # 消息推送、通知接口
│       │   ├── service/
│       │   ├── mapper/
│       │   └── entity/
│       └── resources/
│           ├── mapper/
│           └── application.yml
│
├── music-upload/                    # 上传下载服务（Mod_Upload）
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/music/upload/
│       │   ├── controller/          # 音频、歌词、图片上传下载
│       │   ├── service/
│       │   └── utils/               # 文件存储工具（本地/OSS）
│       └── resources/
│           └── application.yml
│
├── music-list/                      # 歌单服务（Mod_List）
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/music/list/
│       │   ├── controller/          # 创建歌单、添加歌曲、歌单管理
│       │   ├── service/
│       │   ├── mapper/
│       │   └── entity/
│       └── resources/
│           ├── mapper/
│           └── application.yml
│
└── music-like/                      # 收藏服务（Mod_Like）
├── pom.xml
└── src/main/
├── java/com/music/like/
│   ├── controller/          # 收藏歌曲、收藏歌单
│   ├── service/
│   ├── mapper/
│   └── entity/
└── resources/
├── mapper/
└── application.yml
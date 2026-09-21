# Music Dreamer 后端

基于 Spring Cloud 的音乐播放器后端，共 11 个模块。

## 技术栈

| 组件 | 版本 |
|---|---|
| JDK | 21 |
| Spring Boot | 4.0.8 |
| Spring Cloud | 2025.1.3 |
| Spring Cloud Alibaba | 2025.1.0.0（含 nacos-client 3.1.1） |
| MyBatis-Plus | 3.5.17（`mybatis-plus-spring-boot4-starter`） |
| Nacos Server | 3.1.0（Docker） |
| 对象存储 | 阿里云 OSS |

## 模块与端口

| 模块 | 端口 | 说明 |
|---|---|---|
| music-common | — | 公共库：统一返回体 `R`、全局异常、JWT、常量。**不含启动类** |
| music-gateway | 9000 | 网关：路由转发、JWT 鉴权、访问日志（WebFlux） |
| music-auth | 8081 | 认证：登录、注册、Token 刷新 |
| music-music | 8082 | 音乐：歌曲、歌单、推荐 |
| music-singer | 8083 | 歌手：歌手管理、歌手作品 |
| music-admin | 8084 | 管理：用户、歌曲、公告管理 |
| music-setting | 8085 | 用户设置：个人信息、头像 |
| music-msg | 8086 | 消息：消息推送、通知 |
| music-upload | 8087 | 上传下载：音频、歌词、图片（阿里云 OSS） |
| music-list | 8088 | 歌单：创建歌单、添加歌曲 |
| music-like | 8089 | 收藏：收藏歌曲、收藏歌单 |

> 网关用 9000 是因为 **Nacos 3 的控制台占用了 8080**。

## 启动步骤

### 1. 启动基础设施

需要先启动 Docker Desktop，然后：

```bash
docker compose up -d
```

> 如果拉镜像卡在 `TLS handshake timeout`，是 Docker Hub 直连不通，需要配国内加速源
> （Docker Desktop → Settings → Docker Engine，加到 `registry-mirrors`）：
>
> ```json
> "registry-mirrors": [
>   "https://docker.m.daocloud.io",
>   "https://docker.1ms.run",
>   "https://docker.xuanyuan.me"
> ]
> ```
>
> 实测 `nacos/nacos-server` 这种非官方镜像走 `docker.m.daocloud.io` 会超时，用 `docker.1ms.run` 才能拉下来。

会拉起三个容器：

| 服务 | 地址 |
|---|---|
| Nacos 控制台 | http://localhost:8080 （账号密码见 `.env`，默认 nacos/nacos） |
| RabbitMQ 管理台 | http://localhost:15672 （music / music123） |
| Redis | 127.0.0.1:6379 （密码 redis123） |

> **Nacos 3 必须先初始化管理员账号**，否则服务注册会报
> `Code: 401, User not found! Please check user exist or password is right!` 并启动失败。
> 容器起来后执行一次（只需一次，数据存在 docker volume 里）：
>
> ```bash
> curl -X POST "http://localhost:8848/nacos/v3/auth/user/admin" -d "username=nacos&password=nacos"
> # 返回 {"code":0,"message":"success",...} 即成功
> ```
>
> 注意 Nacos 3.x 的鉴权模型和 2.x 不同：`nacos.core.auth.enabled=false`（server API 默认不鉴权）、
> `nacos.core.auth.admin.enabled=true`（admin API 鉴权开启），但客户端带着用户名密码仍会走登录流程，
> 所以账号必须存在。

### 2. 配置 MySQL

MySQL 用你本机已装的实例，不在 compose 里。项目是**拆库设计，每个服务连自己的库**，
默认账号 `root / 123456`，端口 3306：

| 模块 | 数据库 |
|---|---|
| music-auth | `music_auth_db` |
| music-setting | `music_setting_db` |
| music-singer | `music_singer_db` |
| music-music | `music_music_db` |
| music-list | `music_list_db` |
| music-like | `music_like_db` |
| music-msg | `music_msg_db` |
| music-admin | `music_admin_db` |
| music-upload | `music_upload_db` |

建表脚本见 `database/music.sql`（已执行，共 12 张表）。

改连接信息有两种方式（任选其一）：
- 直接改各模块 `src/main/resources/application.yml` 里 `spring.datasource.*` 的默认值
- 或在 IDEA 的 Run Configuration 里配环境变量 `MYSQL_HOST` / `MYSQL_PORT` / `MYSQL_USERNAME` / `MYSQL_PASSWORD`
  （库名各服务不同，不建议用统一环境变量覆盖）

### 3. 构建

```bash
mvn clean install -DskipTests
```

### 4. 启动服务

先起业务服务，最后起网关。IDEA 里逐个运行各模块的 `XxxApplication`，或命令行：

```bash
java -jar music-auth/target/music-auth-1.0.0-SNAPSHOT.jar
```

### 5. 验证

```bash
# 1) 直连服务（绕过网关）→ {"code":200,"msg":"操作成功","data":"pong from music-auth",...}
curl http://localhost:8081/auth/ping

# 2) 网关路由列表 → 应返回 9 条
#    返回 404 说明 management.endpoint.gateway.enabled 没开
#    返回 []   说明路由配置前缀写错了（见下方注意事项 1）
curl http://localhost:9000/actuator/gateway/routes

# 3) 走网关且不在白名单 → HTTP 401 {"code":401,"msg":"未登录或登录已过期"}
curl http://localhost:9000/api/auth/ping

# 4) 完整链路：带有效 token 走网关 → pong（证明路由确实转发到了下游服务）
TOKEN=$(... JwtUtils.createToken(1L, "test") 生成的值 ...)
curl -H "Authorization: Bearer $TOKEN" http://localhost:9000/api/auth/ping
```

> 第 4 步是关键：第 3 步的 401 是鉴权过滤器在**路由之前**拦下的，
> 只证明网关收到了请求，不能证明路由匹配成功。要确认转发链路，必须带有效 token 再试一次。

服务注册状况可以直接看网关的健康检查：

```bash
curl http://localhost:9000/actuator/health
# components.discoveryComposite.components.discoveryClient.details.services
# → ["music-auth","music-gateway", ...] 能列出已注册的服务
```

## 环境变量

见 `.env.example`。其中：

- 前四段供 `docker compose` 做变量替换（放在项目根目录即自动生效）
- 最后一段供 Spring Boot 服务读取，需在 IDE 的 Run Configuration 里配置

OSS 的 `OSS_ACCESS_KEY_ID` / `OSS_ACCESS_KEY_SECRET` 目前是空的，
拿到 AccessKey 后填上才能使用上传功能。

## 注意事项（踩过的坑）

1. **网关路由前缀必须是 `spring.cloud.gateway.server.webflux`**。
   Gateway 5.x 改过名，沿用旧的 `spring.cloud.gateway.*` **不会报错但路由静默失效**。
   唯一可靠的检查方式是访问 `/actuator/gateway/routes`，看是否返回 9 条路由。

2. **`music-common` 的 web 相关依赖全部是 `optional`**，不会传递给依赖它的模块。
   业务模块各自声明 `spring-boot-starter-webmvc`。不要给 common 加编译期传递的 servlet 依赖，
   否则 WebFlux 网关会因为 classpath 混入 `spring-webmvc` 而启动失败。

3. **每个业务模块都必须引 `mybatis-plus-jsqlparser`**。
   MyBatis-Plus 3.5.9 起 `PaginationInnerInterceptor` 被移到了这个包里，
   缺了它分页插件不仅不生效、而且**不报任何错**，查询会直接返回全量数据。

4. **Nacos 里目前没有任何配置**，服务靠本地 `application.yml` 启动。
   想改成配置中心集中管理时，在 Nacos 里新建 data-id 为 `music-<模块名>.yaml`
   的配置即可自动覆盖本地同名项，不需要改代码。

5. **Jackson 3 的注解包没有变**，仍是 `com.fasterxml.jackson.annotation`。
   实体类照常用 `@JsonProperty` / `@JsonFormat` / `@JsonIgnore`，不用改 import。
   但 `ObjectMapper` 换到了 `tools.jackson.databind`，注入时要注意。

6. **`spring-boot-maven-plugin` 只声明在父 pom 的 `<pluginManagement>`**。
   业务模块需要打成可执行 jar 的，各自显式声明一次。`music-common` 不声明，
   产物是普通 jar，否则其他模块无法依赖它。

7. **Nacos 必须先于业务服务启动**（实测确认）。
   Nacos 不可用时，服务注册会抛异常，而注册动作由 Tomcat 启动事件触发，
   所以整个应用会直接启动失败，异常链是：

   ```
   Failed to start bean 'webServerStartStop'
     ← NacosAutoServiceRegistration.start()
     ← NacosServiceRegistry.register()
     ← ErrCode:-401, ErrMsg:Client not connected, current status:STARTING
   ```

   如果只想单独调试某个服务、不想依赖 Nacos，可以加
   `spring.cloud.nacos.discovery.fail-fast=false`，或直接
   `spring.cloud.nacos.discovery.enabled=false` 关掉注册。

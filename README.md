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

再导入开发用演示数据（4 位歌手、3 张专辑、12 首歌曲、4 个歌单、4 条消息、3 条公告）。
音频与封面都已上传到阿里云 OSS（音频为 SoundHelix 示例曲的约 50 秒片段），
bucket 需开启公共读，导入后可直接播放：

```bash
mysql -uroot -p --default-character-set=utf8mb4 < database/seed.sql
```

> `seed.sql` 会先清空 `t_singer` / `t_album` / `t_song` / `t_history` / `t_playlist` /
> `t_playlist_song` / `t_message` / `t_notice`，仅用于开发环境。
> `--default-character-set=utf8mb4` 不能省，否则中文会乱码。

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

> 项目按 JDK 21 编译。若终端里 `java -version` 是 17，启动会报
> `UnsupportedClassVersionError`（class file version 65.0），请改用 JDK 21 的绝对路径，
> 例如 `"D:\jdk21\jdk-21.0.10\bin\java.exe" -jar ...`。

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

模板见 `.env.example`，真实配置写在项目根目录的 `.env`（**已被 `.gitignore` 忽略**，
不要把真实密钥写进 `.env.example`）。

- `docker compose` 会自动读取根目录 `.env` 做变量替换
- `music-upload` 通过 `spring.config.import` 自动加载根目录 `.env`，
  命令行从根目录启动、或 IDEA 以模块目录为工作目录启动都能读到
- 其他服务如果在 IDEA 里单独运行，仍需在 Run Configuration 里配置它们用到的变量

OSS 相关变量：

| 变量 | 说明 |
|---|---|
| `OSS_ENDPOINT` | 例 `oss-cn-beijing.aliyuncs.com`，必须与 bucket 所在地域一致 |
| `OSS_ACCESS_KEY_ID` / `OSS_ACCESS_KEY_SECRET` | AccessKey |
| `OSS_BUCKET_NAME` | bucket 名。**bucket 必须开启公共读**，否则返回的文件 URL 无法直接访问 |
| `OSS_PUBLIC_BASE_URL` | 可选，CDN / 自定义域名 |
| `UPLOAD_STORAGE` | `oss`（默认）或 `local`，本地调试没有外网时用 `local` |

> 如果上传报 `NoSuchBucket`，先确认 bucket 名与地域是否对应。可以用
> `java -cp <classpath> OssCheck.java` 之类的只读脚本列出账号下的 bucket，不要靠猜。

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

8. **网关白名单支持按 HTTP 方法限定**，格式为 `GET:/api/music/song/**`。
   公开只读接口必须用这种写法：若只写 `/api/music/song/**`，同前缀的写接口
   （例如上报收听记录）也会被一并放行。不带方法前缀的规则仍按任意方法匹配，用于登录、注册这类接口。
   OPTIONS 预检请求始终放行。

9. **分页统一用 `music-common` 的 `PageQuery` / `PageResult`**。
   `PageQuery` 的 setter 已做边界收敛（页码 ≥ 1，每页 1-100），
   `PageResult.of(ipage, Vo::from)` 可直接把实体分页转成 VO 分页，避免暴露 `isDeleted` 等内部字段。

10. **上传服务新增了 `t_file.object_key` 列**。使用旧库时需手动迁移一次：

    ```sql
    ALTER TABLE music_upload_db.t_file
      ADD COLUMN object_key VARCHAR(500) DEFAULT NULL COMMENT '存储对象键（删除对象时使用）'
      AFTER file_url;
    ```

    删除文件时要用 objectKey 去删 OSS 对象，从 URL 反推不可靠。
    本地存储模式（`UPLOAD_STORAGE=local`）的文件落在 `./uploads`，通过 `/upload/files/**` 静态映射访问。

11. **上传大小上限是 50MB**（`spring.servlet.multipart.max-file-size`），
    各类型另有自己的限制：音频 50MB、图片 5MB、歌词 1MB。
    超过容器上限会抛 `MaxUploadSizeExceededException`，已在全局异常处理里转成 400。

12. **JWT 现在包含 `role`**，网关注入 `X-User-Role`（1 普通用户 / 2 歌手 / 3 管理员），
    admin 服务据此判断管理员权限。改造前签发的老 token 没有 role，按普通用户处理，
    需要重新登录才能进入后台。

    把某个账号设为管理员：

    ```sql
    UPDATE music_auth_db.t_user SET role = 3 WHERE username = 'testlogin02';
    ```

13. **服务间内部接口统一走 `/internal/**`**，由 music-common 的 `InternalApiFilter`
    校验 `X-Internal-Token`。网关只路由 `/api/**`，所以外部无法经网关访问这些接口；
    这层校验防的是直接访问服务端口。默认开发密钥是 `music-internal-dev-token`，
    生产环境必须用 `MUSIC_INTERNAL_TOKEN` 覆盖，且所有服务配置成同一个值。

14. **admin 聚合服务使用 RestClient + LoadBalancer**（不是 OpenFeign），
    按 Nacos 服务名寻址（如 `http://music-auth/internal/user/count`）。
    单个下游不可用时，看板会把对应指标显示为 0，不会整体 500。

## 测试

```bash
# 全部单元测试
mvn test

# 只跑公共模块 / 某个服务
mvn -pl music-common test
mvn -pl music-auth,music-music,music-like -am test
```

> 首次运行需要联网下载 surefire 的 JUnit Platform provider，之后可离线执行。
> 纯单元测试使用 Mockito；MyBatis-Plus 的 lambda 条件需要手动
> `TableInfoHelper.initTableInfo(...)`，见各测试类的 `@BeforeAll`。

## 容器化部署

后端镜像、前端 Nginx 配置、应用 compose 与常见问题见 **[docs/DEPLOY.md](docs/DEPLOY.md)**。

```bash
mvn clean package -DskipTests
docker compose up -d
docker compose -f deploy/docker-compose.app.yml up -d --build
```

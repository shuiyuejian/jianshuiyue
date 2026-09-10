# JianPicture · 智能云图片库

> 一站式智能图片管理与协作平台：公共图库 + 私有 / 团队空间 + AI 扩图  + 空间数据分析，开箱即用。

![Java](https://img.shields.io/badge/Java-8-orange) ![SpringBoot](https://img.shields.io/badge/Spring_Boot-2.7.6-brightgreen) ![Vue](https://img.shields.io/badge/Vue-3.5-blue) ![MySQL](https://img.shields.io/badge/MySQL-8.0-blue) ![Redis](https://img.shields.io/badge/Redis-6+-red) ![SaToken](https://img.shields.io/badge/Sa--Token-1.39.0-purple)

- GitHub 仓库：<https://github.com/shuiyuejian/jianshuiyue>
- 作者：间水月 <https://github.com/shuiyuejian/jianhsuiye>

## 项目简介

JianPicture 是一个前后端分离的企业级图片管理平台，解决个人与团队在图片资产沉淀中的常见痛点：

- 图片散落在本地 / 聊天记录里，难以检索和复用
- 团队共用一套图片素材时没有权限管控，误删、覆盖时有发生
- 缺少图片数据洞察（用了多少容量、哪些分类最多、谁贡献最多）
- 想要 AI 能力（智能扩图、以图搜图、颜色搜图）但不想重复造轮子

为此，JianPicture 提供了**公共图库**（全站共享、审核后可见）、**个人 / 团队空间**（配额隔离、成员权限管控）、
**AI 扩图**（阿里云百炼）、**以图搜图 / 按主色调搜图**、**空间多维分析**（ECharts 可视化）以及完整的**管理后台**，
后端基于 Spring Boot + Sa-Token + Redis + ShardingSphere，前端基于 Vue 3 + Vite + Ant Design Vue。
![Uploading 1.png…]()


## 功能特性

### 用户模块
- 注册 / 登录 / 登出（Redis 集中式 Session，30 天有效期）
- 个人资料维护、头像上传、密码修改
- VIP 兑换码兑换（`UserExchangeVipPage`），兑换后提升空间配额
- 管理员对用户的增删改查与分页管理

### 公共图库
- 三种上传方式：本地文件上传、URL 转存、批量抓取（关键词 + 数量，一键入库）
- 图片编辑：改名、简介、分类、标签；批量改名 / 打标签 / 改分类
- 图片审核：管理员审核（通过 / 拒绝），保障公共图库内容质量
- 标签 / 分类预设字典，前端下拉快速选择
- 高性能分页列表：Caffeine 本地缓存（5 分钟）+ Redis 分布式缓存多级加速

### 智能搜索
- 关键词 / 标签 / 分类组合搜索
- **以图搜图**：上传图片，基于 Bing 图片搜索能力返回相似图片（`ImageSearchApiFacade` + Jsoup）
- **按颜色搜图**：上传时自动提取图片主色调（`picColor`），按颜色相似度检索
- 公共图库与空间内搜索隔离，互不干扰

### 空间（私有 / 团队）
- 三档配额：普通版（100 张 / 100MB）、专业版（1000 张 / 1000MB）、旗舰版（10000 张 / 10000MB）
- 两种类型：私有空间（仅自己可见）、团队空间（多成员协作）
- 我的空间、空间详情、空间容量用量实时统计与校验（上传 / 删除自动维护 `totalSize` / `totalCount`）

### 团队协作与权限
- 空间成员邀请 / 移除 / 角色编辑，我加入的团队空间列表
- Sa-Token 权限模型：`picture:view` / `picture:upload` / `picture:edit` / `picture:delete` / `spaceUser:manage`
- 注解式鉴权：`@SaSpaceCheckPermission` + `@AuthCheck`（管理员），AOP 统一拦截
- 图片协同编辑：WebSocket 实时通道 + Disruptor 高性能无锁队列， 多人同时编辑同一图片可实时同步

### AI 扩图
- 接入阿里云 AI（百炼图像扩展）：创建扩图任务 → 轮询任务状态 → 回填结果图
- 接口：`POST /picture/out_painting/create_task`、`GET /picture/out_painting/get_task`

### 空间分析（ECharts 可视化）
- 用量分析、分类占比、标签统计（含词云）、空间大小趋势、用户贡献排行等多维度图表
- 接口前缀：`/space/analyze`（`usage` / `category` / `tag` / `size` / `user` / `rank`）

### 管理后台
- 用户管理、图片管理、空间管理、空间成员管理四个 Admin 页面
- 分页 + 条件检索，管理员可直接上下架 / 删除违规内容
## 技术栈

### 后端（`jian-picture/jian-picture-backend`）

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 1.8 | 语言版本 |
| Spring Boot | 2.7.6 | Web / AOP / WebSocket / Validation |
| MyBatis-Plus | 3.5.9 | ORM、分页插件、逻辑删除（`isDelete`） |
| MySQL | 8.0 | 主业务库 `jian_picture` |
| Redis + Spring Session | 6+ | Session 集中存储、图片列表缓存 |
| Caffeine | 3.1.8 | 本地多级缓存 |
| Sa-Token | 1.39.0 | 登录鉴权 + 空间权限（Redis-Jackson 持久化） |
| ShardingSphere-JDBC | 5.2.0 | `picture` 表按 `spaceId` 动态分表 |
| 腾讯云 COS | 5.6.227 | 对象存储（`CosManager` / `FileManager`） |
| Knife4j | 4.4.0 | 接口文档（基于 OpenAPI2） |
| Disruptor | 3.4.2 | 协同编辑消息无锁队列 |
| Jsoup | 1.22.2 | Bing 图片抓取解析 |
| Hutool | 5.8.38 | 工具库 |
| Lombok | - | 样板代码简化 |

### 前端（`jian-picture/jian-picture-frontend`）

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.5 | 渐进式框架（Composition API） |
| Vite | 6.0 | 构建工具，`npm run dev` 启动 |
| TypeScript | 5.6 | 类型约束 |
| Ant Design Vue | 4.2 | UI 组件库 |
| Pinia | 2.2 | 状态管理（含登录用户 Store） |
| Vue Router | 4.4 | 路由（见下方路由表） |
| ECharts + vue-echarts + echarts-wordcloud | 5.5 | 空间分析图表与词云 |
| vue-cropper | 1.1 | 图片裁剪 |
| vue3-colorpicker | 2.3 | 取色器（配合主色调搜索） |
| Axios | 1.7 | 请求库（`withCredentials` 携带 Cookie） |
| file-saver | 2.0 | 图片下载 |

## 目录结构

```text
jian-picture/                        # Git 仓库根目录
├── README.md                        # 本文档
├── jian-picture/                    # 项目主体
│   ├── jian-picture-backend/        # Spring Boot 后端
│   │   ├── sql/
│   │   │   └── create_table_sql.sql # 建表 SQL（含 picture / space / space_user / user）
│   │   └── src/main/java/com/jian/jianpicturebackend/
│   │       ├── controller/          # User / Picture / Space / SpaceUser / SpaceAnalyze / File / Main
│   │       ├── service/             # 业务层（含 SpaceAnalyzeService 空间分析）
│   │       ├── manager/
│   │       │   ├── auth/            # Sa-Token 权限：StpKit / StpInterfaceImpl / SpaceUserAuthManager
│   │       │   ├── upload/          # 上传模板方法（文件 / URL）
│   │       │   ├── sharding/        # PictureShardingAlgorithm 自定义分片算法
│   │       │   └── websocket/       # 协同编辑 WebSocket + Disruptor 队列
│   │       ├── api/
│   │       │   ├── aliyunai/        # 阿里云 AI 扩图接口封装
│   │       │   └── imagesearch/     # 以图搜图门面 + Bing 抓取
│   │       ├── model/               # entity / dto / vo / enums（含 SpaceLevelEnum）
│   │       └── config/              # CORS / COS / Redis-Session / MyBatis-Plus / Knife4j
│   └── jian-picture-frontend/       # Vue 3 前端
│       └── src/
│           ├── pages/               # Home / AddPicture / PictureDetail / Space* / UserProfile ...
│           ├── pages/admin/         # UserManage / PictureManage / SpaceManage / SpaceUserManage
│           ├── router/index.ts      # 路由定义
│           ├── stores/useLoginUserStore.ts  # 登录态
│           ├── request.ts           # Axios 实例（baseURL + 401 跳转登录）
│           └── utils/pictureEditWebSocket.ts# 协同编辑 WebSocket 客户端
└── *.png                            # 需求 / 面试相关的参考图片（非项目代码）
```

## 快速开始

### 环境要求

| 依赖 | 版本 | 备注 |
|------|------|------|
| JDK | 8+ | 后端运行环境 |
| Maven | 3.6+ | 后端构建 |
| Node.js | 22+ | 前端构建（仓库 `devDependencies` 含 `@types/node@22`） |
| MySQL | 8.0 | 业务数据库 |
| Redis | 6+ | Session 与缓存 |

### 1. 克隆项目

```bash
git clone https://github.com/shuiyuejian/jianshuiyue.git
cd jianshuiyue/jian-picture
```

### 2. 初始化数据库

```sql
CREATE DATABASE IF NOT EXISTS jian_picture
  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

然后按顺序执行 `jian-picture-backend/sql/create_table_sql.sql` 中的建表语句
（包含 `user` / `picture` / `space` / `space_user` 四张核心表及索引，
以及 `picture.spaceId`、`picture.picColor`、`space.spaceType` 增量字段）。

> 首次启动建议先清空 Redis，避免旧 Session / 缓存干扰。

### 3. 启动后端

1. 修改 `src/main/resources/application.yml`：数据库账号密码、Redis 地址、
   COS 及阿里云 AI 相关配置（详见下方「配置说明」）。
2. 启动主类：`com.jian.jianpicturebackend.JianPictureBackendApplication`
   或命令行：

```bash
cd jian-picture/jian-picture-backend
mvn spring-boot:run
```

3. 启动成功后：
   - 接口前缀：`http://localhost:8090/api`
   - 接口文档（Knife4j）：`http://localhost:8090/api/doc.html`
   - 健康检查：`GET http://localhost:8090/api/health`

### 4. 启动前端

```bash
cd jian-picture/jian-picture-frontend
npm install
npm run dev
```

- 开发地址默认为 `http://localhost:5173`
- 联调地址在 `src/request.ts` 中配置：`DEV_BASE_URL = "http://localhost:8090"`
 （生产环境可取消 `vite.config.ts` 中的 `/api` 代理注释，或修改 `PROD_BASE_URL`）
- 登录态依赖 Cookie，Axios 已开启 `withCredentials`，前后端联调时不要禁用 Cookie
## 配置说明

### 后端 `application.yml` 关键项

| 配置 | 默认值 | 说明 |
|------|--------|------|
| `server.port` / `servlet.context-path` | `8090` / `/api` | 后端端口与接口前缀 |
| `spring.datasource.*` | `localhost:3306/jian_picture`，`root/1234` | 按本地 MySQL 修改账号密码 |
| `spring.redis.*` | `127.0.0.1:6379` | Redis 地址；`spring.session.store-type: redis` 为 Session 集中存储，**多实例部署必填** |
| `spring.servlet.multipart` | 单文件 10MB / 单次请求 20MB | 超限需同步调整前端上传提示 |
| `shardingsphere.rules.sharding` | `picture` 按 `spaceId` 分表 | 自定义算法 `PictureShardingAlgorithm`；公共图库（`spaceId` 为空）路由默认表 |
| `knife4j.enable` | `true` | 生产环境建议关闭或鉴权后开放 |

以下敏感配置不要提交到公开仓库，建议用环境变量或 `application-local.yml` 覆盖：

- **腾讯云 COS**：`CosClientConfig`（SecretId / SecretKey / Region / Bucket）+ `CosManager` / `FileManager` 封装上传下载；头像上传走 `/file/upload/avatar`
- **阿里云 AI 扩图**：`AliYunAiApi` 所需的 API Key（百炼图像扩展），无 Key 时扩图接口会失败，不影响其他功能
- **Cookie / Session**：`server.servlet.session.cookie.max-age` 与 `spring.session.timeout` 均为 30 天；前后端不同域部署时需配合 `CorsConfig` 设置允许跨域携带 Cookie

### 前端联调配置

| 文件 | 说明 |
|------|------|
| `src/request.ts` | `DEV_BASE_URL` 开发联调地址；响应拦截器统一处理 `code === 40100`（未登录自动跳登录页并携带 `redirect`） |
| `src/stores/useLoginUserStore.ts` | 登录用户信息全局状态，刷新后通过 `/user/get/login` 恢复 |
| `openapi.config.js` + `npm run openapi` | 可按后端 Knife4j/OpenAPI 定义自动生成前端接口代码（按需使用） |

## 核心功能详解

### 公共图库与多级缓存

- 上传链路统一收口到 `PictureService.uploadPicture`：文件校验 → COS 存储 → 解析宽高 / 大小 / 主色调 → 落库并回写空间用量。
- URL 转存（`/picture/upload/url`）与批量抓取（`/picture/upload/batch`，关键词 + 抓取数量）复用同一入库流程，抓取源为 Bing（Jsoup 解析）。
- 列表接口 `/picture/list/page/vo/cache` 采用 **Caffeine（进程内，5 分钟）+ Redis（分布式）** 两级缓存，缓存 Key 包含完整查询条件 MD5，写操作后失效对应 Key。
- 管理员审核流：`reviewStatus`（待审 / 通过 / 拒绝），仅通过的图片进入公共图库。

### 图片搜索：从关键词到以图搜图

| 搜索方式 | 接口 | 说明 |
|----------|------|------|
| 条件搜索 | `POST /picture/list/page/vo` | 名称 / 简介 / 分类 / 标签 / 用户 / 空间 / 审核状态组合 |
| 以图搜图 | `POST /picture/search/picture` | 上传图片，返回相似图片列表（含来源页 URL） |
| 按颜色搜图 | `POST /picture/search/color` | 按入库时提取的 `picColor` 主色调相似度排序 |

### 空间配额模型

| 级别 | value | 图片数量上限 | 总容量上限 |
|------|-------|--------------|------------|
| 普通版 | 0 | 100 张 | 100MB |
| 专业版 | 1 | 1000 张 | 1000MB |
| 旗舰版 | 2 | 10000 张 | 10000MB |

- 配额定义在 `SpaceLevelEnum`，创建 / 升级空间时写入 `maxSize` / `maxCount` 快照。
- 上传 / 删除图片时在同一事务内维护 `space.totalSize` / `totalCount`，超限直接拒绝并提示。
- `spaceType`：`0-私有`（仅创建者可见）、`1-团队`（成员按权限协作）。

### 团队协作与权限实现

- 成员关系落 `space_user` 表（`spaceId + userId` 唯一 + `spaceRole` 角色）。
- 权限常量：`SpaceUserPermissionConstant`（`spaceUser:manage`、`picture:view/upload/edit/delete`）。
- 鉴权链路：`@SaSpaceCheckPermission` → `StpKit` → `SpaceUserAuthManager` 按「空间类型 + 成员角色」解析权限，
  管理员通道走 `@AuthCheck(mustRole = ADMIN)` + `AuthInterceptor`。
- 图片协同编辑：前端 `pictureEditWebSocket.ts` 建立 WebSocket 连接，
  后端 `manager/websocket` 接收编辑动作，经 Disruptor 队列广播给同空间在线成员。

### AI 扩图（阿里云）

1. 前端在图片详情页发起扩图，调用 `POST /picture/out_painting/create_task`（携带图片 id 与扩图参数）。
2. 后端透传阿里云百炼 API 创建任务，返回 `taskId`。
3. 前端轮询 `GET /picture/out_painting/get_task?taskId=...`，任务成功后展示结果图并可一键转存为新图片。

### 空间分析

- 后端 `SpaceAnalyzeService` 按空间聚合：总量 / 已用配额、分类分布、标签 Top（含词云数据）、
  图片大小区间分布、成员贡献排行，对应 `/space/analyze/{usage,category,tag,size,user,rank}` 六个接口。
- 前端 `SpaceAnalyzePage.vue` 使用 ECharts（柱状 / 饼图 / 词云）渲染，支持切换「全空间 / 指定空间」口径。
## 页面路由表

| 路由 | 页面 | 说明 |
|------|------|------|
| `/` | `HomePage` | 公共图库首页（搜索 + 缓存分页列表） |
| `/user/login`、`/user/register` | 登录 / 注册 | 登录后按 `redirect` 回跳 |
| `/picture/:id` | `PictureDetailPage` | 图片详情：预览、编辑、AI 扩图、协同编辑、以图搜图入口 |
| `/add_picture`、`/add_picture/batch` | 创建 / 批量创建图片 | 文件上传、URL 转存、关键词批量抓取 |
| `/search_picture` | `SearchPicturePage` | 以图搜图 / 按颜色搜图 |
| `/my_space`、`/add_space`、`/space/:id` | 我的空间 / 创建空间 / 空间详情 | 空间图片管理与用量展示 |
| `/space_analyze` | `SpaceAnalyzePage` | 空间多维分析图表 |
| `/spaceUserManage/:id` | `SpaceUserManagePage` | 空间成员管理（团队空间） |
| `/user/profile`（个人信息页路由见 `router/index.ts`） | `UserProfilePage` | 资料、头像、密码修改 |
| `/user/exchangeVip`（兑换页路由见 `router/index.ts`） | `UserExchangeVipPage` | VIP 兑换码兑换 |
| `/admin/userManage` | `UserManagePage` | 用户管理（管理员） |
| `/admin/pictureManage` | `PictureManagePage` | 图片管理（管理员，含审核） |
| `/admin/spaceManage` | `SpaceManagePage` | 空间管理（管理员） |

## 接口一览（前缀 `/api`）

| Controller | 前缀 | 主要接口 |
|------------|------|----------|
| `MainController` | `/` | `GET /health` 健康检查 |
| `UserController` | `/user` | `register` / `login` / `logout` / `get/login` / `update/my` / `update/password`，管理员 `add/delete/update/get/list/page/vo` |
| `PictureController` | `/picture` | `upload` / `upload/url` / `upload/batch` / `delete` / `update` / `edit` / `edit/batch` / `list/page(/vo)` / `list/page/vo/cache` / `get(/vo)` / `tag_category` / `review` / `search/picture` / `search/color` / `out_painting/create_task` / `out_painting/get_task` |
| `SpaceController` | `/space` | `add` / `delete` / `update` / `get(/vo)` / `list/page/vo` / `edit` / `list/level` |
| `SpaceUserController` | `/spaceUser` | `add` / `delete` / `get` / `list` / `edit` / `list/my`（我加入的团队空间） |
| `SpaceAnalyzeController` | `/space/analyze` | `usage` / `category` / `tag` / `size` / `user` / `rank` |
| `FileController` | `/file` | `upload/avatar` 头像上传、`test/upload`、`test/download` 调试接口 |

统一返回体：`BaseResponse<T>`（`code/message/data`），异常码定义在 `ErrorCode`，
业务异常使用 `ThrowUtils.throwIf` + `BusinessException` 抛出，由 `GlobalExceptionHandler` 统一收口。

## 数据库设计

核心四表（完整 DDL 见 `sql/create_table_sql.sql`，字符集 `utf8mb4_unicode_ci`）：

- **`user`**：账号 / 加密密码 / 昵称 / 头像 / 简介 / 角色（`user/admin`）/ 会员标识 / 逻辑删除
- **`picture`**：名称 / 简介 / 分类 / 标签（JSON）/ 图片地址 / 缩略图 / 宽高 / 大小 / 主色调（`picColor`）/
  所属空间（`spaceId`，为空表示公共图库）/ 上传人 / 审核状态 / 索引（`idx_spaceId` 等）
- **`space`**：名称 / 级别（0 普通 / 1 专业 / 2 旗舰）/ 类型（0 私有 / 1 团队）/ 配额快照（`maxSize/maxCount`）/
  已用（`totalSize/totalCount`）/ 创建人 / 索引（`idx_userId/idx_spaceName/idx_spaceLevel`）
- **`space_user`**：空间 id / 用户 id / 空间角色 / 创建人，`spaceId + userId` 唯一约束

约定：所有表使用 `isDelete` 逻辑删除（`1` 已删 / `0` 未删），时间字段统一 `createTime/editTime/updateTime`。

## 性能与可扩展性

- **多级缓存**：读多写少的图片列表走 Caffeine + Redis 两级缓存，5 分钟过期 + 写失效。
- **动态分表**：`picture` 表按 `spaceId` 做 ShardingSphere 动态分表，单空间图片量增长时可水平拆分；
  本地开发如不想启用分片，可将数据源切回直连（注意同步修改 `application.yml`）。
- **对象存储**：图片二进制全部走腾讯云 COS，后端只存 URL，前端直链访问，扩容零改造成本。
- **协同编辑削峰**：WebSocket 消息经 Disruptor 无锁队列异步广播，避免阻塞业务线程。
- **分页与索引**：全列表接口强制分页（MyBatis-Plus `Page`），高频查询字段均建索引。

## 部署上线

### 后端

```bash
cd jian-picture/jian-picture-backend
mvn clean package -DskipTests
java -jar target/jian-picture-backend-0.0.1-SNAPSHOT.jar
```

- 生产环境关闭 Knife4j（`knife4j.enable: false`），JVM 建议 `-Xms512m -Xmx1g` 起步。
- 多实例部署时 Redis 必选（Session 共享），前面加 Nginx 做轮询即可（WebSocket 需开启 `ip_hash` 或 Sticky Session）。

### 前端

```bash
cd jian-picture/jian-picture-frontend
npm run build   # 产物在 dist/
```

将 `dist/` 部署到 Nginx，并将 `/api` 反向代理到后端 `8090` 端口；或保持前后端同域，
把 `src/request.ts` 的 `baseURL` 改为同域 `/api` 前缀，避免跨域 Cookie 问题。

## 常见问题 FAQ

1. **登录后刷新就掉线 / 接口 401？**
   检查三点：后端 Redis 是否启动（Session 存 Redis）、前端是否携带 Cookie（`withCredentials: true`，
   浏览器未禁用第三方 Cookie）、前后端是否同站（不同端口算跨站，开发期建议前端配 `/api` 代理）。
2. **图片上传失败？**
   先看是否超过 10MB 限制，再检查 COS 配置（SecretId / Key / Region / Bucket）与 Bucket 公有读权限。
3. **AI 扩图一直失败？**
   需要有效的阿里云百炼 API Key；无 Key 不影响图库其他功能。
4. **批量抓取没有结果？**
   抓取依赖外网访问 Bing，服务器需能访问公网；关键词尽量使用英文以提高召回率。
5. **分表报错 / 本地只想单表跑？**
   ShardingSphere 开启后 SQL 会经分片路由（`sql-show: true` 可打印实际 SQL 排查）；
   数据量小可暂时去掉 `shardingsphere-jdbc` 相关配置，用直连数据源启动。

## 作者与致谢

- 作者：**间水月** — <https://github.com/shuiyuejian/jianhsuiye>
- 本项目仅供学习交流，欢迎 Star / Fork / 提 Issue 和 PR，一起把云图库做得更好！

如果这个项目对你有帮助，欢迎点一个 Star，你的支持是持续更新的最大动力。

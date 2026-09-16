# 家教直通车

面向家教信息撮合的信息发布与审核平台。家长发布家教需求，教员发布家教简历，管理员对两类信息做审核，审核通过后对所有人可见。

服务端渲染的 Spring Boot 应用，页面用 Thymeleaf + Bootstrap 编写。

## 技术栈

| 层次 | 技术 |
| --- | --- |
| 框架 | Spring Boot 3.5.6（Java 17） |
| 安全 | Spring Security 6，基于 Session 的表单登录与角色鉴权 |
| 持久层 | MyBatis-Plus 3.5.14，SQL 写在 Mapper 接口注解里 |
| 数据库 | MySQL 8.0 |
| 模板引擎 | Thymeleaf |
| 前端 | Bootstrap 5.3.0（CDN）、原生 JavaScript |

## 功能

**未登录用户可以**

- 浏览首页、家教需求列表、教员简历列表
- 按关键词搜索需求或简历
- 查看需求与简历详情

**家长（role=0）**

- 发布家教需求，填写科目、年级、薪资、联系方式与描述
- 查看自己发布的需求，编辑处于待审核状态的需求，关闭已发布的需求
- 编辑个人信息

**教员（role=1）**

- 发布家教简历，填写可教科目、教学经验、可授课时间、期望薪资、个人简介与资格证书
- 查看自己发布的简历，编辑处于待审核状态的简历
- 编辑个人信息

**管理员（role=2）**

- 后台仪表盘
- 审核需求与简历：通过或拒绝
- 用户管理：查看全部用户，禁用或启用账号

## 数据库设计

库名 `tutor_system`，建表脚本见 `db/init.sql`。

**user**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| user_id | bigint | 主键，自增 |
| username | varchar(50) | 登录账号，唯一 |
| password | varchar(100) | BCrypt 密文 |
| email | varchar(100) | 邮箱，唯一 |
| phone | varchar(20) | 联系电话 |
| role | tinyint | 0 家长 / 1 教员 / 2 管理员 |
| status | tinyint | 0 禁用 / 1 正常 |
| create_time | datetime | 注册时间 |

**tutor_demand**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| demand_id | bigint | 主键，自增 |
| publisher_id | bigint | 发布者 user_id |
| subject | varchar(20) | 科目 |
| grade | varchar(20) | 年级 |
| description | text | 需求描述 |
| salary | decimal(10,2) | 期望薪资（元/小时） |
| contact | varchar(100) | 联系方式 |
| publish_time | datetime | 发布时间 |
| status | tinyint | 0 待审核 / 1 已发布 / 2 已关闭 |

**tutor_resume**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| resume_id | bigint | 主键，自增 |
| publisher_id | bigint | 发布者 user_id |
| subjects | varchar(100) | 可教科目，逗号分隔 |
| experience | text | 教学经验 |
| available_time | varchar(100) | 可授课时间 |
| expected_salary | decimal(10,2) | 期望薪资 |
| introduction | text | 个人简介 |
| certificates | varchar(255) | 资格证书 |
| publish_time | datetime | 发布时间 |
| status | tinyint | 0 待审核 / 1 已发布 / 2 已关闭 |

### 信息状态流转

发布和编辑都会把信息置为待审核，审核通过后才对外可见：

```
发布/编辑 ──> 0 待审核 ──管理员通过──> 1 已发布 ──发布者关闭──> 2 已关闭
                  └──管理员拒绝──> 2 已关闭
```

拒绝与关闭共用状态 2，因此被拒绝的信息在发布者看来显示为「已关闭」。如果后续需要区分「被拒绝」和「主动关闭」，需要给状态字段增加一个取值。

## 快速开始

### 方式一：Docker（推荐）

需要 Docker 与 Compose v2，无需本机安装 JDK、Maven 和 MySQL。

```bash
docker compose up -d --build      # 首次会构建镜像并拉取 MySQL，约几分钟
docker compose logs -f app        # 看到 "Started TutorSystemApplication" 即启动成功
```

启动后访问 <http://localhost:8081>。

```bash
docker compose down               # 停止，数据库数据保留
docker compose down -v            # 停止并清空数据库，下次启动重新执行 init.sql
```

端口占用说明：

| 服务 | 宿主机端口 | 容器内端口 | 说明 |
| --- | --- | --- | --- |
| 应用 | 8081 | 8080 | 8080 可能被其他项目占用，用 `APP_PORT=8080 docker compose up -d` 可改 |
| MySQL | 3308 | 3306 | 本机 3306 常已有 MySQL，因此改用 3308 |

首次启动时容器会挂载 `db/init.sql` 自动建表和写入演示数据。如果改了建表脚本，需要用 `docker compose down -v` 清掉数据卷才会重新执行。

用 Navicat 等工具连数据库：`127.0.0.1:3308`，库 `tutor_system`，账号 `tf_app`，密码 `tf_pass_2026`。

### 方式二：本地运行

需要 JDK 17、Maven 3.6+、MySQL 8.0。

**1. 初始化数据库**

```bash
mysql -uroot -p < db/init.sql
```

脚本会创建 `tutor_system` 库、三张表，并写入三条演示账号和演示数据。

**2. 配置数据库连接**

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/tutor_system?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai
    username: root          # 也可以用环境变量 DB_USERNAME 覆盖
    password: 1234          # 也可以用环境变量 DB_PASSWORD 覆盖
```

**3. 构建并启动**

```bash
mvn clean package -DskipTests
java -jar target/tutor-system-1.0.0.jar
```

或者直接用 Maven 插件运行：

```bash
mvn spring-boot:run
```

访问 <http://localhost:8080>。

## 演示账号

`db/init.sql` 中已写入以下账号：

| 角色 | 用户名 | 密码 |
| --- | --- | --- |
| 管理员 | admin | admin123 |
| 家长 | parent1 | parent123 |
| 教员 | tutor1 | tutor123 |

注册页只允许注册家长和教员，管理员账号由脚本写入。

## 项目结构

```
tutor-system/
├── src/main/java/com/tutorsystem/
│   ├── config/          SecurityConfig（鉴权规则）、PasswordEncoderConfig
│   ├── controller/      Auth、Home、Demand、Resume、User、Admin 六个控制器
│   ├── entity/          User、TutorDemand、TutorResume
│   ├── mapper/          MyBatis-Plus Mapper，SQL 写在 @Select 注解里
│   ├── service/         服务接口与 impl 实现
│   └── TutorSystemApplication.java
├── src/main/resources/
│   ├── static/style.css
│   ├── templates/       21 个 Thymeleaf 页面
│   │   ├── admin/       后台：仪表盘、需求审核、简历审核、用户管理
│   │   ├── auth/        登录、注册
│   │   ├── demand/      需求列表、详情、发布、编辑、我的需求
│   │   ├── resume/      简历列表、详情、发布、编辑
│   │   ├── user/        个人中心、编辑资料
│   │   └── error/       403、404、500
│   └── application.yml
├── src/test/java/com/tutorsystem/PasswordUtilsTest.java
├── db/init.sql
├── tests/e2e_test.py
├── Dockerfile          多阶段构建，Maven 编译 -> JRE 运行
├── docker-compose.yml
├── maven-settings.xml  镜像内构建专用，走阿里云镜像
└── pom.xml
```

## 路由一览

共 32 个路由，按控制器分组：

| 控制器 | 方法与路径 | 说明 | 权限 |
| --- | --- | --- | --- |
| Auth | GET /login | 登录页 | 公开 |
| Auth | POST /login | 登录处理（Spring Security 接管） | 公开 |
| Auth | GET /register | 注册页 | 公开 |
| Auth | POST /register | 注册处理 | 公开 |
| Auth | GET /logout | 退出登录 | 公开 |
| Home | GET / | 首页，展示最新需求与简历 | 公开 |
| Home | GET /demands | 需求列表，支持 `keyword` | 公开 |
| Home | GET /resumes | 简历列表，支持 `keyword` | 公开 |
| Demand | GET /demand/list | 需求列表（分页 + 搜索） | 登录 |
| Demand | GET /demand/detail/{id} | 需求详情 | 公开 |
| Demand | GET /demand/publish | 发布需求页 | 家长 |
| Demand | POST /demand/publish | 提交发布 | 家长 |
| Demand | GET /demand/edit/{id} | 编辑需求页，仅待审核可编辑 | 家长 |
| Demand | POST /demand/update | 提交修改 | 家长 |
| Demand | POST /demand/close/{id} | 关闭需求 | 家长 |
| Demand | GET /demand/my | 我发布的需求 | 登录 |
| Resume | GET /resume/list | 简历列表 | 登录 |
| Resume | GET /resume/detail/{id} | 简历详情 | 公开 |
| Resume | GET /resume/publish | 发布简历页 | 教员 |
| Resume | POST /resume/publish | 提交发布 | 教员 |
| Resume | GET /resume/edit/{id} | 编辑简历页，仅待审核可编辑 | 教员 |
| Resume | POST /resume/update | 提交修改 | 教员 |
| Resume | POST /resume/close/{id} | 关闭简历 | 教员 |
| User | GET /user/center | 个人中心 | 登录 |
| User | GET /user/edit | 编辑个人资料页 | 登录 |
| User | POST /user/update | 提交资料修改 | 登录 |
| Admin | GET /admin/dashboard | 后台仪表盘 | 管理员 |
| Admin | GET /admin/demands/review | 待审核需求列表 | 管理员 |
| Admin | POST /admin/demands/approve/{id} | 需求审核（status=1 通过，2 拒绝） | 管理员 |
| Admin | GET /admin/resumes/review | 待审核简历列表 | 管理员 |
| Admin | POST /admin/resumes/approve/{id} | 简历审核 | 管理员 |
| Admin | GET /admin/users | 用户列表 | 管理员 |
| Admin | POST /admin/users/toggle/{id} | 禁用 / 启用用户 | 管理员 |

## 鉴权实现

`SecurityConfig` 用 `authorizeHttpRequests` 声明访问规则，静态资源与浏览类页面放行，其余按角色限制：

- `/admin/**` 需要 `ROLE_ADMIN`
- `/demand/publish`、`/demand/edit/**`、`/demand/update`、`/demand/close/**` 需要 `ROLE_PARENT`
- `/resume/publish`、`/resume/edit/**`、`/resume/update`、`/resume/close/**` 需要 `ROLE_TUTOR`
- 其余请求需要登录

角色来自 `user.role` 数字，`UserServiceImpl.loadUserByUsername` 把它映射成 `ROLE_PARENT` / `ROLE_TUTOR` / `ROLE_ADMIN`。账号被禁用时 `status != 1`，对应 `UserDetails.enabled = false`，Spring Security 会拒绝登录。

导航栏里的「家教需求」「教员简历」指向公开的 `/demands` 和 `/resumes`；`/demand/list` 与 `/resume/list` 是各自控制器下的另一套列表入口，没有被列入放行名单，因此需要登录才能访问。两套列表功能重合，实际使用以公开入口为主。

登录成功后 `successHandler` 按角色跳转：管理员去 `/admin/dashboard`，其他角色去 `/user/center`。

因为页面是服务端渲染、表单直接提交，未做前后端分离，所以关闭了 CSRF（`csrf.disable()`）。如果以后要接入 REST 接口，应当重新开启。

## 测试

`tests/e2e_test.py` 是接口级端到端测试，打真实 HTTP 请求、走真实数据库，覆盖 38 项断言：

- 匿名可访问的页面与静态资源是否正常
- 三角色登录、未登录跳转、家长访问后台返回 403、错误密码被拒
- 需求与简历的关键词搜索
- 完整业务流转：发布需求 → 编辑（校验表单回填与提交地址）→ 管理员审核通过 → 已发布不可再编辑
- 反向用例：编辑他人或不存在的需求、查看不存在的详情

需要先启动服务，默认连 8080，可用环境变量覆盖：

```bash
python tests/e2e_test.py
BASE_URL=http://localhost:8081 python tests/e2e_test.py
```

## 已知限制

- 审核拒绝与主动关闭共用状态 2，无法区分
- 演示数据里的邮箱和手机号都是占位值（`@example.com`、`13800138000`），注册页也不做邮箱真实性校验
- 未做图片上传，资格证书只存文本
- 未接入实时沟通、评价、邮件通知
- `PasswordUtilsTest` 是 `@SpringBootTest`，运行 `mvn test` 需要数据库可用；只想验证编译可加 `-DskipTests`
- 分页查询在列表页逐条查发布者信息，数据量大时应改成连表查询

## 许可证

MIT，见 [LICENSE](LICENSE)。

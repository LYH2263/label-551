# 网上蛋糕城 (Online Cake Shop)

一个功能完整的在线蛋糕购物商城系统，采用 JSP + CSS + JavaScript 传统 Web 架构，支持用户浏览商品、购物车、下单购买，以及管理员后台管理功能。

## 🧭 Project Type

- **Type: A) FULLSTACK_WEB** (Frontend + Backend + Database)

## 🧩 技术栈

- **Frontend (View - 静态资源)**: CSS + JavaScript + Nginx
- **Backend (View + Controller)**: JSP + Java Servlet + JDBC (Tomcat 9 + JDK 11)
- **Model**: JavaBean (POJO)
- **Database**: MySQL 8.0
- **Architecture**: MVC (Model-View-Controller)
- **Container**: Docker + Docker Compose

**关键特性**：
- ✅ JSP 与 CSS 完全分离（CSS/JS 由 Nginx 提供，JSP 由 Tomcat 提供）
- ✅ JavaScript 实现手机号格式前端验证
- ✅ 原生 JDBC 实现数据库操作
- ✅ 字符编码过滤器统一处理中文乱码
- ✅ 登录拦截器保护受限页面
- ✅ 三服务 Docker 架构 (frontend + backend + db)

## 🚀 快速启动（唯一命令）

1. 确保 Docker Desktop / Docker Engine 已安装并运行
2. 在项目根目录执行：

```bash
docker compose up
```

3. 等待所有服务启动完成（首次启动需要构建镜像，约 3-5 分钟）
4. 访问地址：
   - **前台页面**: http://localhost:3000
   - **管理员后台**: http://localhost:3000/admin/login

## 👤 测试账号

### 管理员账号
- 手机号：`13800000000`
- 密码：`admin123`
- 显示名：管理员

### 普通用户账号
- 手机号：`13800000001`
- 密码：`123456`
- 显示名：张三


## ✅ 功能清单

### 用户功能
- [x] F1: 用户注册（手机号 + 密码 + 用户名，前端JS验证手机号格式）
- [x] F2: 用户登录/退出（Session保持登录状态）
- [x] F3: 个人中心（查看/修改个人信息）
- [x] F4: 收货地址管理（增删改查、设置默认）
- [x] F5: 蛋糕列表展示（分类筛选、关键词搜索、分页加载）
- [x] F6: 蛋糕详情查看（高清图片、规格、库存、加入购物车）
- [x] F7: 购物车功能（添加、修改数量、删除）
- [x] F8: 下单结算（选择收货地址、提交订单）
- [x] F9: 订单列表查看（按状态筛选）
- [x] F10: 订单详情查看

### 管理员功能
- [x] F11: 后台仪表盘（统计概览）
- [x] F12: 商品管理（增删改查、图片上传、上下架）
- [x] F13: 分类管理（增删改）
- [x] F14: 订单管理（查看、修改状态）
- [x] F15: 用户管理（查看用户列表）

## 🔎 自测说明

### 成功路径

1. **用户注册登录**
   - 访问 http://localhost:3000
   - 点击"注册"，输入手机号（格式验证：1[3-9]xxxxxxxxx）、用户名、密码完成注册
   - 使用注册的手机号和密码登录

2. **浏览蛋糕**
   - 首页展示热门蛋糕
   - 点击蛋糕分类或"蛋糕列表"查看完整列表
   - 支持按分类筛选、关键词搜索、分页浏览

3. **加入购物车**
   - 点击蛋糕进入详情页
   - 选择数量，点击"加入购物车"
   - 查看购物车，调整商品数量或删除

4. **下单结算**
   - 进入购物车，点击"去结算"
   - 选择收货地址，提交订单
   - 在"我的订单"中查看订单状态

5. **管理员后台**
   - 访问 http://localhost:3000/admin/login
   - 使用管理员账号登录
   - 管理商品、分类、订单、用户

### 失败路径

1. **未登录拦截**：未登录访问个人中心、购物车、订单等页面，自动跳转到登录页
2. **权限控制**：普通用户无法访问管理员后台
3. **表单验证**：注册/登录时输入格式错误会实时提示
4. **库存控制**：库存不足时无法添加到购物车
5. **错误提示修复**：
   - 手机号重复注册：显示"该手机号已注册"（非"网络错误"）
   - 原密码错误：显示"原密码错误"（非"网络错误"）

## 🧾 证据文件

```
evidence/
├── 01_boot.txt                    # Docker 三服务启动状态
├── 02_home_3service.png           # 首页（三服务架构）
├── 03_register_duplicate_error.png # 手机号重复错误提示修复
├── 04_password_wrong_error.png    # 原密码错误提示修复
├── 05_cake_list.png               # 蛋糕列表
├── 06_cart.png                    # 购物车
├── 07_admin_login.png             # 管理员登录
└── ...
```

## 📁 项目结构

```
├── docker-compose.yml    # Docker 编排文件（三服务）
├── README.md             # 项目说明
├── prompt.md             # 需求文档
├── CONSTRAINTS.md        # 交付约束

├── frontend/             # 前端静态资源层 ✅ 新增
│   ├── Dockerfile        # Nginx 镜像构建
│   ├── nginx.conf        # Nginx 配置（反向代理）
│   ├── css/              # 样式文件（JSP与CSS分离）
│   ├── js/               # JavaScript 文件
│   └── images/           # 图片资源

├── backend/              # 后端层（JSP + Servlet）
│   ├── Dockerfile        # Tomcat 镜像构建
│   ├── pom.xml           # Maven 配置
│   └── src/main/
│       ├── java/com/cakeshop/
│       │   ├── model/    # 数据模型 (POJO)
│       │   ├── dao/      # 数据访问层 (JDBC原生实现)
│       │   ├── servlet/  # 控制器 (Servlet)
│       │   ├── filter/   # 过滤器 (字符编码、登录拦截)
│       │   └── util/     # 工具类
│       └── webapp/
│           └── WEB-INF/
│               └── views/  # JSP页面 ✅

├── db/
│   └── init.sql          # 数据库初始化脚本

└── evidence/             # 证据截图
```

## 🔧 端口说明

| 服务 | 容器端口 | 宿主机端口 | 说明 |
|------|----------|------------|------|
| Frontend (Nginx) | 80 | 3000 | 统一入口（静态资源 + 反向代理） |
| Backend (Tomcat) | 8080 | 不暴露 | JSP页面 + API接口 |
| Database (MySQL) | 3306 | 不暴露 | 内部网络访问 |

## 🏗️ 三服务架构说明

```
用户请求
    ↓
┌─────────────────────────────────────┐
│  Frontend (Nginx:3000)              │
│  - 静态资源: /css/, /js/, /images/  │
│  - 反向代理: 其他请求 → Backend     │
└─────────────────────────────────────┘
    ↓
┌─────────────────────────────────────┐
│  Backend (Tomcat:8080)              │
│  - JSP 页面渲染                     │
│  - Servlet API 接口                 │
│  - 原生 JDBC 数据库操作             │
└─────────────────────────────────────┘
    ↓
┌─────────────────────────────────────┐
│  Database (MySQL:3306)              │
│  - 数据持久化                       │
│  - Volume 持久化存储                │
└─────────────────────────────────────┘
```

## 📝 关键实现

### 1. JSP 与 CSS 分离
所有 JSP 页面通过 `<link>` 标签引用独立的 CSS 文件，CSS 由 Nginx 提供：
```jsp
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/common.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/user.css">
```

### 2. JavaScript 手机号验证
```javascript
function validatePhone(phone) {
    const regex = /^1[3-9]\d{9}$/;
    return regex.test(phone);
}
```

### 3. MVC 架构
- **Model**：User, Cake, Category, Order 等 POJO 类
- **View**：JSP 页面（位于 WEB-INF/views/）+ CSS/JS（位于 frontend/）
- **Controller**：Servlet（PageServlet 处理页面路由，其他Servlet 处理API）

### 4. 错误提示优化
修复了业务错误显示为"网络错误"的问题：
- 手机号重复 → "该手机号已注册"
- 原密码错误 → "原密码错误"
- 密码错误 → "密码错误"

## 📦 交付范围（QC Submission Package）

本项目交付范围仅包含以下运行时目录：

| 目录 | 内容 | 说明 |
|------|------|------|
| `backend/` | Java Servlet + JSP + JDBC | 后端业务逻辑 + 页面模板 |
| `frontend/` | CSS + JS + 静态资源 | 前端静态文件（Nginx托管） |
| `db/` | SQL初始化脚本 | 数据库建表+种子数据 |
| `docker-compose.yml` | Docker编排 | 三服务一键启动配置 |

> **排除说明**：`templates/` 目录下的参考模板仅为学习参考，**不属于本项目交付物**，不参与构建和运行。

## ⚠️ 注意事项

1. 首次启动需要构建镜像，请耐心等待
2. 数据库数据持久化在 Docker Volume 中
3. 已预置蛋糕分类和示例商品数据
4. 图片上传功能支持 jpg/png/gif 格式
5. 所有CSS文件独立于JSP，实现完全分离
6. 前端使用JavaScript实现表单验证（包括手机号格式验证）

## 🎯 技术栈总结

| 层级 | 技术实现 | 说明 |
|------|----------|------|
| View (静态) | CSS + JS + Nginx | frontend 服务提供 |
| View (动态) | JSP | backend 服务渲染 |
| Controller | Java Servlet | 页面路由 + API接口 |
| Model | JavaBean (POJO) | 数据实体类 |
| DAO | JDBC 原生实现 | 不使用ORM框架 |
| Database | MySQL 8.0 | 数据持久化 |
| Container | Docker + Nginx + Tomcat | 三服务一键启动 |

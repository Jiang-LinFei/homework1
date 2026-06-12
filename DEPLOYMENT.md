# 机房预约管理系统 · 部署文档

面向 **CentOS 8 + 宝塔面板 + Docker Compose** 的完整部署指南。整套系统以 4 个容器运行：MySQL 5.7 / Redis 7 / Django 后端（gunicorn）/ 前端（Nginx 托管 Vue 构建产物并反代 `/api`）。

---

## 0. 架构总览

```
浏览器  ──>  frontend 容器(Nginx:80)  ──┬── 静态文件(Vue 打包产物)
                                        ├── /api/   反代──> backend 容器(gunicorn:8000)
                                        ├── /admin/ 反代──> backend 容器(Django Admin)
                                        └── /static/反代──> backend 容器(静态资源)
                                                              │
                                          backend ──┬── mysql 容器(3306)
                                                     └── redis 容器(6379)
```

- 对外只暴露**一个端口**（默认 `8080`，映射到 frontend 容器的 80）。
- backend / mysql / redis 仅在 compose 内网互通，不对外暴露，安全性更好。
- 数据持久化在 Docker 卷 `mysql_data`、`redis_data`，容器重建不丢数据。

---

## 1. 环境要求

| 组件 | 版本 | 说明 |
| --- | --- | --- |
| 操作系统 | CentOS 8 | 其它 Linux 同理 |
| Docker | 20.10+ | 宝塔「Docker 管理器」或手动安装 |
| Docker Compose | v2（`docker compose`） | Docker 自带的 compose 插件 |
| 内存 | ≥ 2 GB | MySQL + 构建前端需要一定内存 |
| 磁盘 | ≥ 5 GB 空闲 | 镜像 + 数据卷 |

> 代码里已锁定运行版本：Python 3.10.11、Django 3.2.25、DRF 3.14、MySQL 5.7.44、Node 20、Vue 3.4.38、Vite 5.4.14 —— 这些都在容器内，宿主机**无需**单独安装 Python/Node/MySQL。

---

## 2. 安装 Docker（两种方式任选）

### 方式 A：宝塔面板（推荐，省事）

1. 宝塔面板 → 「软件商店」→ 搜索 **Docker 管理器** → 安装。
2. 安装后在「Docker 管理器」里确认 Docker 服务已启动。
3. 宝塔新版自带 `docker compose` 命令；若没有，按方式 B 补装 compose 插件。

### 方式 B：命令行手动安装

```bash
# 安装 Docker
sudo dnf install -y dnf-plugins-core
sudo dnf config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
sudo dnf install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin

# 启动并设为开机自启
sudo systemctl enable --now docker

# 验证
docker --version
docker compose version
```

> 如果 `docker compose version` 提示找不到命令，说明缺 compose 插件：
> `sudo dnf install -y docker-compose-plugin` 即可。

---

## 3. 获取代码

```bash
# 放到你习惯的目录，例如 /www/wwwroot
cd /www/wwwroot
git clone <你的仓库地址> homework1   # 或把本地 d:\project\cgx\homework1 上传上来
cd homework1
git checkout cgx                      # 使用 cgx 分支
```

确认目录里有：`backend/`、`frontend/`、`docker-compose.yml`、`.env.example`。

---

## 4. 配置环境变量

```bash
cp .env.example .env
vi .env        # 按需修改
```

`.env` 各项说明：

| 变量 | 说明 | 生产建议 |
| --- | --- | --- |
| `SECRET_KEY` | Django 密钥 | **务必改成一段随机长字符串** |
| `DEBUG` | 调试开关 | 生产填 `false` |
| `ALLOWED_HOSTS` | 允许的访问域名/IP，逗号分隔 | 填你的域名或服务器 IP，如 `example.com,1.2.3.4`；图省事可暂填 `*` |
| `MYSQL_ROOT_PASSWORD` | MySQL root 密码 | 改成强密码 |
| `DB_NAME` / `DB_USER` / `DB_PASSWORD` | 业务库名/账号/密码 | 改成强密码 |
| `INIT_DEMO` | 首次启动是否灌演示数据 | 想要演示账号填 `true`，纯净库填 `false` |
| `WEB_PORT` | 对外端口 | 默认 `8080`，可改 `80`（需确保未被占用） |

生成随机 `SECRET_KEY` 的一个办法：

```bash
python3 -c "import secrets;print(secrets.token_urlsafe(50))"
# 没有 python3 也可以用：openssl rand -base64 50
```

---

## 5. 构建并启动

```bash
docker compose up -d --build
```

首次会拉取镜像 + 构建前后端，耗时几分钟。启动后：

- backend 容器入口会**自动等待 MySQL 就绪 → 建表(migrate) → 收集静态文件 → （可选）灌演示数据 → 启动 gunicorn**。
- 无需手动跑 `migrate` / `createsuperuser`。

查看状态与日志：

```bash
docker compose ps                 # 四个服务应为 Up / healthy
docker compose logs -f backend    # 看后端启动日志（Ctrl+C 退出）
docker compose logs -f frontend
```

---

## 6. 访问系统

浏览器打开：`http://<服务器IP>:<WEB_PORT>`，例如 `http://1.2.3.4:8080`。

演示账号（`INIT_DEMO=true` 时自动创建）：

| 角色 | 账号 | 密码 |
| --- | --- | --- |
| 管理员 | `admin` | `admin` |
| 教师 | `teacher` | `teacher` |
| 学生 | `student` | `student` |

Django 后台：`http://<服务器IP>:<WEB_PORT>/admin/`（用 `admin` / `admin`）。

> **生产环境记得登录后台改掉演示账号密码**，或把 `.env` 的 `INIT_DEMO` 设为 `false` 用纯净库自行建账号。

---

## 7. 宝塔放行端口 / 配置域名

### 7.1 放行端口

宝塔面板 → 「安全」→ 放行 `WEB_PORT`（如 8080）。同时确认云服务器**安全组**也放行该端口。

### 7.2（可选）用域名 + 80/443 反代

如果希望用域名访问、并由宝塔统一管 HTTPS：

1. 宝塔「网站」→ 添加站点，绑定你的域名（可不建实际目录）。
2. 站点设置 → 「反向代理」→ 目标 URL 填 `http://127.0.0.1:8080`。
3. 站点设置 → 「SSL」→ 申请 Let's Encrypt 证书，开启「强制 HTTPS」。

这样外部走 `https://你的域名`，宝塔 Nginx 再转发到容器的 8080。

---

## 8. 常用运维命令

```bash
# 停止 / 启动 / 重启
docker compose stop
docker compose start
docker compose restart

# 更新代码后重新部署
git pull
docker compose up -d --build

# 查看日志
docker compose logs -f backend

# 进入后端容器执行 Django 命令（如手动建超级管理员）
docker compose exec backend python manage.py createsuperuser

# 彻底停止并删除容器（数据卷保留，数据不丢）
docker compose down

# 连数据卷一起删除（⚠ 会清空数据库，慎用）
docker compose down -v
```

---

## 9. 数据备份与恢复

数据库数据在 Docker 卷 `mysql_data` 中。

**备份**（导出 SQL 到宿主机）：

```bash
docker compose exec mysql \
  sh -c 'exec mysqldump -uroot -p"$MYSQL_ROOT_PASSWORD" "$MYSQL_DATABASE"' \
  > backup_$(date +%F).sql
```

**恢复**：

```bash
docker compose exec -T mysql \
  sh -c 'exec mysql -uroot -p"$MYSQL_ROOT_PASSWORD" "$MYSQL_DATABASE"' \
  < backup_2026-06-12.sql
```

建议在宝塔「计划任务」里加一条每日 shell，执行上面的备份命令。

---

## 10. 常见问题（FAQ）

**Q1. 启动后访问报错 / 后端一直重启？**
看后端日志：`docker compose logs backend`。多数是数据库没连上——入口脚本会自动等 MySQL，等待期间属正常；若长时间起不来，检查 `.env` 的数据库密码是否前后一致（`MYSQL_ROOT_PASSWORD`/`DB_PASSWORD`）。

**Q2. 改了 `.env` 不生效？**
`.env` 改完要重建容器：`docker compose up -d`（环境变量在容器创建时注入）。改了数据库密码且卷里已有旧数据，需 `docker compose down -v` 重来（会清库）或手动改库内密码。

**Q3. 页面能开但接口 404 / 跨域？**
本系统前端和接口同源（都经 Nginx），正常不会跨域。若你单独改了端口或反代，确认 `frontend/nginx.conf` 里 `/api/` 反代指向 `backend:8000`。

**Q4. 8080 端口被占用？**
改 `.env` 里 `WEB_PORT=别的端口`，再 `docker compose up -d`。

**Q5. 前端构建内存不足（exit code 137）？**
给服务器加 swap，或在配置更高的机器上 `docker compose build` 后再部署。

**Q6. 想要纯净环境、不要演示数据？**
部署前把 `.env` 的 `INIT_DEMO` 设为 `false`，然后进后端容器 `createsuperuser` 自建账号。

---

## 11. 端口与目录速查

| 项 | 值 |
| --- | --- |
| 对外端口 | `WEB_PORT`（默认 8080）→ frontend:80 |
| 后端（仅内网） | backend:8000 |
| MySQL（仅内网） | mysql:3306 |
| Redis（仅内网） | redis:6379 |
| 数据卷 | `mysql_data`、`redis_data` |
| 代码目录 | `backend/`（Django）、`frontend/`（Vue） |

部署中遇到问题，把 `docker compose ps` 和 `docker compose logs backend` 的输出发出来即可定位。

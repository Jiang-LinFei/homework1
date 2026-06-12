# 机房预约管理系统

按时间段 / 课表预约机房与机位，自动冲突检测、审批工作流、占用日历与利用率仪表盘。前后端分离，Docker Compose 一键部署。

## 技术栈

| 层 | 技术 |
| --- | --- |
| 后端 | Python 3.10 · Django 3.2.25 · DRF 3.14 · SimpleJWT · django-redis |
| 数据 | MySQL 5.7 · Redis 7 |
| 前端 | Node 20 · Vue 3.4.38 · Vite 5.4.14 · Element Plus · Pinia · ECharts · FullCalendar |
| 部署 | Docker Compose（Nginx 托管前端并反代 `/api`、gunicorn 跑后端） |

## 功能

- 角色：管理员 / 教师 / 学生（JWT 登录，按角色控制菜单与接口）
- 机房 / 机位 / 节次管理
- 预约：按节次或自定义时间预约整间机房或指定机位，**提交时自动冲突检测**
- 审批流：学生/教师提交 → 教师/管理员通过或驳回
- 排课：给机房排周期性课程，参与冲突检测与占用展示
- 占用日历：FullCalendar 周/日/月视图，预约（蓝）与排课（橙）一图呈现
- 仪表盘：预约总数、通过率、状态分布、机房 Top5、近 7 日趋势

## 快速开始（Docker，推荐）

```bash
cp .env.example .env      # 按需修改密码/端口
docker compose up -d --build
```

启动后访问 `http://服务器IP:8080`（端口由 `.env` 的 `WEB_PORT` 控制）。
首次启动会自动建表并生成演示数据。

演示账号：

| 角色 | 账号 | 密码 |
| --- | --- | --- |
| 管理员 | `admin` | `admin` |
| 教师 | `teacher` | `teacher` |
| 学生 | `student` | `student` |

Django 后台：`http://服务器IP:8080/admin/`（用 `admin` / `admin`）。

## 本地开发

后端：

```bash
cd backend
python -m venv .venv && source .venv/bin/activate   # Windows: .venv\Scripts\Activate.ps1
pip install -r requirements.txt
# 默认连接 127.0.0.1:3307 的 MySQL 与 127.0.0.1:6380 的 Redis，可用环境变量覆盖
python manage.py migrate
python manage.py init_demo
python manage.py runserver        # http://127.0.0.1:8000
```

前端：

```bash
cd frontend
npm install
npm run dev                       # http://127.0.0.1:5173，已配置 /api 代理到 :8000
```

## 主要环境变量

| 变量 | 说明 | 默认 |
| --- | --- | --- |
| `DB_HOST` / `DB_PORT` | MySQL 地址 | `mysql` / `3306`（compose 内） |
| `DB_NAME` / `DB_USER` / `DB_PASSWORD` | 数据库名/账号/密码 | `jifang` / `jifang` / `jifang123` |
| `REDIS_URL` | Redis 连接串 | `redis://redis:6379/1` |
| `SECRET_KEY` | Django 密钥（生产务必修改） | 开发占位值 |
| `DEBUG` | 调试模式 | `false` |
| `INIT_DEMO` | 首启是否灌演示数据 | `true` |
| `WEB_PORT` | 前端对外端口 | `8080` |

## 目录结构

```
backend/            Django 项目（config + apps：accounts/rooms/bookings/schedules/stats/common）
frontend/           Vue3 + Vite SPA
docker-compose.yml  四服务编排（mysql/redis/backend/frontend）
docs/               设计文档与实现计划
```

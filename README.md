# 机房信息管理系统（Django + MySQL + Redis）

一个学生作业级别的机房信息管理系统，前端采用 INSPINIA Bootstrap 后台模板，界面较为美观。
后端基于 **Django 4.2**，数据库使用 **MySQL**，缓存与会话使用 **Redis**。

> 本项目前端模板基于开源项目 [lyk19940625/LaboratoryManagementSystem](https://github.com/lyk19940625/LaboratoryManagementSystem)，
> 在其基础上整理为可运行的 Django 4.2 结构，并集成了 Redis（缓存 + 会话）。

## 功能

- 登录 / 注册（区分管理员、学生角色）
- 机房任务发布、领取、放弃
- 我的任务、任务进度、作业上传与下载
- 上机考勤 / 签到统计
- Django 后台管理（用户、任务、考勤等）

## 技术栈

| 层 | 技术 |
| --- | --- |
| Web 框架 | Django 4.2 |
| 数据库 | MySQL（utf8mb4） |
| 缓存 / 会话 | Redis（django-redis，`cached_db` 会话） |
| 前端 | Bootstrap 3 + INSPINIA 模板 + jQuery |

## 环境要求

- Python 3.10+
- MySQL 5.7+ / MariaDB 10.4+
- Redis 5+

## 快速开始

```bash
# 1. 创建并激活虚拟环境
python3 -m venv .venv
source .venv/bin/activate

# 2. 安装依赖
pip install -r requirements.txt

# 3. 准备数据库（MySQL）
#    默认连接：库 lab_management / 用户 lab / 密码 lab123456
mysql -uroot -p -e "CREATE DATABASE lab_management CHARACTER SET utf8mb4;"
mysql -uroot -p -e "CREATE USER 'lab'@'localhost' IDENTIFIED BY 'lab123456'; \
                    GRANT ALL ON lab_management.* TO 'lab'@'localhost'; FLUSH PRIVILEGES;"

# 4. 确保 Redis 已启动（默认 redis://127.0.0.1:6379/1）

# 5. 迁移数据库
python manage.py migrate

# 6. 初始化演示数据（可选）
python manage.py init_demo

# 7. 启动开发服务器
python manage.py runserver 0.0.0.0:8000
```

打开 http://127.0.0.1:8000/ 即可访问。

## 演示账号（执行 `init_demo` 后）

| 角色 | 账号(uid) | 密码 |
| --- | --- | --- |
| 管理员 | `2001` | `123456` |
| 学生 | `2002` | `123456` |
| 学生 | `2003` | `123456` |
| 后台超级用户 | `admin` | `admin123456` |

Django 后台地址：http://127.0.0.1:8000/admin/

## 配置（环境变量）

所有连接信息都可以通过环境变量覆盖，默认值见 `LaboratoryManagementSystem/settings.py`：

| 变量 | 默认值 | 说明 |
| --- | --- | --- |
| `MYSQL_DATABASE` | `lab_management` | 数据库名 |
| `MYSQL_USER` | `lab` | 数据库用户 |
| `MYSQL_PASSWORD` | `lab123456` | 数据库密码 |
| `MYSQL_HOST` | `127.0.0.1` | 数据库地址 |
| `MYSQL_PORT` | `3306` | 数据库端口 |
| `REDIS_URL` | `redis://127.0.0.1:6379/1` | Redis 连接 |
| `DJANGO_DEBUG` | `True` | 调试模式 |
| `DJANGO_SECRET_KEY` | （内置开发用） | 生产环境务必修改 |

## 目录结构

```
.
├── manage.py
├── requirements.txt
├── LaboratoryManagementSystem/   # 项目配置包（settings/urls/wsgi/asgi）
├── LMS/                          # 业务应用（models/views/admin/migrations）
│   └── management/commands/init_demo.py
├── templates/                    # 页面模板
├── static/                       # 静态资源（INSPINIA 模板）
└── upload/                       # 作业上传目录
```

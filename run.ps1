# 机房信息管理系统 - Windows 一键启动脚本 (PowerShell)
# 用法：在项目根目录执行  .\run.ps1
#   首次运行若提示禁止执行脚本，先运行：
#   Set-ExecutionPolicy -Scope CurrentUser -ExecutionPolicy RemoteSigned
#
# 依赖：Python 3.10+、Docker Desktop（用于启动 MySQL 与 Redis）

$ErrorActionPreference = "Stop"

Write-Host "==> [1/5] 创建并激活虚拟环境" -ForegroundColor Cyan
if (-not (Test-Path ".venv")) {
    python -m venv .venv
}
. .\.venv\Scripts\Activate.ps1

Write-Host "==> [2/5] 安装依赖" -ForegroundColor Cyan
python -m pip install --upgrade pip
pip install -r requirements.txt

Write-Host "==> [3/5] 启动 MySQL 与 Redis (Docker)" -ForegroundColor Cyan
if (Get-Command docker -ErrorAction SilentlyContinue) {
    if (-not (docker ps -a --format '{{.Names}}' | Select-String -Quiet '^lms-redis$')) {
        docker run -d --name lms-redis -p 6379:6379 redis | Out-Null
    } else { docker start lms-redis | Out-Null }

    if (-not (docker ps -a --format '{{.Names}}' | Select-String -Quiet '^lms-mysql$')) {
        docker run -d --name lms-mysql -p 3306:3306 `
            -e MYSQL_ROOT_PASSWORD=123456 `
            -e MYSQL_DATABASE=lab_management `
            -e MYSQL_USER=lab -e MYSQL_PASSWORD=lab123456 `
            mysql:8 | Out-Null
    } else { docker start lms-mysql | Out-Null }

    Write-Host "    等待 MySQL 就绪..." -ForegroundColor DarkGray
    do {
        Start-Sleep -Seconds 3
        docker exec lms-mysql mysqladmin ping -ulab -plab123456 --silent 2>$null
    } while ($LASTEXITCODE -ne 0)
    Write-Host "    MySQL 已就绪" -ForegroundColor Green
} else {
    Write-Host "    未检测到 docker。请确保本机已运行 MySQL(库 lab_management / 用户 lab 密码 lab123456) 和 Redis(6379)。" -ForegroundColor Yellow
    Write-Host "    连接信息可通过环境变量覆盖，详见 README。" -ForegroundColor Yellow
}

Write-Host "==> [4/5] 迁移数据库并初始化演示数据" -ForegroundColor Cyan
python manage.py migrate
python manage.py init_demo

Write-Host "==> [5/5] 启动开发服务器: http://127.0.0.1:8000/" -ForegroundColor Cyan
python manage.py runserver 0.0.0.0:8000

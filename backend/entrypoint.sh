#!/usr/bin/env bash
set -e

DB_HOST="${DB_HOST:-mysql}"
DB_PORT="${DB_PORT:-3306}"

echo "等待数据库 ${DB_HOST}:${DB_PORT} ..."
until nc -z "$DB_HOST" "$DB_PORT"; do
  sleep 2
done
echo "数据库已就绪。"

python manage.py migrate --noinput
python manage.py collectstatic --noinput

# 首次启动初始化演示数据（已存在则命令内部跳过）
if [ "${INIT_DEMO:-true}" = "true" ]; then
  python manage.py init_demo || true
fi

exec gunicorn config.wsgi:application \
  --bind 0.0.0.0:8000 \
  --workers "${GUNICORN_WORKERS:-3}" \
  --timeout 120

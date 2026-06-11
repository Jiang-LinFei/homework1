"""初始化机房信息管理系统的演示数据。

用法：python manage.py init_demo
会创建：
  * Django 后台超级用户 admin / admin123456
  * 业务用户：管理员 lifei(2001) / 学生 zhangsan(2002)、lisi(2003)，密码均为 123456
  * 若干机房任务及学生任务记录
"""
from django.contrib.auth import get_user_model
from django.core.management.base import BaseCommand

from LMS.models import StudentTask, Task, User


class Command(BaseCommand):
    help = "初始化机房信息管理系统演示数据"

    def handle(self, *args, **options):
        # Django 后台超级用户
        AuthUser = get_user_model()
        if not AuthUser.objects.filter(username='admin').exists():
            AuthUser.objects.create_superuser('admin', 'admin@example.com', 'admin123456')
            self.stdout.write(self.style.SUCCESS('已创建后台超级用户 admin / admin123456'))
        else:
            self.stdout.write('后台超级用户 admin 已存在，跳过')

        # 业务用户
        users = [
            dict(uid=2001, uname='李飞', password='123456', type='manager', room='机房A',
                 sex='男', class_field='教师', birthday='1990', tel='13800000001', email='lifei@x.com'),
            dict(uid=2002, uname='张三', password='123456', type='student', room='机房A',
                 sex='男', class_field='计科1班', birthday='2003', tel='13800000002', email='zs@x.com'),
            dict(uid=2003, uname='李四', password='123456', type='student', room='机房B',
                 sex='女', class_field='计科2班', birthday='2003', tel='13800000003', email='ls@x.com'),
        ]
        for u in users:
            User.objects.update_or_create(uid=u['uid'], defaults=u)
        self.stdout.write(self.style.SUCCESS('已创建/更新 %d 个业务用户' % len(users)))

        # 机房任务
        tasks = [
            dict(tid=1, tname='机房巡检', content='检查A机房40台机器电源与网络', students='张三,',
                 number=5, finish='发布中', progress=20, value=100, duration='3'),
            dict(tid=2, tname='系统重装', content='为B机房统一重装操作系统镜像', students='李四,',
                 number=3, finish='发布中', progress=50, value=80, duration='5'),
            dict(tid=3, tname='设备登记', content='登记新到货的20台显示器资产信息', students='',
                 number=4, finish='发布中', progress=0, value=60, duration='2'),
        ]
        for t in tasks:
            Task.objects.update_or_create(tid=t['tid'], defaults=t)
        self.stdout.write(self.style.SUCCESS('已创建/更新 %d 个机房任务' % len(tasks)))

        # 学生任务
        student_tasks = [
            dict(sid=20021, sname='机房巡检#子任务', uid=2002, uname='张三', tid=1,
                 content='A机房1-20号机检查', finish='进行中', progress=20, value=20, duration='3'),
            dict(sid=20032, sname='系统重装#子任务', uid=2003, uname='李四', tid=2,
                 content='B机房镜像分发', finish='进行中', progress=50, value=40, duration='5'),
        ]
        for s in student_tasks:
            StudentTask.objects.update_or_create(sid=s['sid'], defaults=s)
        self.stdout.write(self.style.SUCCESS('已创建/更新 %d 条学生任务' % len(student_tasks)))

        self.stdout.write(self.style.SUCCESS('演示数据初始化完成。'))

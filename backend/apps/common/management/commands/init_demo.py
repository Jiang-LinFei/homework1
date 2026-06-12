from datetime import date, time, timedelta

from django.contrib.auth import get_user_model
from django.core.management.base import BaseCommand
from django.utils import timezone

from apps.bookings.models import Booking
from apps.rooms.models import Room, Seat, TimeSlot
from apps.schedules.models import Schedule

User = get_user_model()


class Command(BaseCommand):
    help = '初始化演示数据：账号、机房、机位、节次、排课、预约'

    def handle(self, *args, **options):
        # 用户
        users = {
            'admin': ('管理员', 'admin', '13800000001'),
            'teacher': ('王老师', 'teacher', '13800000002'),
            'student': ('张同学', 'student', '13800000003'),
        }
        created_users = {}
        for username, (name, role, phone) in users.items():
            u, _ = User.objects.get_or_create(
                username=username,
                defaults={'name': name, 'role': role, 'phone': phone})
            u.name, u.role, u.phone = name, role, phone
            if role == 'admin':
                u.is_staff = u.is_superuser = True
            u.set_password(username)
            u.save()
            created_users[username] = u
        self.stdout.write('用户: admin/admin, teacher/teacher, student/student')

        # 节次
        slots_def = [
            ('第1-2节', time(8, 0), time(9, 40), 1),
            ('第3-4节', time(10, 0), time(11, 40), 2),
            ('第5-6节', time(14, 0), time(15, 40), 3),
            ('第7-8节', time(16, 0), time(17, 40), 4),
        ]
        slots = []
        for name, s, e, o in slots_def:
            ts, _ = TimeSlot.objects.get_or_create(
                name=name, defaults={'start_time': s, 'end_time': e, 'order': o})
            slots.append(ts)

        # 机房 + 机位
        rooms_def = [
            ('实训楼A301', 'A栋3楼', 60),
            ('实训楼A302', 'A栋3楼', 50),
            ('图书馆机房B201', 'B栋2楼', 40),
        ]
        rooms = []
        for name, loc, cap in rooms_def:
            r, _ = Room.objects.get_or_create(
                name=name, defaults={'location': loc, 'capacity': cap,
                                     'open_time': time(8), 'close_time': time(22),
                                     'status': 'open'})
            rooms.append(r)
            for i in range(1, min(cap, 10) + 1):
                Seat.objects.get_or_create(room=r, code=f'{i:02d}')

        # 排课（本周一开始）
        today = timezone.now().date()
        monday = today - timedelta(days=today.isoweekday() - 1)
        Schedule.objects.get_or_create(
            room=rooms[0], course_name='Python程序设计', weekday=1, timeslot=slots[0],
            defaults={'teacher': created_users['teacher'], 'start_week': 1,
                      'end_week': 18, 'semester_start': monday})
        Schedule.objects.get_or_create(
            room=rooms[1], course_name='数据库原理', weekday=3, timeslot=slots[1],
            defaults={'teacher': created_users['teacher'], 'start_week': 1,
                      'end_week': 18, 'semester_start': monday})

        # 预约示例
        Booking.objects.get_or_create(
            room=rooms[0], date=today + timedelta(days=1), timeslot=slots[2],
            defaults={'applicant': created_users['student'], 'purpose': '课程设计',
                      'status': 'approved', 'reviewer': created_users['admin'],
                      'reviewed_at': timezone.now()})
        Booking.objects.get_or_create(
            room=rooms[2], date=today + timedelta(days=2), timeslot=slots[3],
            defaults={'applicant': created_users['student'], 'purpose': '社团活动',
                      'status': 'pending'})

        self.stdout.write(self.style.SUCCESS('演示数据初始化完成'))

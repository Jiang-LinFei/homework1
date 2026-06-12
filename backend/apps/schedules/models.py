from django.conf import settings
from django.db import models

from apps.rooms.models import Room, TimeSlot


class Schedule(models.Model):
    WEEKDAYS = [
        (1, '周一'), (2, '周二'), (3, '周三'), (4, '周四'),
        (5, '周五'), (6, '周六'), (7, '周日'),
    ]

    room = models.ForeignKey(Room, on_delete=models.CASCADE, related_name='schedules',
                             verbose_name='机房')
    course_name = models.CharField('课程名称', max_length=100)
    teacher = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.SET_NULL,
                                null=True, related_name='schedules', verbose_name='教师')
    weekday = models.PositiveSmallIntegerField('星期', choices=WEEKDAYS)
    timeslot = models.ForeignKey(TimeSlot, on_delete=models.CASCADE, verbose_name='节次')
    start_week = models.PositiveSmallIntegerField('起始周', default=1)
    end_week = models.PositiveSmallIntegerField('结束周', default=18)
    semester_start = models.DateField('学期开始日(第1周周一)')
    created_at = models.DateTimeField('创建时间', auto_now_add=True)

    class Meta:
        ordering = ['weekday', 'timeslot__order']

    def __str__(self):
        return f'{self.course_name}@{self.room.name} {self.get_weekday_display()}'

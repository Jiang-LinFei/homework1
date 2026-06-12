from django.db import models


class Room(models.Model):
    class Status(models.TextChoices):
        OPEN = 'open', '开放'
        CLOSED = 'closed', '关闭'
        MAINTENANCE = 'maintenance', '维护中'

    name = models.CharField('机房名称', max_length=50, unique=True)
    location = models.CharField('位置', max_length=100, blank=True)
    capacity = models.PositiveIntegerField('容量(机位数)', default=0)
    open_time = models.TimeField('开放时间', default='08:00')
    close_time = models.TimeField('关闭时间', default='22:00')
    status = models.CharField('状态', max_length=12, choices=Status.choices,
                              default=Status.OPEN)
    description = models.TextField('备注', blank=True)
    created_at = models.DateTimeField('创建时间', auto_now_add=True)

    class Meta:
        ordering = ['name']

    def __str__(self):
        return self.name


class Seat(models.Model):
    class Status(models.TextChoices):
        AVAILABLE = 'available', '可用'
        MAINTENANCE = 'maintenance', '维护中'

    room = models.ForeignKey(Room, on_delete=models.CASCADE, related_name='seats',
                             verbose_name='所属机房')
    code = models.CharField('机位编号', max_length=20)
    status = models.CharField('状态', max_length=12, choices=Status.choices,
                              default=Status.AVAILABLE)

    class Meta:
        ordering = ['room', 'code']
        unique_together = ('room', 'code')

    def __str__(self):
        return f'{self.room.name}-{self.code}'


class TimeSlot(models.Model):
    name = models.CharField('节次名称', max_length=30)
    start_time = models.TimeField('开始时间')
    end_time = models.TimeField('结束时间')
    order = models.PositiveIntegerField('排序', default=0)

    class Meta:
        ordering = ['order', 'start_time']

    def __str__(self):
        return f'{self.name}({self.start_time:%H:%M}-{self.end_time:%H:%M})'

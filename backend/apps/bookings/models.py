from django.conf import settings
from django.db import models

from apps.rooms.models import Room, Seat, TimeSlot


class Booking(models.Model):
    class Status(models.TextChoices):
        PENDING = 'pending', '待审批'
        APPROVED = 'approved', '已通过'
        REJECTED = 'rejected', '已驳回'
        CANCELLED = 'cancelled', '已取消'

    applicant = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE,
                                  related_name='bookings', verbose_name='申请人',
                                  null=True, blank=True)
    room = models.ForeignKey(Room, on_delete=models.CASCADE, related_name='bookings',
                             verbose_name='机房')
    seat = models.ForeignKey(Seat, on_delete=models.SET_NULL, null=True, blank=True,
                             related_name='bookings', verbose_name='机位')
    date = models.DateField('预约日期')
    timeslot = models.ForeignKey(TimeSlot, on_delete=models.SET_NULL, null=True,
                                 blank=True, verbose_name='节次')
    start_time = models.TimeField('开始时间', null=True, blank=True)
    end_time = models.TimeField('结束时间', null=True, blank=True)
    purpose = models.CharField('用途', max_length=200, blank=True)
    status = models.CharField('状态', max_length=10, choices=Status.choices,
                              default=Status.PENDING)
    reviewer = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.SET_NULL,
                                 null=True, blank=True, related_name='reviewed_bookings',
                                 verbose_name='审批人')
    review_comment = models.CharField('审批意见', max_length=200, blank=True)
    reviewed_at = models.DateTimeField('审批时间', null=True, blank=True)
    created_at = models.DateTimeField('创建时间', auto_now_add=True)

    class Meta:
        ordering = ['-created_at']

    @property
    def time_range(self):
        if self.start_time and self.end_time:
            return self.start_time, self.end_time
        if self.timeslot:
            return self.timeslot.start_time, self.timeslot.end_time
        return None, None

    def __str__(self):
        return f'{self.room.name} {self.date} ({self.get_status_display()})'

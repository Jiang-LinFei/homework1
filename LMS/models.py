from django.db import models


class DailyTime(models.Model):
    """每日上机/考勤记录。"""
    id = models.CharField(primary_key=True, max_length=45)
    date = models.CharField(max_length=45, blank=True, null=True)
    start_time = models.CharField(max_length=45, blank=True, null=True)
    end_time = models.CharField(max_length=45, blank=True, null=True)
    uname = models.CharField(max_length=8, blank=True, null=True)
    time_length = models.CharField(max_length=45, blank=True, null=True)
    reason = models.CharField(max_length=200, blank=True, null=True)
    leave_time = models.CharField(max_length=8, blank=True, null=True)
    daily_timecol = models.CharField(max_length=45, blank=True, null=True)

    class Meta:
        db_table = 'daily_time'


class StudentTask(models.Model):
    """学生承接的任务及其完成情况。"""
    sid = models.IntegerField(primary_key=True)
    sname = models.CharField(max_length=8, blank=True, null=True)
    uid = models.IntegerField(blank=True, null=True)
    uname = models.CharField(max_length=45, blank=True, null=True)
    tid = models.IntegerField(blank=True, null=True)
    content = models.CharField(max_length=80, blank=True, null=True)
    finish = models.CharField(max_length=8, blank=True, null=True)
    progress = models.IntegerField(blank=True, null=True)
    value = models.IntegerField(blank=True, null=True)
    duration = models.CharField(max_length=4, blank=True, null=True)
    start_time = models.DateField(blank=True, null=True)
    end_time = models.DateField(blank=True, null=True)
    path = models.CharField(max_length=800, blank=True, null=True)

    class Meta:
        db_table = 'student_task'


class Task(models.Model):
    """机房/实验室任务。"""
    tid = models.IntegerField(primary_key=True)
    tname = models.CharField(max_length=8, blank=True, null=True)
    content = models.CharField(max_length=80, blank=True, null=True)
    students = models.CharField(max_length=80, blank=True, null=True)
    number = models.IntegerField(blank=True, null=True)
    finish = models.CharField(max_length=8, blank=True, null=True)
    progress = models.IntegerField(blank=True, null=True)
    value = models.IntegerField(blank=True, null=True)
    duration = models.CharField(max_length=4, blank=True, null=True)
    start_time = models.DateField(blank=True, null=True)
    end_time = models.DateField(blank=True, null=True)
    path = models.CharField(max_length=800, blank=True, null=True)

    class Meta:
        db_table = 'task'


class Time(models.Model):
    """上机时长统计。"""
    date = models.IntegerField(primary_key=True)
    uid = models.IntegerField(blank=True, null=True)
    time_day = models.IntegerField(blank=True, null=True)
    time_week = models.IntegerField(blank=True, null=True)
    time_month = models.IntegerField(blank=True, null=True)
    apply = models.IntegerField(blank=True, null=True)
    reason_a = models.CharField(max_length=20, blank=True, null=True)
    leave = models.IntegerField(blank=True, null=True)

    class Meta:
        db_table = 'time'


class User(models.Model):
    """系统用户（学生 / 管理员）。"""
    uid = models.IntegerField(primary_key=True)
    uname = models.CharField(max_length=8, blank=True, null=True)
    password = models.CharField(max_length=10, blank=True, null=True)
    type = models.CharField(max_length=8, blank=True, null=True)
    room = models.CharField(max_length=8, blank=True, null=True)
    sex = models.CharField(max_length=2, blank=True, null=True)
    # Field renamed because "class" is a Python reserved word.
    class_field = models.CharField(db_column='class', max_length=8, blank=True, null=True)
    birthday = models.CharField(max_length=8, blank=True, null=True)
    photo = models.CharField(max_length=20, blank=True, null=True)
    tel = models.CharField(max_length=11, blank=True, null=True)
    email = models.CharField(max_length=20, blank=True, null=True)

    class Meta:
        db_table = 'user'

    def __str__(self):
        return '%s(%s)' % (self.uname, self.uid)

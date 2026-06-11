from django.contrib import admin

from LMS.models import DailyTime, StudentTask, Task, Time, User


@admin.register(User)
class UserAdmin(admin.ModelAdmin):
    list_display = ('uid', 'uname', 'type', 'room', 'sex', 'class_field', 'tel', 'email')
    list_filter = ('type', 'room')
    search_fields = ('uid', 'uname')


@admin.register(Task)
class TaskAdmin(admin.ModelAdmin):
    list_display = ('tid', 'tname', 'content', 'students', 'number', 'finish', 'progress', 'value')
    list_filter = ('finish',)
    search_fields = ('tname', 'content')


@admin.register(StudentTask)
class StudentTaskAdmin(admin.ModelAdmin):
    list_display = ('sid', 'sname', 'uname', 'tid', 'finish', 'progress', 'value')
    search_fields = ('sname', 'uname')


@admin.register(DailyTime)
class DailyTimeAdmin(admin.ModelAdmin):
    list_display = ('id', 'uname', 'date', 'start_time', 'end_time', 'time_length')
    search_fields = ('uname',)


@admin.register(Time)
class TimeAdmin(admin.ModelAdmin):
    list_display = ('date', 'uid', 'time_day', 'time_week', 'time_month', 'apply', 'leave')

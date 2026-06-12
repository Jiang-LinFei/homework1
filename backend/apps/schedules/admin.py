from django.contrib import admin

from .models import Schedule


@admin.register(Schedule)
class ScheduleAdmin(admin.ModelAdmin):
    list_display = ('course_name', 'room', 'weekday', 'timeslot', 'teacher',
                    'start_week', 'end_week')
    list_filter = ('room', 'weekday')

from rest_framework import serializers

from .models import Schedule


class ScheduleSerializer(serializers.ModelSerializer):
    room_name = serializers.CharField(source='room.name', read_only=True)
    teacher_name = serializers.CharField(source='teacher.name', read_only=True,
                                         default='')
    timeslot_name = serializers.CharField(source='timeslot.name', read_only=True)
    weekday_display = serializers.CharField(source='get_weekday_display',
                                            read_only=True)

    class Meta:
        model = Schedule
        fields = ('id', 'room', 'room_name', 'course_name', 'teacher',
                  'teacher_name', 'weekday', 'weekday_display', 'timeslot',
                  'timeslot_name', 'start_week', 'end_week', 'semester_start',
                  'created_at')

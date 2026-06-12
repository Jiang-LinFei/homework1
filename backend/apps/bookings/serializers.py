from rest_framework import serializers

from apps.rooms.models import TimeSlot

from .conflicts import has_conflict
from .models import Booking


class BookingSerializer(serializers.ModelSerializer):
    applicant_name = serializers.CharField(source='applicant.name', read_only=True)
    room_name = serializers.CharField(source='room.name', read_only=True)
    seat_code = serializers.CharField(source='seat.code', read_only=True, default='')
    timeslot_name = serializers.CharField(source='timeslot.name', read_only=True,
                                          default='')
    status_display = serializers.CharField(source='get_status_display', read_only=True)
    reviewer_name = serializers.CharField(source='reviewer.name', read_only=True,
                                          default='')

    class Meta:
        model = Booking
        fields = ('id', 'applicant', 'applicant_name', 'room', 'room_name',
                  'seat', 'seat_code', 'date', 'timeslot', 'timeslot_name',
                  'start_time', 'end_time', 'purpose', 'status', 'status_display',
                  'reviewer', 'reviewer_name', 'review_comment', 'reviewed_at',
                  'created_at')
        read_only_fields = ('status', 'reviewer', 'review_comment', 'reviewed_at',
                            'applicant')

    def _resolve_range(self, attrs):
        timeslot = attrs.get('timeslot')
        start, end = attrs.get('start_time'), attrs.get('end_time')
        if timeslot:
            return timeslot.start_time, timeslot.end_time
        return start, end

    def validate(self, attrs):
        if not attrs.get('timeslot') and not (attrs.get('start_time')
                                              and attrs.get('end_time')):
            raise serializers.ValidationError('请选择节次，或填写开始/结束时间')
        start, end = self._resolve_range(attrs)
        if start >= end:
            raise serializers.ValidationError('结束时间必须晚于开始时间')
        if has_conflict(attrs['room'], attrs.get('seat'), attrs['date'], start, end):
            raise serializers.ValidationError('该时间段与已通过的预约或排课冲突，请另选时间/机房')
        return attrs

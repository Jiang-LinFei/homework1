from rest_framework import serializers

from .models import Room, Seat, TimeSlot


class SeatSerializer(serializers.ModelSerializer):
    room_name = serializers.CharField(source='room.name', read_only=True)
    status_display = serializers.CharField(source='get_status_display', read_only=True)

    class Meta:
        model = Seat
        fields = ('id', 'room', 'room_name', 'code', 'status', 'status_display')


class RoomSerializer(serializers.ModelSerializer):
    status_display = serializers.CharField(source='get_status_display', read_only=True)
    seat_count = serializers.IntegerField(source='seats.count', read_only=True)

    class Meta:
        model = Room
        fields = ('id', 'name', 'location', 'capacity', 'open_time', 'close_time',
                  'status', 'status_display', 'description', 'seat_count', 'created_at')


class TimeSlotSerializer(serializers.ModelSerializer):
    class Meta:
        model = TimeSlot
        fields = ('id', 'name', 'start_time', 'end_time', 'order')

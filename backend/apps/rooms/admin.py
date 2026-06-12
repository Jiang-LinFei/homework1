from django.contrib import admin

from .models import Room, Seat, TimeSlot


@admin.register(Room)
class RoomAdmin(admin.ModelAdmin):
    list_display = ('name', 'location', 'capacity', 'status')
    list_filter = ('status',)
    search_fields = ('name', 'location')


@admin.register(Seat)
class SeatAdmin(admin.ModelAdmin):
    list_display = ('room', 'code', 'status')
    list_filter = ('room', 'status')


@admin.register(TimeSlot)
class TimeSlotAdmin(admin.ModelAdmin):
    list_display = ('name', 'start_time', 'end_time', 'order')

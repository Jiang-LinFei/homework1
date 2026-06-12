from django.contrib import admin

from .models import Booking


@admin.register(Booking)
class BookingAdmin(admin.ModelAdmin):
    list_display = ('room', 'seat', 'date', 'status', 'applicant', 'created_at')
    list_filter = ('status', 'room', 'date')
    search_fields = ('purpose', 'room__name')

from datetime import datetime, timedelta

from django.db.models import Count
from django.utils import timezone
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from rest_framework.views import APIView

from apps.bookings.models import Booking
from apps.schedules.models import Schedule
from apps.schedules.utils import schedule_active_on


def _parse(d, default):
    if not d:
        return default
    try:
        return datetime.strptime(d, '%Y-%m-%d').date()
    except ValueError:
        return default


class CalendarView(APIView):
    """占用日历：合并已通过预约 + 排课，输出 FullCalendar 事件。"""
    permission_classes = [IsAuthenticated]

    def get(self, request):
        today = timezone.now().date()
        start = _parse(request.query_params.get('start'), today)
        end = _parse(request.query_params.get('end'), today + timedelta(days=7))
        room_id = request.query_params.get('room')

        bookings = Booking.objects.filter(status='approved', date__gte=start,
                                          date__lte=end).select_related('room', 'seat',
                                                                        'timeslot')
        schedules = Schedule.objects.select_related('room', 'timeslot', 'teacher')
        if room_id:
            bookings = bookings.filter(room_id=room_id)
            schedules = schedules.filter(room_id=room_id)

        events = []
        for b in bookings:
            s, e = b.time_range
            if not s:
                continue
            label = b.seat.code if b.seat else '整间'
            events.append({
                'title': f'{b.room.name}[{label}] {b.purpose or "预约"}',
                'start': f'{b.date}T{s}',
                'end': f'{b.date}T{e}',
                'type': 'booking',
                'color': '#409EFF',
            })

        d = start
        while d <= end:
            for sc in schedules:
                if schedule_active_on(sc, d):
                    events.append({
                        'title': f'{sc.room.name} 排课:{sc.course_name}',
                        'start': f'{d}T{sc.timeslot.start_time}',
                        'end': f'{d}T{sc.timeslot.end_time}',
                        'type': 'schedule',
                        'color': '#E6A23C',
                    })
            d += timedelta(days=1)
        return Response(events)


class DashboardView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        qs = Booking.objects.all()
        total = qs.count()
        by_status = {row['status']: row['count'] for row in
                     qs.values('status').annotate(count=Count('id'))}
        approved = by_status.get('approved', 0)
        reviewed = approved + by_status.get('rejected', 0)
        approval_rate = round(approved / reviewed * 100, 1) if reviewed else 0.0

        top_rooms = list(qs.values('room__name').annotate(count=Count('id'))
                         .order_by('-count')[:5])

        today = timezone.now().date()
        trend = []
        for i in range(6, -1, -1):
            day = today - timedelta(days=i)
            trend.append({'date': day.strftime('%m-%d'),
                          'count': qs.filter(date=day).count()})

        return Response({
            'total': total,
            'by_status': {
                'pending': by_status.get('pending', 0),
                'approved': approved,
                'rejected': by_status.get('rejected', 0),
                'cancelled': by_status.get('cancelled', 0),
            },
            'approval_rate': approval_rate,
            'top_rooms': [{'name': r['room__name'], 'count': r['count']}
                          for r in top_rooms],
            'trend': trend,
        })

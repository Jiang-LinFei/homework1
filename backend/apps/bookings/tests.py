from datetime import date, time

from django.test import TestCase

from apps.bookings.conflicts import has_conflict
from apps.bookings.models import Booking
from apps.rooms.models import Room, Seat, TimeSlot
from apps.schedules.models import Schedule


class ConflictTests(TestCase):
    def setUp(self):
        self.room = Room.objects.create(name='301', location='A', capacity=50,
                                        open_time=time(8), close_time=time(22),
                                        status='open')
        self.seat1 = Seat.objects.create(room=self.room, code='01')
        self.seat2 = Seat.objects.create(room=self.room, code='02')

    def test_overlap_same_room_whole(self):
        Booking.objects.create(room=self.room, date=date(2026, 6, 12),
                               start_time=time(10), end_time=time(12),
                               purpose='x', status='approved')
        self.assertTrue(has_conflict(self.room, None, date(2026, 6, 12),
                                     time(11), time(13)))

    def test_no_overlap_different_time(self):
        Booking.objects.create(room=self.room, date=date(2026, 6, 12),
                               start_time=time(10), end_time=time(12),
                               purpose='x', status='approved')
        self.assertFalse(has_conflict(self.room, None, date(2026, 6, 12),
                                      time(13), time(14)))

    def test_rejected_does_not_conflict(self):
        Booking.objects.create(room=self.room, date=date(2026, 6, 12),
                               start_time=time(10), end_time=time(12),
                               purpose='x', status='rejected')
        self.assertFalse(has_conflict(self.room, None, date(2026, 6, 12),
                                      time(11), time(13)))

    def test_different_seats_no_conflict(self):
        Booking.objects.create(room=self.room, seat=self.seat1,
                               date=date(2026, 6, 12), start_time=time(10),
                               end_time=time(12), purpose='x', status='approved')
        self.assertFalse(has_conflict(self.room, self.seat2, date(2026, 6, 12),
                                      time(11), time(13)))

    def test_whole_room_conflicts_with_seat(self):
        Booking.objects.create(room=self.room, seat=self.seat1,
                               date=date(2026, 6, 12), start_time=time(10),
                               end_time=time(12), purpose='x', status='approved')
        self.assertTrue(has_conflict(self.room, None, date(2026, 6, 12),
                                     time(11), time(13)))

    def test_schedule_occupies_room(self):
        ts = TimeSlot.objects.create(name='第1-2节', start_time=time(8),
                                     end_time=time(10), order=1)
        # 2026-06-12 是周五 (isoweekday=5)
        Schedule.objects.create(room=self.room, course_name='Python',
                                weekday=5, timeslot=ts, start_week=1, end_week=18,
                                semester_start=date(2026, 6, 8))
        self.assertTrue(has_conflict(self.room, None, date(2026, 6, 12),
                                     time(9), time(11)))

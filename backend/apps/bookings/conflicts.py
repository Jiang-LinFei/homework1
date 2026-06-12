"""机房/机位预约冲突检测。

规则：
- 同一机房、同一日期、时间区间重叠即冲突。
- 整间预约(seat=None)与该机房任意机位/整间预约互斥；
  指定机位的预约只与「同机位」或「整间」预约互斥。
- 仅与状态在 status_in（默认 approved）的预约比较。
- 同时把当天生效的排课占用纳入冲突判断。
"""


def _overlap(s1, e1, s2, e2):
    return s1 < e2 and s2 < e1


def has_conflict(room, seat, d, start, end, exclude_id=None,
                 status_in=('approved',)):
    from apps.bookings.models import Booking
    from apps.schedules.models import Schedule
    from apps.schedules.utils import schedule_active_on

    qs = Booking.objects.filter(room=room, date=d, status__in=status_in)
    if exclude_id:
        qs = qs.exclude(id=exclude_id)
    for b in qs.select_related('timeslot'):
        # 双方都指定了机位且不同 -> 不冲突
        if seat and b.seat_id and b.seat_id != seat.id:
            continue
        bs, be = b.time_range
        if bs and be and _overlap(start, end, bs, be):
            return True

    # 排课占用（视为整间机房占用）
    for sc in Schedule.objects.filter(room=room).select_related('timeslot'):
        if not schedule_active_on(sc, d):
            continue
        ss, se = sc.timeslot.start_time, sc.timeslot.end_time
        if _overlap(start, end, ss, se):
            return True
    return False

from datetime import date, timedelta


def schedule_active_on(schedule, d: date) -> bool:
    """判断某排课在指定日期是否生效（星期匹配且落在起止周内）。"""
    if schedule.weekday != d.isoweekday():
        return False
    # 第1周周一 = semester_start 所在周的周一
    first_monday = schedule.semester_start - timedelta(
        days=schedule.semester_start.isoweekday() - 1)
    week_index = (d - first_monday).days // 7 + 1
    return schedule.start_week <= week_index <= schedule.end_week

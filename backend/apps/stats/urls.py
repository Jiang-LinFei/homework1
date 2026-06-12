from django.urls import path

from .views import CalendarView, DashboardView

urlpatterns = [
    path('calendar', CalendarView.as_view(), name='calendar'),
    path('stats/dashboard', DashboardView.as_view(), name='dashboard'),
]

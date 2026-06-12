from rest_framework import viewsets

from apps.accounts.permissions import IsAdminOrReadOnly

from .models import Schedule
from .serializers import ScheduleSerializer


class ScheduleViewSet(viewsets.ModelViewSet):
    queryset = Schedule.objects.select_related('room', 'teacher', 'timeslot').all()
    serializer_class = ScheduleSerializer
    permission_classes = [IsAdminOrReadOnly]
    filterset_fields = ['room', 'weekday', 'teacher']

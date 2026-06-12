from rest_framework import viewsets

from apps.accounts.permissions import IsAdminOrReadOnly

from .models import Room, Seat, TimeSlot
from .serializers import RoomSerializer, SeatSerializer, TimeSlotSerializer


class RoomViewSet(viewsets.ModelViewSet):
    queryset = Room.objects.all()
    serializer_class = RoomSerializer
    permission_classes = [IsAdminOrReadOnly]
    filterset_fields = ['status']
    search_fields = ['name', 'location']


class SeatViewSet(viewsets.ModelViewSet):
    queryset = Seat.objects.select_related('room').all()
    serializer_class = SeatSerializer
    permission_classes = [IsAdminOrReadOnly]
    filterset_fields = ['room', 'status']


class TimeSlotViewSet(viewsets.ModelViewSet):
    queryset = TimeSlot.objects.all()
    serializer_class = TimeSlotSerializer
    permission_classes = [IsAdminOrReadOnly]
